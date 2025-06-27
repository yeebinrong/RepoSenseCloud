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
[ -n "$FORMATS" ] && CMD="$CMD --formats $FORMATS"
[ -n "$TIMEZONE" ] && CMD="$CMD --timezone $TIMEZONE"
[ -n "$ORIGINALITY_THRESHOLD" ] && CMD="$CMD --originality-threshold $ORIGINALITY_THRESHOLD"

# Boolean flags with default values
LAST_MOD_DATE=${LAST_MOD_DATE:-"false"}
FIND_PREV_AUTHORS=${FIND_PREV_AUTHORS:-"false"}
ANALYZE_AUTHORSHIP=${ANALYZE_AUTHORSHIP:-"true"}
SHALLOW_CLONING=${SHALLOW_CLONING:-"false"}
IGNORE_FILESIZE_LIMIT=${IGNORE_FILESIZE_LIMIT:-"false"}

# Add boolean flags
[ "$IGNORE_FILESIZE_LIMIT" = "true" ] && CMD="$CMD --ignore-filesize-limit"
[ "$LAST_MOD_DATE" = "true" ] && CMD="$CMD --last-modified-date"
[ "$FIND_PREV_AUTHORS" = "true" ] && CMD="$CMD --find-previous-authors"
[ "$ANALYZE_AUTHORSHIP" = "true" ] && CMD="$CMD --analyze-authorship"
[ "$SHALLOW_CLONING" = "true" ] && CMD="$CMD --shallow-cloning"

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
if aws s3 cp --recursive "$OUTPUT" "s3://$REPORT_BUCKET/$OWNER/$JOBID/"; then
    echo "Report successfully uploaded to S3."
    # Update DDB job to Completed
    CUR_STATUS=$(aws dynamodb get-item \
    --table-name rsc-localhost-job-data \
    --key "{\"owner\": {\"S\": \"$OWNER\"}, \"jobId\": {\"S\": \"$JOBID\"}}" \
    --query "Item.status.S" \
    --output text)

    aws dynamodb update-item \
        --table-name rsc-localhost-job-data \
        --key "{\"owner\": {\"S\": \"$OWNER\"}, \"jobId\": {\"S\": \"$JOBID\"}}" \
        --update-expression "SET #s = :completed, #ps = :prev" \
        --expression-attribute-names '{"#s": "status", "#ps": "prevStatus"}' \
        --expression-attribute-values '{":completed": {"S": "Completed"}, ":prev": {"S": "'"$CUR_STATUS"'"}}' \
        --region ap-southeast-1
    STATUS=0

    # Zip report in local folder
    echo "Zipping the report folder..."
    cd "$OUTPUT"
    zip -r /tmp/reposense-report.zip ./*
    cd -
    # Upload zipped report to S3
    echo "Uploading zipped report to S3..."
    if aws s3 cp /tmp/reposense-report.zip "s3://$REPORT_BUCKET/$OWNER/$JOBID/reposense-report.zip"; then
        echo "Zipped report successfully uploaded to S3."
    else
        echo "Failed to upload zipped report to S3." >&2
    fi
    # Remove local zipped report after upload
    echo "Cleaning up local zipped report..."
    rm -f /tmp/reposense-report.zip
else
    # Update DDB job to Failed
    CUR_STATUS=$(aws dynamodb get-item \
    --table-name rsc-localhost-job-data \
    --key "{\"owner\": {\"S\": \"$OWNER\"}, \"jobId\": {\"S\": \"$JOBID\"}}" \
    --query "Item.status.S" \
    --output text)

    aws dynamodb update-item \
        --table-name rsc-localhost-job-data \
        --key "{\"owner\": {\"S\": \"$OWNER\"}, \"jobId\": {\"S\": \"$JOBID\"}}" \
        --update-expression "SET #s = :failed, #ps = :prev" \
        --expression-attribute-names '{"#s": "status", "#ps": "prevStatus"}' \
        --expression-attribute-values '{":failed": {"S": "Failed"}, ":prev": {"S": "'"$CUR_STATUS"'"}}' \
        --region ap-southeast-1
    echo "Failed to upload report to S3." >&2
    STATUS=1
fi
# Always update lastUpdated at the end
# Get the timeZone value from DynamoDB
RAW_TIMEZONE=$(aws dynamodb get-item \
    --table-name rsc-localhost-job-data \
    --key "{\"owner\": {\"S\": \"$OWNER\"}, \"jobId\": {\"S\": \"$JOBID\"}}" \
    --query "Item.timeZone.S" \
    --output text)

# Convert "UTC+08" to "Etc/GMT-8" (POSIX reverses the sign)
TIMEZONE=$(echo "$RAW_TIMEZONE" | sed -E 's/UTC\+0*([0-9]+)/Etc\/GMT-\1/;s/UTC\-0*([0-9]+)/Etc\/GMT+\1/')

# Use TIMEZONE to get the current date and time in that zone
# Get the time in the specified timezone
TIME_ONLY=$(TZ="$TIMEZONE" date +%H:%M)
# Get the UTC offset in +0800 format (no colon)
UTC_OFFSET=$(TZ="$TIMEZONE" date +%z)
# Combine them
TIME="${TIME_ONLY} UTC${UTC_OFFSET}"
DATE=$(TZ="$TIMEZONE" date +%F)

aws dynamodb update-item \
    --table-name rsc-localhost-job-data \
    --key "{\"owner\": {\"S\": \"$OWNER\"}, \"jobId\": {\"S\": \"$JOBID\"}}" \
    --update-expression "SET #s = :lastUpdated" \
    --expression-attribute-names '{"#s": "lastUpdated"}' \
    --expression-attribute-values "{\":lastUpdated\": {\"M\": {\"date\": {\"S\": \"$DATE\"}, \"time\": {\"S\": \"$TIME\"}}}}" \
    --region ap-southeast-1

exit $STATUS
# The report can be accessed at {output_path}/reposense-report/index.html