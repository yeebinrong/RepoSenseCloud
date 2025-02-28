#!/bin/sh
set -e

# Change to the service directory
cd /app/service

# Watch for changes and recompile
while inotifywait -r -e modify ./src/main/; 
do 
  mvn compile -o -DskipTests; 
done >/dev/null 2>&1 &

# Run the Spring Boot application
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=false"
