package com.hamburger.job.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

class SqsServiceTest {

    @Mock
    private SqsClient sqsClient;

    private SqsService sqsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create SqsService instance
        sqsService = new SqsService();
        
        // Use reflection to inject mock SqsClient
        ReflectionTestUtils.setField(sqsService, "sqsClient", sqsClient);
    }

    @Test
    void testSendMessage_success() {
        // Setup
        String jobId = "test-job-id";
        String groupId = "rsc-batch-localhost";
        String dupId = jobId + "_" + System.currentTimeMillis();
        String messageBody = "{\"key\":\"value\"}";
        String queueUrl = "https://sqs.ap-southeast-1.amazonaws.com/904233118848/rsc-sqs-queue-localhost.fifo";
        
        // Create a specific SendMessageRequest to avoid ambiguity
        SendMessageRequest expectedRequest = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(messageBody)
            .messageGroupId(jobId)  // For FIFO queues
            .messageDeduplicationId(jobId)  // For FIFO queues
            .build();
        
        // Mock the response
        SendMessageResponse mockResponse = SendMessageResponse.builder()
            .messageId("test-message-id")
            .build();
        
        // Use ArgumentCaptor to capture the actual request
        ArgumentCaptor<SendMessageRequest> requestCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);
        
        // Use specific method reference to avoid ambiguity
        when(sqsClient.sendMessage(requestCaptor.capture())).thenReturn(mockResponse);
        
        // Call the method under test
        sqsService.sendMessage(jobId, messageBody);
        
        // Verify the captured request matches expected values
        SendMessageRequest capturedRequest = requestCaptor.getValue();
        assertEquals(queueUrl, capturedRequest.queueUrl());
        assertEquals(messageBody, capturedRequest.messageBody());
        assertEquals(groupId, capturedRequest.messageGroupId());
        String deduplicationId = capturedRequest.messageDeduplicationId();
        assertTrue(deduplicationId.matches(jobId + "_\\d+"), 
            "Deduplication ID should match pattern: jobId_timestamp");
    }

    @Test
    void testSendMessage_exception() {
        // Setup
        String jobId = "test-job-id";
        String messageBody = "{\"key\":\"value\"}";
        
        // Mock exception - use proper AWS SDK exception builder pattern
        SdkException sqsException = SqsException.builder()
            .message("Test SQS exception")
            .build();
        
        // Mock exception
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
            .thenThrow(sqsException);
        
        // Assert exception is thrown
        assertThrows(RuntimeException.class, () -> {
            sqsService.sendMessage(jobId, messageBody);
        });
    }
}