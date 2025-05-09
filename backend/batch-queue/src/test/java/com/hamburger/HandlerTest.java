package com.hamburger;

import com.amazonaws.services.lambda.runtime.*;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.awssdk.services.batch.model.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class HandlerTest {

    @Test
    void testHandleRequest() {
        // Mock message
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setBody("test-message");

        // Mock SQS event
        SQSEvent event = new SQSEvent();
        event.setRecords(List.of(message));

        // Mock context
        Context context = mock(Context.class);
        LambdaLogger logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);

        // Create handler instance
        Handler handler = new Handler() {

            protected void submitBatchJob(String message) {
                // Overriding to prevent actual AWS call
                assertEquals("test-message", message);
            }
        };

        // Run handler
        String result = handler.handleRequest(event, context);

        // Verify result
        assertEquals("Processed successfully", result);
    }

}
