#!/bin/bash

# === Config ===
AWS_REGION="ap-southeast-1"
EKS_CLUSTER_NAME="rsc-eks-cluster"
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

# === Docker Compose Services ===
SERVICES=("rsc-user-service" "rsc-job-service") # Change to your service names

# === Deploy infrastructure via Serverless Framework ===
echo "Deploying AWS resources via Serverless Framework..."
sls deploy --stage dev --region "$AWS_REGION" --config eks-serverless.yml || { echo "Serverless deploy failed"; exit 1; }

# === Configure kubectl for EKS ===
echo "Updating kubeconfig for EKS cluster..."
aws eks update-kubeconfig --region "$AWS_REGION" --name "$EKS_CLUSTER_NAME"

# === Push Docker images to ECR and deploy to EKS ===
for SERVICE in "${SERVICES[@]}"; do
  echo "Processing $SERVICE..."

  IMAGE_NAME="$SERVICE"
  ECR_REPO="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$IMAGE_NAME"

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
            - containerPort: 80
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
      port: 80
      targetPort: 80
  type: ClusterIP
EOF

  # === Apply manifest to EKS ===
  kubectl apply -f "$SERVICE-deployment.yaml"

  echo "$SERVICE deployed..."

done

echo "EKS Deployment complete."
