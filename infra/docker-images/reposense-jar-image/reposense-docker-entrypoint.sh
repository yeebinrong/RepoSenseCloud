#!/bin/sh

set -x

# Initialize command with jar execution
# -Xmx3g required else will face memory heap issue
CMD="java -Xms512m -Xmx3000m -jar /app/RepoSense.jar"

# Set default output directory if not specified
OUTPUT=${OUTPUT:-"/app/output"}
CMD="$CMD --output $OUTPUT"

# Required parameters check
if [ -z "$REPOS" ]; then
    echo "Error: REPOS environment variable is required"
    exit 1
fi
CMD="$CMD --repos $REPOS"

# Optional parameters with parameter validation
[ -n "$SINCE" ] && CMD="$CMD --since $SINCE"
[ -n "$UNTIL" ] && CMD="$CMD --until $UNTIL"
[ -n "$PERIOD" ] && CMD="$CMD --period $PERIOD"
[ -n "$FORMAT" ] && CMD="$CMD --formats $FORMAT"
[ -n "$TIMEZONE" ] && CMD="$CMD --timezone $TIMEZONE"
[ -n "$ORIGINALITY_THRESHOLD" ] && CMD="$CMD --originality-threshold $ORIGINALITY_THRESHOLD"

# Boolean flags with default values
IGNORE_CONFIG=${IGNORE_CONFIG:-"true"}
LAST_MOD_DATE=${LAST_MOD_DATE:-"false"}
FIND_PREV_AUTHORS=${FIND_PREV_AUTHORS:-"false"}
ANALYZE_AUTHORSHIP=${ANALYZE_AUTHORSHIP:-"true"}
SHALLOW_CLONING=${SHALLOW_CLONING:-"false"}
IGNORE_FILESIZE=${IGNORE_FILESIZE:-"false"}

# Add boolean flags
[ "$IGNORE_CONFIG" = "true" ] && CMD="$CMD --ignore-standalone-config"
[ "$LAST_MOD_DATE" = "true" ] && CMD="$CMD --last-modified-date"
[ "$FIND_PREV_AUTHORS" = "true" ] && CMD="$CMD --find-previous-authors"
[ "$ANALYZE_AUTHORSHIP" = "true" ] && CMD="$CMD --analyze-authorship"
[ "$SHALLOW_CLONING" = "true" ] && CMD="$CMD --shallow-cloning"
[ "$IGNORE_FILESIZE" = "true" ] && CMD="$CMD --ignore-filesize-limit"

# Print configuration
echo "RepoSense Configuration:"
echo "----------------------"
echo "Repository: $REPOS"
echo "Output Directory: $OUTPUT"
[ -n "$SINCE" ] && echo "Since: $SINCE"
[ -n "$UNTIL" ] && echo "Until: $UNTIL"
[ -n "$FORMAT" ] && echo "Formats: $FORMAT"
[ -n "$TIMEZONE" ] && echo "Timezone: $TIMEZONE"
echo "----------------------"

# Execute command
echo "CMD: $CMD"
$CMD

# Upload generated report to S3
echo "Uploading report to S3..."
if aws s3 cp --recursive "$OUTPUT" "s3://$REPORT_BUCKET/$ID/"; then # TODO: may have bug if dest folder is not cleaned first?
  echo "Report successfully uploaded to S3."
else
  echo "Failed to upload report to S3." >&2
  exit 1  # Optional: exit if the upload fails
fi

exit 0

# The report can be accessed at {output_path}/reposense-report/index.html