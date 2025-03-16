package com.hamburger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.*;
import java.util.*;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.*;
import software.amazon.awssdk.services.batch.*;
import software.amazon.awssdk.services.batch.model.*;

public class Handler implements RequestHandler<SQSEvent, String> {

    @Override
    public String handleRequest(SQSEvent event, Context context) {
        for (SQSEvent.SQSMessage message : event.getRecords()) {
            String messageBody = message.getBody();
            context.getLogger().log("Received message: " + messageBody);

            // Submit the job to AWS Batch
            submitBatchJob(messageBody);
        }

        return "Processed successfully";
    }

    private void submitBatchJob(String message) {
        String jobName = "JavaBatchJob";
        String jobQueue = "JavaBatchJobQueue";   // Replace with your job queue name
        String jobDefinition = "JavaBatchJob";     // Replace with your job definition name and revision
        int vcpus = 2;
        int memory = 4096;

        try (BatchClient batchClient = BatchClient.builder().region(Region.AP_SOUTHEAST_1) // Change to your AWS region
                                                  .credentialsProvider(DefaultCredentialsProvider.create()).build()) {

            SubmitJobRequest request = SubmitJobRequest
                    .builder()
                    .jobName(jobName)
                    .jobQueue(jobQueue)
                    .jobDefinition(jobDefinition)
                    .parameters(Map.of("message", message))
                    .build();

            SubmitJobResponse response = batchClient.submitJob(request);

            System.out.println("Job Submitted! Job ID: " + response.jobId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}