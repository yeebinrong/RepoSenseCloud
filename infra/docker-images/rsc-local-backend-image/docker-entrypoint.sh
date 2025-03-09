#!/bin/sh
set -e

SERVICE_NAME=$1

# Change to the service directory
cd /app/backend/$SERVICE_NAME

# Watch for changes and recompile
while inotifywait -r -e modify /app/backend/$SERVICE_NAME/src/main/;
do 
  cd ..;
  mvn compile -o -DskipTests; 
done >/dev/null 2>&1 &

# Run the Spring Boot application
mvn spring-boot:run