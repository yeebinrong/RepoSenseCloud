#!/bin/bash

# Get the directory of the current script
SCRIPT_DIR=$(dirname "$0")

# Deploy the iam stack
cd "$SCRIPT_DIR/iam"
serverless deploy