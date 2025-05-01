#!/bin/bash

# This script builds the reposense-jar image which is to be used in the AWS Batch Job and 
# uploads it directly to the AWS ECR. The AWS Batch job will be able to directly use the image
# from the AWS ECR.

# Exit on any error
set -e

# Get the directory of the script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# Go 3 levels up to the root of the project
cd "$SCRIPT_DIR/../../.." && pwd

# === CONFIGURATION ===
AWS_REGION="ap-southeast-1"            # Change to your region
ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --output text)
REPO_NAME="rsc-batch-jobs"                     # Change to your ECR repository name
IMAGE_TAG="latest"                     # Change to your image tag if needed

# === Derived values ===
ECR_URI="${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${REPO_NAME}"
DOCKERFILE_PATH="./infra/docker-images/reposense-jar-image/Dockerfile"  # Or customize if needed

echo "Logging into ECR..."
aws ecr get-login-password --region $AWS_REGION | \
    docker login --username AWS --password-stdin ${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

echo "Building Docker image with tag ${ECR_URI}..."
docker build --platform=linux/arm64 -t ${ECR_URI}:${IMAGE_TAG} -f ${DOCKERFILE_PATH} .

echo "Pushing image to ECR..."
docker push ${ECR_URI}:${IMAGE_TAG}

echo "Image pushed successfully to: ${ECR_URI}:${IMAGE_TAG}"
