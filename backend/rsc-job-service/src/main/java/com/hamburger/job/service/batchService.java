package com.hamburger.job.service;
import software.amazon.awssdk.services.batch.model.ListJobsRequest;
import software.amazon.awssdk.services.batch.model.ListJobsResponse;
import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.awssdk.services.batch.model.JobSummary;
import software.amazon.awssdk.services.batch.model.TerminateJobRequest;

import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.List;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;


public class batchService {
    
    private final BatchClient batchClient;

    public batchService() {
            this.batchClient = BatchClient.builder()
            .region(Region.AP_SOUTHEAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    public void terminateAllBatchJobsForJobId(String jobId, String reason) {
    String jobQueue = System.getenv("BATCH_JOB_QUEUE");

    System.out.println("@batchService: Terminating all jobs for jobId: " + jobId + " in queue: " + jobQueue);
        try {
            for (String status : List.of("SUBMITTED", "PENDING", "RUNNABLE", "STARTING", "RUNNING")) {
                ListJobsRequest listRequest = ListJobsRequest.builder()
                        .jobQueue(jobQueue)
                        .jobStatus(status)
                        .build();
                ListJobsResponse listResponse = batchClient.listJobs(listRequest);
                for (JobSummary jobSummary : listResponse.jobSummaryList()) {
                    String jobName = jobSummary.jobName();
                    if (jobName.startsWith("rsc-batch-job-" + jobId + "-")) {
                        System.out.println("Terminating job: " + jobSummary.jobId() + " with status: " + status);
                        terminateBatchJob(jobSummary.jobId(), reason);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void terminateBatchJob(String jobId, String reason) {
        TerminateJobRequest terminateRequest = TerminateJobRequest.builder()
                .jobId(jobId)
                .reason(reason)
                .build();
        batchClient.terminateJob(terminateRequest);
    }

}



