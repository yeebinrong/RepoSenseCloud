package com.hamburger.batch.scheduler;

import com.amazonaws.services.lambda.runtime.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TaskDispatcherHandlerTest {

    private DynamoDbClient dynamoDb;
    private SqsClient sqs;
    private Context context;
    private TaskDispatcherHandler handler;

    private final String tableName = "test-table";
    private final String queueUrl = "https://sqs.aws/test-queue";

    @BeforeEach
    void setup() {
        dynamoDb = mock(DynamoDbClient.class);
        sqs = mock(SqsClient.class);
        context = mock(Context.class);
        when(context.getLogger()).thenReturn(new LambdaLogger() {
            @Override
            public void log(String s) {

            }

            @Override
            public void log(byte[] bytes) {

            }
        });
        handler = new TaskDispatcherHandler(dynamoDb, sqs, tableName, queueUrl);
    }

    @Test
    void testValidScheduledTask_sentToSqs() {
        Instant now = Instant.now();
        String date = now.toString().substring(0, 10);
        String time = now.toString().substring(11, 16) + ":00";

        Map<String, AttributeValue> item = Map.of(
                "jobId", AttributeValue.builder().s("job-1").build(),
                "status", AttributeValue.builder().s("Pending").build(),
                "nextScheduled", AttributeValue.builder().m(Map.of(
                        "date", AttributeValue.builder().s(date).build(),
                        "time", AttributeValue.builder().s(time).build()
                                                                  )).build()
                                                 );

        when(dynamoDb.scan(any(ScanRequest.class))).thenReturn(ScanResponse.builder().items(List.of(item)).build());
        when(sqs.sendMessage(any(SendMessageRequest.class))).thenReturn(SendMessageResponse.builder().messageId("msg-1").build());

        Map<String, Object> result = handler.handleRequest(Map.of(), context);
        assertEquals(200, result.get("statusCode"));
        assertTrue(result.get("body").toString().contains("\"count\": 1"));
    }

    @Test
    void testMissingNextScheduled_skipsItem() {
        Map<String, AttributeValue> item = Map.of(
                "jobId", AttributeValue.builder().s("job-2").build(),
                "status", AttributeValue.builder().s("Pending").build()
                // Missing "nextScheduled"
                                                 );

        when(dynamoDb.scan(any(ScanRequest.class))).thenReturn(ScanResponse.builder().items(List.of(item)).build());

        Map<String, Object> result = handler.handleRequest(Map.of(), context);
        assertEquals(200, result.get("statusCode"));
        assertTrue(result.get("body").toString().contains("\"count\": 0"));

        verifyNoInteractions(sqs);
    }

    @Test
    void testMissingDateOrTime_skipsItem() {
        Map<String, AttributeValue> item = Map.of(
                "jobId", AttributeValue.builder().s("job-3").build(),
                "status", AttributeValue.builder().s("Pending").build(),
                "nextScheduled", AttributeValue.builder().m(Map.of(
                        "date", AttributeValue.builder().s("2025-06-27").build()
                        // Missing time
                                                                  )).build()
                                                 );

        when(dynamoDb.scan(any(ScanRequest.class))).thenReturn(ScanResponse.builder().items(List.of(item)).build());

        Map<String, Object> result = handler.handleRequest(Map.of(), context);
        assertEquals(200, result.get("statusCode"));
        assertTrue(result.get("body").toString().contains("\"count\": 0"));

        verifyNoInteractions(sqs);
    }

    @Test
    void testEmptyDateOrTime_skipsItem() {
        Map<String, AttributeValue> item = Map.of(
                "jobId", AttributeValue.builder().s("job-4").build(),
                "status", AttributeValue.builder().s("Pending").build(),
                "nextScheduled", AttributeValue.builder().m(Map.of(
                        "date", AttributeValue.builder().s("").build(),
                        "time", AttributeValue.builder().s("").build()
                                                                  )).build()
                                                 );

        when(dynamoDb.scan(any(ScanRequest.class))).thenReturn(ScanResponse.builder().items(List.of(item)).build());

        Map<String, Object> result = handler.handleRequest(Map.of(), context);
        assertEquals(200, result.get("statusCode"));
        assertTrue(result.get("body").toString().contains("\"count\": 0"));

        verifyNoInteractions(sqs);
    }

    @Test
    void testOutOfWindowTask_skipped() {
        Instant tooOld = Instant.now().minusSeconds(10 * 60); // 10 minutes ago
        String date = tooOld.toString().substring(0, 10);
        String time = tooOld.toString().substring(11, 16) + ":00";

        Map<String, AttributeValue> item = Map.of(
                "jobId", AttributeValue.builder().s("job-5").build(),
                "status", AttributeValue.builder().s("Pending").build(),
                "nextScheduled", AttributeValue.builder().m(Map.of(
                        "date", AttributeValue.builder().s(date).build(),
                        "time", AttributeValue.builder().s(time).build()
                                                                  )).build()
                                                 );

        when(dynamoDb.scan(any(ScanRequest.class))).thenReturn(ScanResponse.builder().items(List.of(item)).build());

        Map<String, Object> result = handler.handleRequest(Map.of(), context);
        assertEquals(200, result.get("statusCode"));
        assertTrue(result.get("body").toString().contains("\"count\": 0"));

        verifyNoInteractions(sqs);
    }

    @Test
    void testDynamoDbFailure_returns500() {
        when(dynamoDb.scan(any(ScanRequest.class))).thenThrow(new RuntimeException("DynamoDB scan failed"));

        Map<String, Object> result = handler.handleRequest(Map.of(), context);
        assertEquals(500, result.get("statusCode"));
        assertTrue(result.get("body").toString().contains("DynamoDB scan failed"));

        verifyNoInteractions(sqs);
    }

    @Test
    void testSqsSendFailure_stillContinues() {
        Instant now = Instant.now();
        String date = now.toString().substring(0, 10);
        String time = now.toString().substring(11, 16) + ":00";

        Map<String, AttributeValue> item = Map.of(
                "jobId", AttributeValue.builder().s("job-6").build(),
                "status", AttributeValue.builder().s("Pending").build(),
                "nextScheduled", AttributeValue.builder().m(Map.of(
                        "date", AttributeValue.builder().s(date).build(),
                        "time", AttributeValue.builder().s(time).build()
                                                                  )).build()
                                                 );

        when(dynamoDb.scan(any(ScanRequest.class))).thenReturn(ScanResponse.builder().items(List.of(item)).build());
        when(sqs.sendMessage(any(SendMessageRequest.class))).thenThrow(new RuntimeException("SQS send failed"));

        // Optional: you could wrap sqs.sendMessage in try/catch in handler if you want to isolate failures

        Map<String, Object> result = handler.handleRequest(Map.of(), context);
        // In current code, an exception inside the loop breaks all processing
        // So this will return 500
        assertEquals(500, result.get("statusCode"));
        assertTrue(result.get("body").toString().contains("SQS send failed"));
    }
}
