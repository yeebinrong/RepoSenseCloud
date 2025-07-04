#!/bin/bash

set -x

# === Config ===
if [ -z "$1" ]; then
  echo "Usage: missing stage argument"
  exit 1
fi
STAGE=$1
if [ "$STAGE" != "dev" ] && [ "$STAGE" != "prod" ]; then
  echo "Invalid stage. Use 'dev' or 'prod'."
  exit 1
fi

TAG=$2
if [ -z "$TAG" ]; then
  echo "Usage: missing tag argument"
  exit 1
fi

AWS_REGION="ap-southeast-1"
EKS_CLUSTER_NAME="rsc-eks-cluster-${STAGE}"
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
IMAGE_NAME="rsc-eks"
ECR_REPO="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$IMAGE_NAME"

# === Docker Compose Services ===
SERVICES=("rsc-user-service" "rsc-job-service")

# # === Deploy infrastructure via Serverless Framework ===
echo "Deploying EKS infra via Serverless Framework..."
sls deploy --stage "$STAGE" --region "$AWS_REGION" --config eks-serverless.yml || { echo "Serverless deploy failed"; exit 1; }

SG_ID=$(aws ec2 describe-security-groups \
  --region "$AWS_REGION" \
  --filters Name=group-name,Values="eks-cluster-sg-rsc-eks-cluster-${STAGE}-*" \
  --query 'SecurityGroups[0].GroupId' \
  --output text)

if [[ "$SG_ID" == "None" ]]; then
  echo "Security group $SG_NAME not found"
  exit 1
fi

NAT_IP=$(aws ec2 describe-instances \
  --region "$AWS_REGION" \
  --filters "Name=tag:Name,Values=rsc-common-nat-instance" "Name=instance-state-name,Values=running" \
  --query 'Reservations[0].Instances[0].PrivateIpAddress' \
  --output text)

if [[ "$NAT_IP" == "None" ]]; then
  echo "❌ NAT instance not found"
  exit 1
fi

echo "Found NAT instance IP: $NAT_IP"

aws ec2 authorize-security-group-ingress \
  --region "$AWS_REGION" \
  --group-id "$SG_ID" \
  --ip-permissions "[
    {
      \"IpProtocol\": \"-1\",
      \"IpRanges\": [
        {\"CidrIp\": \"${NAT_IP}/32\", \"Description\": \"Allow NAT instance\"},
        {\"CidrIp\": \"30.255.0.0/16\", \"Description\": \"Allow private CIDR block\"}
      ]
    }
  ]" > /dev/null 2>&1

echo "Ingress rules successfully added"

# === Update kubeconfig ===
echo "Updating kubeconfig for EKS cluster..."
aws eks update-kubeconfig --region "$AWS_REGION" --name "$EKS_CLUSTER_NAME"

# === Install AWS Load Balancer Controller ===
VPC_ID=$(aws eks describe-cluster --name "$EKS_CLUSTER_NAME" --region "$AWS_REGION" --query "cluster.resourcesVpcConfig.vpcId" --output text)

# Create IAM OIDC provider if not exists
eksctl utils associate-iam-oidc-provider --cluster "$EKS_CLUSTER_NAME" --approve

# Create service account IAM role (only if not created)
cat <<EOF > iam-policy.json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "acm:DescribeCertificate",
                "acm:ListCertificates",
                "acm:GetCertificate",
                "ec2:AuthorizeSecurityGroupIngress",
                "ec2:CreateSecurityGroup",
                "ec2:CreateTags",
                "ec2:DeleteTags",
                "ec2:DeleteSecurityGroup",
                "ec2:Describe*",
                "ec2:RevokeSecurityGroupIngress",
                "elasticloadbalancing:*",
                "iam:ListServerCertificates",
                "iam:GetServerCertificate",
                "waf-regional:GetWebACLForResource",
                "waf-regional:GetWebACL",
                "waf-regional:AssociateWebACL",
                "waf-regional:DisassociateWebACL",
                "wafv2:GetWebACLForResource",
                "wafv2:GetWebACL",
                "wafv2:AssociateWebACL",
                "wafv2:DisassociateWebACL",
                "shield:GetSubscriptionState",
                "shield:DescribeProtection",
                "shield:CreateProtection",
                "shield:DeleteProtection",
                "shield:DescribeSubscription",
                "shield:ListProtections"
            ],
            "Resource": "*"
        }
    ]
}
EOF

aws iam create-policy --policy-name AWSLoadBalancerControllerIAMPolicy \
  --policy-document file://iam-policy.json || true

eksctl create iamserviceaccount \
  --cluster "$EKS_CLUSTER_NAME" \
  --namespace kube-system \
  --name aws-load-balancer-controller \
  --attach-policy-arn arn:aws:iam::$AWS_ACCOUNT_ID:policy/AWSLoadBalancerControllerIAMPolicy \
  --approve \
  --override-existing-serviceaccounts \
  --region "$AWS_REGION" || true

# Install via Helm
helm repo add eks https://aws.github.io/eks-charts
helm repo update

helm upgrade -i aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName="$EKS_CLUSTER_NAME" \
  --set region="$AWS_REGION" \
  --set vpcId="$VPC_ID" \
  --set serviceAccount.name=aws-load-balancer-controller

INGRESS_PATHS=""

if [ "$STAGE" == "dev" ]; then
  HOST_NAME="reposensecloud-dev.hamb-urger.com"
else
  HOST_NAME="reposensecloud.hamb-urger.com"
fi

SECRET_KEY=$(openssl rand -base64 32)

USER_SVC_PORT=3001
JOB_SVC_PORT=3002

# === Deploy Services ===
for SERVICE in "${SERVICES[@]}"; do
  echo "Processing $SERVICE..."

  if [ "$SERVICE" == "rsc-user-service" ]; then
    PATH_PREFIX=user
    PORT=$USER_SVC_PORT
  elif [ "$SERVICE" == "rsc-job-service" ]; then
    PATH_PREFIX=jobs
    PORT=$JOB_SVC_PORT
  fi

# === Generate Kubernetes manifest ===
cat <<EOF > "$SERVICE-deployment.yaml"
apiVersion: apps/v1
kind: Deployment
metadata:
  name: $SERVICE
spec:
  replicas: 1
  selector:
    matchLabels:
      app: $SERVICE
  template:
    metadata:
      labels:
        app: $SERVICE
    spec:
      containers:
        - name: $SERVICE
          image: $ECR_REPO:latest
          ports:
            - containerPort: $PORT
          env:
            - name: SHIPMENT_BUCKET
              value: s3://rsc-shipment-bucket
            - name: SERVICE_NAME
              value: $SERVICE
            - name: SERVICE_VERSION
              value: 1.0-SNAPSHOT
            - name: TAG
              value: "$TAG"
            - name: USER_SVC_PORT
              value: "$USER_SVC_PORT"
            - name: JOB_SVC_PORT
              value: "$JOB_SVC_PORT"
            - name: STAGE
              value: "$STAGE"
            - name: FRONTEND_ORIGIN
              value: "https://www.$HOST_NAME"
            - name: JWT_SECRET_KEY
              value: "$SECRET_KEY"
---
apiVersion: v1
kind: Service
metadata:
  name: $SERVICE
spec:
  selector:
    app: $SERVICE
  ports:
    - protocol: TCP
      port: $PORT
      targetPort: $PORT
  type: ClusterIP
EOF

  # === Apply manifest to EKS ===
  kubectl apply -f "$SERVICE-deployment.yaml" || { echo "kubectl apply failed for $SERVICE"; exit 1; }
  kubectl rollout restart deployment "$SERVICE"

  INGRESS_PATHS+="
          - path: /api/$PATH_PREFIX
            pathType: Prefix
            backend:
              service:
                name: $SERVICE
                port:
                  number: $PORT
"

  echo "$SERVICE deployed..."
done

cat <<EOF > shared-ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: rsc-shared-ingress
  annotations:
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}, {"HTTPS": 443}]'
    alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:ap-southeast-1:$AWS_ACCOUNT_ID:certificate/f15ad783-86fa-4674-936a-a236d1316b10
    alb.ingress.kubernetes.io/listen-https: "443"
    alb.ingress.kubernetes.io/healthcheck-path: /api/health
spec:
  ingressClassName: alb
  rules:
    - host: www.$HOST_NAME
      http:
        paths:
$INGRESS_PATHS
    - host: $HOST_NAME
      http:
        paths:
$INGRESS_PATHS
EOF

kubectl apply -f shared-ingress.yaml || { echo "kubectl apply failed for shared ingress"; exit 1; }

echo "EKS Deployment complete. Please manually redirect cloudfront to the EKS ALB"

  # Need to manually modify Access configuration mode to EKS API and Config Map
  # And then add the role you are using to the IAM access entries for the below commands to work
  # Make sure that the admin role is assigned as well

  # Manually add the origin for the dns and behaviour to route requests to the EKS ALB in the cloudfront distribution