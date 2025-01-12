#!/bin/bash
if [ $# -ne 1 ]; then
    echo "Usage: $0 <environment>"
    exit 1
fi

ENVIRONMENT=$1

if [ "$ENVIRONMENT" != "localhost" ] && [ "$ENVIRONMENT" != "production" ]; then
    echo "Invalid environment. Accepted values are localhost and production"
    exit 1
fi

# Deploy the infrastructure
# init and plan and apply the opentofu files located relatively at ./infra/opentofu
cd ./infra/opentofu
tofu init
tofu plan -var "environment=$ENVIRONMENT"
tofu apply -var "environment=$ENVIRONMENT" -auto-approve