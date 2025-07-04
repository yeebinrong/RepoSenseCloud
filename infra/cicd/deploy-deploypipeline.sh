#!/bin/bash

set -e
set -x

STAGE="${1}" # dev , production


if [ -z "$STAGE" ]; then
  echo "Usage: $0 <stage>"
  exit 1
fi

# Validate stage
if [[ "$STAGE" != "dev" && "$STAGE" != "production" ]]; then
  echo "Invalid stage. Use 'dev' or 'production'."
  exit 1
fi

aws cloudformation deploy \
  --template-file DeployPipeline.yml \
  --stack-name rsc-$STAGE-deploy-pipeline \
  --parameter-overrides ArtifactBucket=rsc-shipment-bucket Stage=$STAGE \
  --capabilities CAPABILITY_NAMED_IAM
