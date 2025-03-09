#!/bin/sh
set -e

SERVICE_NAME=$1

# Change to the service directory
cd /app/backend/$SERVICE_NAME

# Watch for changes and recompile, then restart the application
while true; do
  # Start the application in the background
  mvn spring-boot:run &
  APP_PID=$!

  # Wait for changes
  inotifywait -r -e modify /app/backend/$SERVICE_NAME/src/main/

  echo "Changes detected. Recompiling and restarting..."

  # Kill the application
  kill $APP_PID 2>/dev/null || true
  wait $APP_PID 2>/dev/null || true

  # Recompile
  cd ..
  mvn compile -o -DskipTests
  cd $SERVICE_NAME

  # Give a moment for things to settle
  sleep 1
done