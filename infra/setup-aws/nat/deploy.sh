#!/bin/bash

# default value of stage to dev
STAGE=${1:-dev}
STACK_NAME="rsc-${STAGE}-nat-stack"

echo "Deploying stack ${STACK_NAME} for stage ${STAGE}..."

# Check if stack already exists
if aws cloudformation describe-stacks --stack-name ${STACK_NAME} > /dev/null 2>&1; then
    echo "Stack ${STACK_NAME} already exists. Updating existing stack..."
    aws cloudformation update-stack \
      --stack-name ${STACK_NAME} \
      --template-body file://./nat-setup.yml \
      --parameters ParameterKey=Stage,ParameterValue="${STAGE}" >/dev/null 2>&1
    
    # Check if update is required or not
    if [ $? -ne 0 ]; then
        echo "No updates are to be performed. Stack is up-to-date."
        exit 0
    fi
else
    echo "Creating new stack ${STACK_NAME}..."
    aws cloudformation create-stack \
      --stack-name ${STACK_NAME} \
      --template-body file://./nat-setup.yml \
      --parameters ParameterKey=Stage,ParameterValue="${STAGE}" >/dev/null 2>&1
fi

if [ $? -ne 0 ]; then
    echo "Stack creation/update failed."
    exit 1
fi

echo "Waiting for stack operation to complete..."

# Wait for the stack to complete
aws cloudformation wait stack-create-complete --stack-name ${STACK_NAME} >/dev/null 2>&1 || \
aws cloudformation wait stack-update-complete --stack-name ${STACK_NAME} >/dev/null 2>&1

if [ $? -eq 0 ]; then
    echo "Stack ${STACK_NAME} deployed successfully!"
else
    echo "Stack operation failed or timed out. Check cloudformation for errors..."
    exit 1
fi