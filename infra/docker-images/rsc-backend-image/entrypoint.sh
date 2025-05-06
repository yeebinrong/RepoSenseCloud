#!/bin/bash

echo "Downloading JAR from $JAR_PATH..."
aws s3 cp $JAR_PATH /app/app.jar

echo "Starting app..."
exec java -jar /app/app.jar # TODO: consider to add -Xmx cmd to avoid memory heap issue