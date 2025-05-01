package com.hamburger.batch.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamburger.batch.models.RepoSenseJobRequest;

import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.awssdk.services.batch.model.ContainerOverrides;
import software.amazon.awssdk.services.batch.model.KeyValuePair;
import software.amazon.awssdk.services.batch.model.SubmitJobRequest;
import software.amazon.awssdk.services.batch.model.SubmitJobResponse;

@Log4j2
public class SqsHandler implements RequestHandler<SQSEvent, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String handleRequest(SQSEvent event, Context context) {
        for (SQSEvent.SQSMessage message : event.getRecords()) {
            try {
                String messageBody = message.getBody();
                log.info("Received message: " + messageBody);

                // Parse JSON to RepoSenseJobRequest
                RepoSenseJobRequest jobRequest = OBJECT_MAPPER.readValue(messageBody, RepoSenseJobRequest.class);

                // Validate request
                if (!jobRequest.isValid()) {
                    log.info("Invalid job request: missing required fields.");
                    continue;  // Skip this message
                }

                // Submit the job to AWS Batch
                submitBatchJob(jobRequest, context);

            } catch (Exception e) {
                log.error("Failed to parse or submit job: " + e.getMessage());
            }
        }
        return "Processing complete.";
    }

    private void submitBatchJob(RepoSenseJobRequest request, Context context) {
        String jobName = "rsc-batch-job-" + request.getId() + "-" + System.currentTimeMillis();
        String jobQueue = System.getenv("BATCH_JOB_QUEUE");
        String jobDefinition = System.getenv("BATCH_JOB_DEFINITION");

        try (BatchClient batchClient = BatchClient.builder()
                .region(Region.AP_SOUTHEAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {
            ContainerOverrides overrides = ContainerOverrides.builder()
            .environment(
                // Set common environment variables for the job
                KeyValuePair.builder().name("ID").value(request.getId()).build(),
                KeyValuePair.builder().name("STAGE").value(System.getenv("STAGE")).build(),
                KeyValuePair.builder().name("REPORT_BUCKET").value(System.getenv("REPORT_BUCKET")).build(),
                // Set environment variables for the job
                KeyValuePair.builder().name("REPOS").value(request.getRepos()).build(),
                KeyValuePair.builder().name("SINCE").value(request.getSince()).build(),
                KeyValuePair.builder().name("UNTIL").value(request.getUntil()).build(),
                KeyValuePair.builder().name("FORMATS").value(request.getFormats()).build(),
                KeyValuePair.builder().name("IGNORE_STANDALONE_CONFIG").value(String.valueOf(request.getIgnoreConfig())).build(),
                KeyValuePair.builder().name("LAST_MODIFIED_DATE").value(String.valueOf(request.getLastModDate())).build(),
                KeyValuePair.builder().name("TIMEZONE").value(request.getTimezone()).build(),
                KeyValuePair.builder().name("FIND_PREVIOUS_AUTHORS").value(String.valueOf(request.getFindPrevAuthors())).build(),
                KeyValuePair.builder().name("ANALYZE_AUTHORSHIP").value(String.valueOf(request.getAnalyzeAuthorship())).build(),
                KeyValuePair.builder().name("ORIGINALITY_THRESHOLD").value(String.valueOf(request.getOriginalityThreshold())).build()
            )
            .build();
            SubmitJobRequest jobRequest = SubmitJobRequest.builder()
                .jobName(jobName)
                .jobQueue(jobQueue)
                .jobDefinition(jobDefinition)
                .containerOverrides(overrides)
                .build();

            log.info("Submitting job to AWS Batch: " + jobRequest);
            SubmitJobResponse response = batchClient.submitJob(jobRequest);
            log.info("Job submitted! Job ID: " + response.jobId());
        }
    }
}
