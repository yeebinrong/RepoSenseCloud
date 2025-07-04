#!/bin/bash

set -e
set -x

# Usage check: require stage argument and validate allowed values
if [ -z "$1" ]; then
  echo "Error: Missing stage argument. Please specify 'dev' or 'production'."
  exit 1
fi

STAGE="$1"
REBUILD_PROJECT="${2:-true}"

if [[ "$STAGE" != "dev" && "$STAGE" != "production" ]]; then
  echo "Error: Invalid stage '$STAGE'. Allowed values are 'dev' or 'production'."
  exit 1
fi

echo "Deploying stage: $STAGE"

# CD to current directory
cd "$(dirname "$0")"

if [ "$STAGE" == "dev" ]; then
  HOST_NAME="www.reposensecloud-dev.hamb-urger.com"
else
  HOST_NAME="www.reposensecloud.hamb-urger.com"
fi

# Build the frontend for deployment
REACT_APP_USER_SERVICE_URL=api/user \
REACT_APP_JOB_SERVICE_URL=api/jobs \
REACT_APP_REPORT_BUCKET_URL=https://rsc-reports-$STAGE.s3.ap-southeast-1.amazonaws.com \

if [ "$REBUILD_PROJECT" == "true" ]; then
  echo "Rebuilding frontend project..."
  npm run build
else
  echo "Skipping frontend project rebuild."
fi

# Deploy frontend using serverless client deploy for the given stage
serverless client deploy --stage "$STAGE" --no-confirm

AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

# Get all distribution IDs
DIST_IDS=$(aws cloudfront list-distributions --query "DistributionList.Items[].Id" --output text)
DISTRIBUTION_ID="None"
for DIST_ID in $DIST_IDS; do
  TAGS_JSON=$(aws cloudfront list-tags-for-resource --resource "arn:aws:cloudfront::$AWS_ACCOUNT_ID:distribution/$DIST_ID" --output json)
  STAGE_VALUE=$(echo "$TAGS_JSON" | jq -r '.Tags.Items[] | select(.Key=="STAGE") | .Value')
  if [ "$STAGE_VALUE" == "$STAGE" ]; then
    DISTRIBUTION_ID="$DIST_ID"
    echo "Found CloudFront distribution with ID: $DISTRIBUTION_ID for stage: $STAGE"
    break
  fi
done

if [ "$DISTRIBUTION_ID" == "None" ] || [ -z "$DISTRIBUTION_ID" ]; then
  echo "No CloudFront distribution found with tag stage=$STAGE"
else
  echo "Invalidating CloudFront distribution $DISTRIBUTION_ID for stage $STAGE"
  if aws cloudfront create-invalidation --distribution-id "$DISTRIBUTION_ID" --paths "/*" > /dev/null 2>&1; then
    echo "Invalidation submitted for $DISTRIBUTION_ID"
  else
    echo "Failed to submit invalidation"
    exit 1
  fi
fi