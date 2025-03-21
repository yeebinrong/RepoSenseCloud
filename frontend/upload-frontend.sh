#!/bin/bash

# CD to current directory
cd "$(dirname "$0")"

# Build the frontend for deployment
npm run build

# Upload static files to s3 (default stage is dev)
# Use "serverless deploy" instead of "serverless client deploy" to deploy the serverless.yml
serverless client deploy