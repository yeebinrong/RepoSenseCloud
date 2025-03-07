#!/bin/sh
set -e

# Set SERVICE_NAME from the first argument
SERVICE_NAME=$1

# Change to the service directory
cd /app/backend/

mvn install -DskipTests

cd ${SERVICE_NAME}

# Watch for changes and recompile
while inotifywait -r -e modify ./src/main/;
do 
  cd ..;
  mvn install -DskipTests;
  cd ${SERVICE_NAME};
  mvn compile -o -DskipTests;
done >/dev/null 2>&1 &

# Run the Spring Boot application
mvn spring-boot:run