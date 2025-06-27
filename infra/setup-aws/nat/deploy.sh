#!/bin/bash

set -x

# default value of stage to dev
STAGE=${1:-common}
STACK_NAME="rsc-infra-nat-${STAGE}"

echo "Deploying stack ${STACK_NAME} for stage ${STAGE}..."

IS_UPDATE=false

# Check if stack already exists
if aws cloudformation describe-stacks --stack-name ${STACK_NAME} > /dev/null 2>&1; then
    echo "Stack ${STACK_NAME} already exists. Updating existing stack..."

    UPDATE_OUTPUT=$(aws cloudformation update-stack \
    --stack-name ${STACK_NAME} \
    --template-body file://./nat-setup.yml \
    --parameters ParameterKey=Stage,ParameterValue="${STAGE}" \
    --capabilities CAPABILITY_NAMED_IAM 2>&1)

    UPDATE_EXIT_CODE=$?

    if [[ $UPDATE_EXIT_CODE -ne 0 ]]; then
        if echo "$UPDATE_OUTPUT" | grep -q "No updates are to be performed"; then
            echo "No updates are to be performed. Stack is up-to-date."
            exit 0
        else
            echo "Error updating stack:"
            echo "$UPDATE_OUTPUT"
            exit $UPDATE_EXIT_CODE
        fi
    fi

    echo "Update initiated successfully."
    IS_UPDATE=true
else
    echo "Creating new stack ${STACK_NAME}..."
    CREATE_OUTPUT=$(aws cloudformation create-stack \
      --stack-name ${STACK_NAME} \
      --template-body file://./nat-setup.yml \
      --parameters ParameterKey=Stage,ParameterValue="${STAGE}" \
      --capabilities CAPABILITY_NAMED_IAM 2>&1)

    if [ $? -ne 0 ]; then
        echo "Stack creation failed:"
        echo "$CREATE_OUTPUT"
        exit 1
    fi
fi

echo "Waiting for stack operation to complete..."

# Wait for the correct operation to complete
if [ "$IS_UPDATE" = true ]; then
    aws cloudformation wait stack-update-complete --stack-name ${STACK_NAME}
else
    aws cloudformation wait stack-create-complete --stack-name ${STACK_NAME}
fi

if [ $? -eq 0 ]; then
    echo "✅ Stack ${STACK_NAME} deployed successfully!"
else
    echo "❌ Stack operation failed or timed out. Check CloudFormation console for more details..."
    exit 1
fi
