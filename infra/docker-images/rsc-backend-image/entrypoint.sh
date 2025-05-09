#!/bin/bash

set -x

# SHIPMENT_BUCKET = s3://rsc-shipment-bucket
# SERVICE_NAME = rsc-user-service
# SERVICE_VERSION = 1.0-SNAPSHOT

SHIPMENT_BUCKET=${SHIPMENT_BUCKET:-"s3://rsc-shipment-bucket"}
SERVICE_NAME=${SERVICE_NAME:-""}
SERVICE_VERSION=${SERVICE_VERSION:-"1.0-SNAPSHOT"}

echo "Downloading JAR from $SHIPMENT_BUCKET..."
aws s3 cp $SHIPMENT_BUCKET/backend/$SERVICE_NAME/$SERVICE_NAME-${SERVICE_VERSION}.jar /app/app.jar

echo "Starting app..."
exec java -jar /app/app.jar # TODO: consider to add -Xmx cmd to avoid memory heap issue