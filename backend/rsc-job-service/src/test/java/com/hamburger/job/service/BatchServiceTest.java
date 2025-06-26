package com.hamburger.job.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.awssdk.services.batch.model.*;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BatchServiceTest {
    
    @Mock
    private BatchClient batchClient;
    
    private BatchService batchService;
    
    private final String TEST_JOB_QUEUE = "test-job-queue";
    private final String TEST_JOB_ID = "test-job-id";
    private final String TEST_REASON = "test-reason";
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set environment variable for testing
        String originalValue = System.getProperty("BATCH_JOB_QUEUE");
        try {
            System.setProperty("BATCH_JOB_QUEUE", TEST_JOB_QUEUE);
            
            // Create real instance of service
            batchService = new BatchService();
            
            // Replace the real BatchClient with our mock
            ReflectionTestUtils.setField(batchService, "batchClient", batchClient);
        } finally {
            // Restore original environment variable
            if (originalValue != null) {
                System.setProperty("BATCH_JOB_QUEUE", originalValue);
            } else {
                System.clearProperty("BATCH_JOB_QUEUE");
            }
        }
    }
    
    @Test
    void terminateAllBatchJobsForJobId_withMatchingJobs_shouldTerminateJobs() {
        // Arrange
        List<String> statuses = List.of("SUBMITTED", "PENDING", "RUNNABLE", "STARTING", "RUNNING");
        
        for (String status : statuses) {
            // Create job summaries, one matching and one non-matching
            JobSummary matchingJob = JobSummary.builder()
                    .jobId("batch-job-id-" + status)
                    .jobName("rsc-batch-job-" + TEST_JOB_ID + "-task")
                    .build();
            
            JobSummary nonMatchingJob = JobSummary.builder()
                    .jobId("non-matching-id-" + status)
                    .jobName("other-job")
                    .build();
            
            ListJobsResponse response = ListJobsResponse.builder()
                    .jobSummaryList(matchingJob, nonMatchingJob)
                    .build();
            
            // Mock the listJobs call for each status
            when(batchClient.listJobs(any(ListJobsRequest.class)))
                    .thenReturn(response);
        }
        
        // Act
        batchService.terminateAllBatchJobsForJobId(TEST_JOB_ID, TEST_REASON);
        
        // Assert
        // Verify listJobs was called for each status
        verify(batchClient, times(5)).listJobs(any(ListJobsRequest.class));
        
        // Verify terminateJob was called for each matching job (one per status)
        verify(batchClient, times(5)).terminateJob(any(TerminateJobRequest.class));
    }
    
    @Test
    void terminateAllBatchJobsForJobId_withNoMatchingJobs_shouldNotTerminateAnyJobs() {
        // Arrange
        List<String> statuses = List.of("SUBMITTED", "PENDING", "RUNNABLE", "STARTING", "RUNNING");
        
        for (String status : statuses) {
            // Create job summaries with non-matching job names
            JobSummary nonMatchingJob = JobSummary.builder()
                    .jobId("non-matching-id-" + status)
                    .jobName("other-job")
                    .build();
            
            ListJobsResponse response = ListJobsResponse.builder()
                    .jobSummaryList(nonMatchingJob)
                    .build();
            
            // Mock the listJobs call for each status
            when(batchClient.listJobs(any(ListJobsRequest.class)))
                    .thenReturn(response);
        }
        
        // Act
        batchService.terminateAllBatchJobsForJobId(TEST_JOB_ID, TEST_REASON);
        
        // Assert
        // Verify listJobs was called for each status
        verify(batchClient, times(5)).listJobs(any(ListJobsRequest.class));
        
        // Verify terminateJob was not called since no jobs matched
        verify(batchClient, never()).terminateJob(any(TerminateJobRequest.class));
    }
    
    @Test
    void terminateAllBatchJobsForJobId_withEmptyJobList_shouldNotTerminateAnyJobs() {
        // Arrange
        ListJobsResponse emptyResponse = ListJobsResponse.builder()
                .jobSummaryList(new ArrayList<>())
                .build();
        
        // Mock the listJobs call to return an empty list for all statuses
        when(batchClient.listJobs(any(ListJobsRequest.class)))
                .thenReturn(emptyResponse);
        
        // Act
        batchService.terminateAllBatchJobsForJobId(TEST_JOB_ID, TEST_REASON);
        
        // Assert
        // Verify listJobs was called for each status
        verify(batchClient, times(5)).listJobs(any(ListJobsRequest.class));
        
        // Verify terminateJob was not called since no jobs were found
        verify(batchClient, never()).terminateJob(any(TerminateJobRequest.class));
    }
    
    @Test
    void terminateAllBatchJobsForJobId_whenExceptionThrown_shouldCatchException() {
        // Arrange
        when(batchClient.listJobs(any(ListJobsRequest.class)))
                .thenThrow(new RuntimeException("Test exception"));
        
        // Act
        batchService.terminateAllBatchJobsForJobId(TEST_JOB_ID, TEST_REASON);
        
        // Assert
        // Verify listJobs was called once (and threw exception)
        verify(batchClient, times(1)).listJobs(any(ListJobsRequest.class));
        
        // Verify terminateJob was not called due to the exception
        verify(batchClient, never()).terminateJob(any(TerminateJobRequest.class));
    }
}