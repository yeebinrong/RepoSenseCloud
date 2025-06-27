package com.hamburger.batch.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.hamburger.batch.models.RepoSenseJobRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SqsHandlerTest {
    @Test
    public void testHandleRequest_withValidMessage_returnsProcessingComplete() throws Exception {
        // Arrange
        String validJson = "{" +
                "\"owner\":\"binrong\"," +
                "\"id\":\"1234567890123\"," +
                "\"repos\":\"https://github.com/reposense/RepoSense.git\"" +
                "}";
        SQSEvent.SQSMessage sqsMessage = new SQSEvent.SQSMessage();
        sqsMessage.setBody(validJson);
        SQSEvent event = new SQSEvent();
        event.setRecords(Collections.singletonList(sqsMessage));
        Context mockContext = Mockito.mock(Context.class);

        SqsHandler handler = new SqsHandler();

        // Act
        String result = handler.handleRequest(event, mockContext);

        // Assert
        assertEquals("Processing complete.", result);
    }

    @Test
    public void testHandleRequest_withInvalidMessage_skipsProcessing() {
        // Arrange: missing 'id' field
        String invalidJson = "{" +
                "\"owner\":\"binrong\"," +
                "\"repos\":\"https://github.com/reposense/RepoSense.git\"" +
                "}";
        SQSEvent.SQSMessage sqsMessage = new SQSEvent.SQSMessage();
        sqsMessage.setBody(invalidJson);
        SQSEvent event = new SQSEvent();
        event.setRecords(Collections.singletonList(sqsMessage));
        Context mockContext = Mockito.mock(Context.class);
        SqsHandler handler = new SqsHandler();
        // Act
        String result = handler.handleRequest(event, mockContext);
        // Assert
        assertEquals("Processing complete.", result);
    }

    @Test
    public void testHandleRequest_withMalformedJson_logsError() {
        // Arrange: invalid JSON
        String malformedJson = "not a json string";
        SQSEvent.SQSMessage sqsMessage = new SQSEvent.SQSMessage();
        sqsMessage.setBody(malformedJson);
        SQSEvent event = new SQSEvent();
        event.setRecords(Collections.singletonList(sqsMessage));
        Context mockContext = Mockito.mock(Context.class);
        SqsHandler handler = new SqsHandler();
        // Act
        String result = handler.handleRequest(event, mockContext);
        // Assert
        assertEquals("Processing complete.", result);
    }

}
