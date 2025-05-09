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
AWS_REGION="ap-southeast-1"
EKS_CLUSTER_NAME="rsc-eks-cluster-${STAGE}"
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
IMAGE_NAME="rsc-eks"
ECR_REPO="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$IMAGE_NAME"

# === Docker Compose Services ===
SERVICES=("rsc-user-service" "rsc-job-service") # Change to your service names

# === Deploy infrastructure via Serverless Framework ===
echo "Deploying EKS infra via Serverless Framework..."
sls deploy --stage "$STAGE" --region "$AWS_REGION" --config eks-serverless.yml || { echo "Serverless deploy failed"; exit 1; }

# === Configure kubectl for EKS ===
echo "Updating kubeconfig for EKS cluster..."
aws eks update-kubeconfig --region "$AWS_REGION" --name "$EKS_CLUSTER_NAME"

# === Deploy to EKS ===
for SERVICE in "${SERVICES[@]}"; do
  echo "Processing $SERVICE..."

  PORT=80
  # If SERVICE == rsc-user-service, set port as 3000
  if [ "$SERVICE" == "rsc-user-service" ]; then
    PORT=3001
  elif [ "$SERVICE" == "rsc-job-service" ]; then
    PORT=3002
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
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: $SERVICE
  annotations:
    kubernetes.io/ingress.class: alb                # ALB Ingress Controller
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}]'  # Listen on HTTP (port 80)
    alb.ingress.kubernetes.io/group.name: $SERVICE   # Optional: Group the ALB under a specific name
    alb.ingress.kubernetes.io/group.order: '1'      # Optional: To order multiple ingress rules
spec:
  ingressClassName: alb
  rules:
    - http:
        paths:
          - path: /api
            pathType: Prefix
            backend:
              service:
                name: $SERVICE
                port:
                  number: $PORT


EOF

  # === Apply manifest to EKS ===
  kubectl apply -f "$SERVICE-deployment.yaml" || { echo "kubectl apply failed for $SERVICE"; exit 1; }
  kubectl rollout restart deployment "$SERVICE"

  echo "$SERVICE deployed..."

done

echo "EKS Deployment complete."
