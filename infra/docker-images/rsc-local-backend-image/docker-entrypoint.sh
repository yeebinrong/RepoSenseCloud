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

  # Wait for changes using fswatch
  fswatch --monitor=poll_monitor -1 -r /app/backend/$SERVICE_NAME/src/main/

  echo "Changes detected. Recompiling and restarting..."

  # Kill the application
  kill $APP_PID 2>/dev/null || true
  wait $APP_PID 2>/dev/null || true

  # Recompile
  mvn compile -o -DskipTests

  # Give a moment for things to settle
  sleep 1
done