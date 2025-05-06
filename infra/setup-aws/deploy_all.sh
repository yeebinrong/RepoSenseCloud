#!/bin/bash

# Get the directory of the current script
SCRIPT_DIR=$(dirname "$0")

# Deploy the iam stack using serverless framework
cd "$SCRIPT_DIR/iam"
bash deploy.sh

# Deploy the nat stack using cloudformation template
cd "../nat"
bash deploy.sh

# Deploy the s3 stack using serverless framework
cd "../s3"
bash deploy.sh