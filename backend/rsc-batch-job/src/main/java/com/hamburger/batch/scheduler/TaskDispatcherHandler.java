package com.hamburger.batch.scheduler;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TaskDispatcherHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final DynamoDbClient dynamoDb = DynamoDbClient.create();
    private final SqsClient sqs = SqsClient.create();

    private final String tableName = System.getenv("TABLE_NAME");
    private final String queueUrl = System.getenv("QUEUE_URL");

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        try {
            long nowMillis = System.currentTimeMillis();
            long fiveMinAgoMillis = nowMillis - (5 * 60 * 1000);

            ScanRequest scanRequest = ScanRequest.builder().tableName(tableName).filterExpression("#s = :pending").expressionAttributeNames(Map.of("#s", "status")).expressionAttributeValues(Map.of(":pending", AttributeValue.builder().s("Pending").build())).build();

            ScanResponse response = dynamoDb.scan(scanRequest);
            List<Map<String, AttributeValue>> items = response.items();

            int sentCount = 0;

            for (Map<String, AttributeValue> item : items) {
                if (!item.containsKey("nextScheduled") || !item.get("nextScheduled").m().containsKey("date") || !item.get("nextScheduled").m().containsKey("time")) {
                    continue;
                }

                String date = item.get("nextScheduled").m().get("date").s();
                String time = item.get("nextScheduled").m().get("time").s();

                if (date == null || time == null) {
                    continue;
                }
                if (date.isEmpty() || time.isEmpty()) {
                    continue;
                }

                String isoString = date + "T" + time + "Z";
                Instant scheduledAt = Instant.parse(isoString);

                if (scheduledAt.toEpochMilli() >= fiveMinAgoMillis && scheduledAt.toEpochMilli() < nowMillis) {
                    // Send to SQS
                    String jobId = item.containsKey("jobId") ? item.get("jobId").s() : "(no id)";
                    String jsonBody = toJson(item);

                    SendMessageRequest sendMsgRequest = SendMessageRequest.builder().queueUrl(queueUrl).messageBody(jsonBody).build();

                    SendMessageResponse sendMsgResponse = sqs.sendMessage(sendMsgRequest);
                    context.getLogger().log("📤 Sent task " + jobId + " to SQS\n");

                    sentCount++;
                }
            }

            return Map.of("statusCode", 200, "body", "{\"count\": " + sentCount + "}");

        } catch (Exception e) {
            context.getLogger().log("❌ Error: " + e.getMessage() + "\n");
            return Map.of("statusCode", 500, "body", "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private String toJson(Map<String, AttributeValue> item) {
        // Very basic converter — for complex data consider using Jackson to map AttributeValue -> POJO -> JSON
        return item.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue().s() + "\"").collect(Collectors.joining(",", "{", "}"));
    }
}
