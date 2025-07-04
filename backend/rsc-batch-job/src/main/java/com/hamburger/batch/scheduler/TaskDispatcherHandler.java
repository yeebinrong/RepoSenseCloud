package com.hamburger.batch.scheduler;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TaskDispatcherHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final DynamoDbClient dynamoDb;
    private final SqsClient sqs;
    private final String tableName;
    private final String queueUrl;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public TaskDispatcherHandler() {
        this(DynamoDbClient.create(), SqsClient.create(),
            System.getenv("TABLE_NAME"), System.getenv("QUEUE_URL"));
    }

    public TaskDispatcherHandler(DynamoDbClient dynamoDb, SqsClient sqs,
                                 String tableName, String queueUrl) {
        this.dynamoDb = dynamoDb;
        this.sqs = sqs;
        this.tableName = tableName;
        this.queueUrl = queueUrl;
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Singapore"));
            LocalDate today = now.toLocalDate();

            QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .indexName("jobType-index")
                .keyConditionExpression("jobType = :scheduled")
                .expressionAttributeValues(Map.of(
                    ":scheduled", AttributeValue.builder().s("scheduled").build()
                ))
                .build();

            QueryResponse response = dynamoDb.query(queryRequest);
            List<Map<String, AttributeValue>> items = response.items();

            int sentCount = 0;

            for (Map<String, AttributeValue> item : items) {
                String frequency = item.getOrDefault("frequency", AttributeValue.builder().s("").build()).s();
                String startDateStr = item.getOrDefault("startDate", AttributeValue.builder().s("").build()).s();
                String endDateStr = item.getOrDefault("endDate", AttributeValue.builder().s("").build()).s();
                String startHourStr = item.getOrDefault("startHour", AttributeValue.builder().s("--").build()).s();
                String startMinuteStr = item.getOrDefault("startMinute", AttributeValue.builder().s("--").build()).s();
                Map<String, AttributeValue> lastUpdatedMap = item.getOrDefault("lastUpdated", AttributeValue.builder().m(Collections.emptyMap()).build()).m();
                Map<String, AttributeValue> settingsUpdatedAtMap = item.getOrDefault("settingsUpdatedAt", AttributeValue.builder().m(Collections.emptyMap()).build()).m();

                String dateStr = lastUpdatedMap.getOrDefault("date", AttributeValue.builder().s("").build()).s();
                String timeStr = lastUpdatedMap.getOrDefault("time", AttributeValue.builder().s("").build()).s();
                String settingsDateStr = settingsUpdatedAtMap.getOrDefault("date", AttributeValue.builder().s("").build()).s();
                String settingsTimeStr = settingsUpdatedAtMap.getOrDefault("time", AttributeValue.builder().s("").build()).s();

                ZonedDateTime lastUpdated;
                ZonedDateTime settingsUpdatedAt;
                try {
                    String timestampStr = dateStr + " " + timeStr; // e.g. "2025-07-04 01:33 UTC+0800"
                    String settingsTimestampStr = settingsDateStr + " " + settingsTimeStr; // e.g. "2025-07-04 01:33 UTC+0800"
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'Z");
                    lastUpdated = ZonedDateTime.parse(timestampStr, formatter);
                    settingsUpdatedAt = ZonedDateTime.parse(settingsTimestampStr, formatter);

                } catch (Exception ex) {
                    // fallback to epoch if parsing fails
                    lastUpdated = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC+8"));
                    settingsUpdatedAt = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC+8"));
                }

                if (
                    frequency.isEmpty() ||
                    (frequency.equals("hourly") && startMinuteStr.equals("--")) ||
                    (frequency.equals("daily") && (startHourStr.equals("--") || startMinuteStr.equals("--")))
                ) {
                    continue; // Invalid config
                }

                // Date check
                LocalDate startDate = startDateStr.isEmpty() ? LocalDate.MIN : LocalDate.parse(startDateStr, formatter);
                LocalDate endDate = endDateStr.isEmpty() ? LocalDate.MAX : LocalDate.parse(endDateStr, formatter);
                if (today.isBefore(startDate) || today.isAfter(endDate)) continue;

                boolean shouldRun = false;

                switch (frequency) {
                    case "minutely" -> {
                        Duration diff = Duration.between(lastUpdated, now);
                        shouldRun = diff.toMinutes() >= 5 || lastUpdated.isEqual(settingsUpdatedAt);
                    }
                    case "hourly" -> {
                        int minute = Integer.parseInt(startMinuteStr);
                        ZonedDateTime scheduledTime = now.withMinute(minute).withSecond(0).withNano(0);
                        if (now.isAfter(scheduledTime)) {
                            ZonedDateTime lastHour = scheduledTime.minusHours(1);
                            shouldRun = lastUpdated.isBefore(lastHour) || lastUpdated.isEqual(settingsUpdatedAt);
                        }
                    }
                    case "daily" -> {
                        int hour = Integer.parseInt(startHourStr);
                        int minute = Integer.parseInt(startMinuteStr);
                        ZonedDateTime scheduledTime = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
                        if (now.isAfter(scheduledTime)) {
                            ZonedDateTime yesterday = scheduledTime.minusDays(1);
                            shouldRun = lastUpdated.isBefore(yesterday) || lastUpdated.isEqual(settingsUpdatedAt);
                        }
                    }
                }

                // if status is already running, skip
                String status = item.getOrDefault("status", AttributeValue.builder().s("Pending").build()).s();
                if (shouldRun && status.equals("Running")) {
                    shouldRun = false; // Skip if already running
                    context.getLogger().log("Job " + item.get("jobId").s() + " is already running, skipping.\n");
                }

                if (shouldRun) {
                    String jobId = item.containsKey("jobId") ? item.get("jobId").s() : "(no id)";

                    String timeZone = item.getOrDefault("timeZone", AttributeValue.builder().s("UTC+08").build()).s();
                    ZoneId zoneId = ZoneId.of(timeZone.replace("UTC", "GMT")); // e.g., UTC+08 → GMT+08

                    ZonedDateTime currentTime = ZonedDateTime.now(zoneId);
                    Map<String, AttributeValue> updatedLastUpdated = Map.of(
                        "date", AttributeValue.builder().s(currentTime.format(DateTimeFormatter.ISO_LOCAL_DATE)).build(),
                        "time", AttributeValue.builder().s(
                            currentTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " UTC" + currentTime.getOffset().getId().replace(":", "")
                        ).build()
                    );

                    // Update lastUpdated in DynamoDB
                    Map<String, AttributeValue> key = Map.of(
                        "owner", item.get("owner"),
                        "jobId", item.get("jobId")
                    );

                    UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                        .tableName(tableName)
                        .key(key)
                        .updateExpression("SET lastUpdated = :lu, #st = :s")
                        .expressionAttributeNames(Map.of("#st", "status"))
                        .expressionAttributeValues(Map.of(
                            ":lu", AttributeValue.builder().m(updatedLastUpdated).build(),
                            ":s", AttributeValue.builder().s("Running").build()
                        ))
                        .build();

                    dynamoDb.updateItem(updateRequest);

                    String jsonBody = toJson(item);

                    SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .messageBody(jsonBody)
                            .messageGroupId("rsc-batch-" + System.getenv("STAGE")) // FIFO queue requires a message group ID
                            .messageDeduplicationId(jobId + "_" + System.currentTimeMillis()) // Unique ID for deduplication
                            .delaySeconds(0)
                            .build();

                    SendMessageResponse sendMsgResponse = sqs.sendMessage(sendMsgRequest);
                    // context.getLogger().log("Message sent with ID: " + sendMsgResponse.messageId() + "\n");
                    context.getLogger().log("sendMsgResponse: " + sendMsgResponse.toString() + "\n");
                    sentCount++;
                }
            }

            return Map.of("statusCode", 200, "body", "{\"count\": " + sentCount + "}");

        } catch (Exception e) {
            context.getLogger().log("Exception occurred\n");
            context.getLogger().log("Class: " + e.getClass().getName() + "\n");
            context.getLogger().log("Message: " + e.getMessage() + "\n");

            // Include full stack trace as a string
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            context.getLogger().log("Stack trace:\n" + sw.toString());

            return Map.of("statusCode", 500, "body", "{\"error\": \"" + e.getClass().getSimpleName() + "\"}");
        }
    }

    private double getNumber(Map<String, AttributeValue> item, String key, double defaultValue) {
        return item.getOrDefault(key, AttributeValue.builder().n(String.valueOf(defaultValue)).build()).n() != null
                ? Double.parseDouble(item.get(key).n())
                : defaultValue;
    }

    private String getString(Map<String, AttributeValue> item, String key, String defaultValue) {
        return item.getOrDefault(key, AttributeValue.builder().s(defaultValue).build()).s();
    }

    private boolean getBoolean(Map<String, AttributeValue> item, String key) {
        return item.getOrDefault(key, AttributeValue.builder().bool(false).build()).bool();
    }

    private String toJson(Map<String, AttributeValue> item) {
        String owner = getString(item, "owner", "");
        String jobId = getString(item, "jobId", "");
        String repoLink = getString(item, "repoLink", "");
        String sinceDate = getString(item, "sinceDate", "");
        String untilDate = getString(item, "untilDate", "");
        String timezone = getString(item, "timeZone", "");
        String period = getString(item, "period", "");
        String formats = getString(item, "formatChipValues", "");
        double originalityThreshold = getNumber(item, "originalityThreshold", 0.5);
        boolean ignoreFileSizeLimit = getBoolean(item, "ignoreFileSizeLimit");
        boolean shallowClone = getBoolean(item, "shallowClone");
        boolean lastModDate = getBoolean(item, "addLastMod");
        boolean findPrevAuthors = getBoolean(item, "prevAuthors");
        boolean analyzeAuthorship = getBoolean(item, "authorship");

        return String.format("""
            {
                "owner": "%s",
                "id": "%s",
                "repos": "%s",
                "since": "%s",
                "until": "%s",
                "formats": "%s",
                "period": "%s",
                "originalityThreshold": %.2f,
                "timezone": "%s",
                "ignoreFileSizeLimit": %b,
                "shallowClone": %b,
                "lastModDate": %b,
                "findPrevAuthors": %b,
                "analyzeAuthorship": %b
            }
            """,
            owner, jobId, repoLink, sinceDate, untilDate, formats, period,
            originalityThreshold, timezone,
            ignoreFileSizeLimit, shallowClone, lastModDate, findPrevAuthors, analyzeAuthorship
        );
    }
}
