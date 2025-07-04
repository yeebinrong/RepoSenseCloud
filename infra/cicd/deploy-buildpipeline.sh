#!/bin/bash

set -e
set -x

aws cloudformation deploy \
  --template-file BuildPipeline.yml \
  --stack-name rsc-common-build-pipeline \
  --parameter-overrides ArtifactBucket=rsc-shipment-bucket Stage=common \
  --capabilities CAPABILITY_NAMED_IAM
