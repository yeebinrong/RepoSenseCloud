package com.hamburger.job.models.dao;

import com.hamburger.job.models.Job;
import com.hamburger.job.models.exceptions.StartJobException;
import com.hamburger.job.service.BatchService;
import com.hamburger.job.service.S3Service;
import com.hamburger.job.service.SqsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class JobDbDaoIntegrationTest {

    @Mock
    private DynamoDbEnhancedClient enhancedClient;
    @Mock
    private DynamoDbTable<Job> jobTable;
    @Mock
    private SqsService sqsService;
    @Mock
    private BatchService batchService;
    @Mock
    private S3Service s3Service;
    @Mock
    private DynamoDbClient dynamoDbClient;

    private JobDbDao jobDbDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(enhancedClient.table(anyString(), any(TableSchema.class))).thenReturn(jobTable);
        this.jobDbDao = new JobDbDao(dynamoDbClient, enhancedClient, sqsService, batchService, s3Service);
    }

    @Test
    void testGetAllJobs_validOwner() {
        // Setup
        String owner = "user1";
        Job job = new Job();
        List<Job> jobs = List.of(job);
        Page<Job> page = mock(Page.class);
        when(page.items()).thenReturn(jobs);
        when(page.lastEvaluatedKey()).thenReturn(null);
        PageIterable<Job> iterable = mock(PageIterable.class);
        when(iterable.iterator()).thenReturn(List.of(page).iterator());
        when(jobTable.query(any(QueryEnhancedRequest.class))).thenReturn(iterable);

        // Execute
        Optional<List<Job>> result = jobDbDao.getAllJobs(owner);

        // Verify
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        verify(jobTable).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void testGetAllJobs_withPagination() {
        // Setup for multiple pages
        String owner = "user1";
        Job job1 = new Job();
        Job job2 = new Job();
        List<Job> jobs1 = List.of(job1);
        List<Job> jobs2 = List.of(job2);
        
        Map<String, AttributeValue> lastKey = new HashMap<>();
        lastKey.put("key", AttributeValue.builder().s("value").build());
        
        // First page
        @SuppressWarnings("unchecked")
        Page<Job> page1 = mock(Page.class);
        when(page1.items()).thenReturn(jobs1);
        when(page1.lastEvaluatedKey()).thenReturn(lastKey); // Has more pages
        
        // Second page
        @SuppressWarnings("unchecked")
        Page<Job> page2 = mock(Page.class);
        when(page2.items()).thenReturn(jobs2);
        when(page2.lastEvaluatedKey()).thenReturn(null); // No more pages
        
        @SuppressWarnings("unchecked")
        PageIterable<Job> iterable1 = mock(PageIterable.class);
        when(iterable1.iterator()).thenReturn(List.of(page1).iterator());
        
        @SuppressWarnings("unchecked")
        PageIterable<Job> iterable2 = mock(PageIterable.class);
        when(iterable2.iterator()).thenReturn(List.of(page2).iterator());
        
        // First query returns page1, second query returns page2
        when(jobTable.query((QueryEnhancedRequest) argThat(req -> 
            req instanceof QueryEnhancedRequest && 
            ((QueryEnhancedRequest)req).exclusiveStartKey() == null)))
            .thenReturn(iterable1);
        when(jobTable.query((QueryEnhancedRequest) argThat(req -> 
            req instanceof QueryEnhancedRequest && 
            ((QueryEnhancedRequest)req).exclusiveStartKey() != null)))
            .thenReturn(iterable2);

        // Execute
        Optional<List<Job>> result = jobDbDao.getAllJobs(owner);

        // Verify
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size()); // Combined results from both pages
        verify(jobTable, times(2)).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void testGetAllJobs_exception() {
        when(jobTable.query(any(QueryEnhancedRequest.class))).thenThrow(new RuntimeException("fail"));
        Optional<List<Job>> result = jobDbDao.getAllJobs("user1");
        assertTrue(result.isEmpty());
        verify(jobTable).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void testGetJobsByPage_valid() {
        Job job = new Job();
        List<Job> jobs = Arrays.asList(job, job, job);
        JobDbDao spyDao = spy(jobDbDao);
        doReturn(Optional.of(jobs)).when(spyDao).getAllJobs(anyString());
        Optional<List<Job>> result = spyDao.getJobsByPage("user1", 1, 2);
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        verify(spyDao).getAllJobs("user1");
    }

    @Test
    void testGetJobsByPage_invalid() {
        JobDbDao spyDao = spy(jobDbDao);
        doReturn(Optional.empty()).when(spyDao).getAllJobs(anyString());
        Optional<List<Job>> result = spyDao.getJobsByPage("user1", 1, 2);
        assertTrue(result.isEmpty());
        verify(spyDao).getAllJobs("user1");
    }

    @Test
    void testGetJobsByPage_exception() {
        JobDbDao spyDao = spy(jobDbDao);
        doThrow(new RuntimeException("Error")).when(spyDao).getAllJobs(anyString());
        
        Optional<List<Job>> result = spyDao.getJobsByPage("user1", 1, 2);
        
        assertTrue(result.isEmpty());
        verify(spyDao).getAllJobs("user1");
    }

    @Test
    void testGetJobsByPage_emptyPage() {
        // Create list with 5 jobs
        List<Job> jobs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            jobs.add(new Job());
        }
        
        JobDbDao spyDao = spy(jobDbDao);
        doReturn(Optional.of(jobs)).when(spyDao).getAllJobs(anyString());
        
        // Request page 3 with 2 items per page (should return only 1 item)
        Optional<List<Job>> result = spyDao.getJobsByPage("user1", 3, 2);
        
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        verify(spyDao).getAllJobs("user1");
    }

    @Test
    void testGetJobsByPage_outOfBounds() {
        List<Job> jobs = List.of(new Job(), new Job());
        
        JobDbDao spyDao = spy(jobDbDao);
        doReturn(Optional.of(jobs)).when(spyDao).getAllJobs(anyString());
        
        // Request page that is beyond available data
        Optional<List<Job>> result = spyDao.getJobsByPage("user1", 2, 2);
        
        assertTrue(result.isPresent());
        assertEquals(0, result.get().size());
        verify(spyDao).getAllJobs("user1");
    }

    @Test
    void testGetJobsById_valid() {
        Job job = new Job();
        when(jobTable.getItem(any(Key.class))).thenReturn(job);
        
        Optional<Job> result = jobDbDao.getJobsById("owner", "jobId");
        
        assertTrue(result.isPresent());
        verify(jobTable).getItem(any(Key.class));
    }

    @Test
    void testGetJobsById_notFound() {
        when(jobTable.getItem(any(Key.class))).thenReturn(null);
        
        Optional<Job> result = jobDbDao.getJobsById("owner", "jobId");
        
        assertTrue(result.isEmpty());
        verify(jobTable).getItem(any(Key.class));
    }

    @Test
    void testGetJobsById_invalid() {
        when(jobTable.getItem(any(Key.class))).thenThrow(new RuntimeException("fail"));
        
        Optional<Job> result = jobDbDao.getJobsById("owner", "jobId");
        
        assertTrue(result.isEmpty());
        verify(jobTable).getItem(any(Key.class));
    }

    @Test
    void testGetJobsByKeyword_valid() {
        Job job = new Job();
        List<Job> jobs = List.of(job);
        
        when(jobTable.scan(any(ScanEnhancedRequest.class))).thenReturn(ScanEnhancedResult.create(jobs));
        
        Optional<List<Job>> result = jobDbDao.getJobsByKeyword("owner", "keyword");
        
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        verify(jobTable).scan(any(ScanEnhancedRequest.class));
    }

    @Test
    void testGetJobsByKeyword_noResults() {
        when(jobTable.scan(any(ScanEnhancedRequest.class))).thenReturn(ScanEnhancedResult.create(List.of()));
        
        Optional<List<Job>> result = jobDbDao.getJobsByKeyword("owner", "keyword");
        
        assertTrue(result.isPresent());
        assertEquals(0, result.get().size());
        verify(jobTable).scan(any(ScanEnhancedRequest.class));
    }

    @Test
    void testGetJobsByKeyword_invalid() {
        when(jobTable.scan(any(ScanEnhancedRequest.class))).thenThrow(new RuntimeException("fail"));
        
        Optional<List<Job>> result = jobDbDao.getJobsByKeyword("owner", "keyword");
        
        assertTrue(result.isEmpty());
        verify(jobTable).scan(any(ScanEnhancedRequest.class));
    }
    
    @Test
    void testGetReport_valid() {
        // Test the current implementation (returns null)
        String result = jobDbDao.getReport("jobId");
        assertNull(result);
    }

    @Test
    void testSaveJob() {
        // Current implementation is empty
        assertDoesNotThrow(() -> jobDbDao.saveJob());
    }
    
    @Test
    void testCreateJob_valid() {
        Job job = new Job();
        job.setOwner("owner");
        job.setJobId("jobId");
        
        doNothing().when(jobTable).putItem(any(Job.class));
        
        assertDoesNotThrow(() -> jobDbDao.createJob(job));
        verify(jobTable).putItem(job);
    }
    
    @Test
    void testCreateJob_exception() {
        Job job = new Job();
        
        doThrow(new RuntimeException("Error creating job")).when(jobTable).putItem(any(Job.class));
        
        assertDoesNotThrow(() -> jobDbDao.createJob(job));
        verify(jobTable).putItem(job);
    }

    @Test
    void testStartJob_withPendingStatus() {
        Job job = new Job();
        job.setStatus("Pending");
        job.setTimeZone("UTC+08");
        job.setFormatChipValues(List.of("format1", "format2"));
        
        when(jobTable.getItem(any(Key.class))).thenReturn(job);
        doAnswer(invocation -> {
            Job updatedJob = invocation.getArgument(0);
            assertEquals("Running", updatedJob.getStatus());
            assertNotNull(updatedJob.getLastUpdated());
            return updatedJob;
        }).when(jobTable).updateItem(any(Job.class));
        doNothing().when(sqsService).sendMessage(anyString(), anyString());
        
        assertDoesNotThrow(() -> jobDbDao.startJob("owner", "jobId"));
        verify(jobTable).updateItem(any(Job.class));
        verify(sqsService).sendMessage(anyString(), anyString());
    }

    @Test
    void testStartJob_withCompletedStatus() {
        Job job = new Job();
        job.setStatus("Completed");
        job.setTimeZone("UTC+08");
        job.setFormatChipValues(new ArrayList<>()); // Initialize with empty list
        
        when(jobTable.getItem(any(Key.class))).thenReturn(job);
        doAnswer(invocation -> {
            Job updatedJob = invocation.getArgument(0);
            assertEquals("Running", updatedJob.getStatus());
            assertEquals("Completed", updatedJob.getPrevStatus());
            return updatedJob;
        }).when(jobTable).updateItem(any(Job.class));
        doNothing().when(sqsService).sendMessage(anyString(), anyString());
        
        assertDoesNotThrow(() -> jobDbDao.startJob("owner", "jobId"));
        verify(jobTable).updateItem(any(Job.class));
        verify(sqsService).sendMessage(anyString(), anyString());
    }

    @Test
    void testStartJob_withFailedStatus() {
        Job job = new Job();
        job.setStatus("Failed");
        job.setTimeZone("UTC+08");
        job.setFormatChipValues(new ArrayList<>()); // Initialize with empty list
        
        when(jobTable.getItem(any(Key.class))).thenReturn(job);
        doAnswer(invocation -> {
            Job updatedJob = invocation.getArgument(0);
            assertEquals("Running", updatedJob.getStatus());
            assertEquals("Failed", updatedJob.getPrevStatus());
            return updatedJob;
        }).when(jobTable).updateItem(any(Job.class));
        doNothing().when(sqsService).sendMessage(anyString(), anyString());
        
        assertDoesNotThrow(() -> jobDbDao.startJob("owner", "jobId"));
        verify(jobTable).updateItem(any(Job.class));
        verify(sqsService).sendMessage(anyString(), anyString());
    }
    
    @Test
    void testStartJob_withInvalidStatus() {
        Job job = new Job();
        job.setStatus("Running"); // Job already running
        
        when(jobTable.getItem(any(Key.class))).thenReturn(job);
        
        assertThrows(StartJobException.class, () -> jobDbDao.startJob("owner", "jobId"));
        verify(jobTable, never()).updateItem(any(Job.class));
        verify(sqsService, never()).sendMessage(anyString(), anyString());
    }

    @Test
    void testStartJob_throwsException() {
        Job job = new Job();
        job.setStatus("Pending");
        job.setTimeZone("UTC+08");
        job.setFormatChipValues(new ArrayList<>()); // Initialize with empty list
        
        when(jobTable.getItem(any(Key.class))).thenReturn(job);
        when(jobTable.updateItem(any(Job.class))).thenThrow(new RuntimeException("Database error"));
        
        assertThrows(RuntimeException.class, () -> jobDbDao.startJob("owner", "jobId"));
        verify(sqsService, never()).sendMessage(anyString(), anyString());
    }

    @Test
    void testStartJob_notFound() {
        when(jobTable.getItem(any(Key.class))).thenReturn(null);
        
        assertThrows(StartJobException.class, () -> jobDbDao.startJob("owner", "jobId"));
        verify(jobTable, never()).updateItem(any(Job.class));
        verify(sqsService, never()).sendMessage(anyString(), anyString());
    }

    @Test
    void testStartJob_exceptionFindingJob() {
        when(jobTable.getItem(any(Key.class))).thenThrow(new RuntimeException("Database error"));
        
        assertThrows(StartJobException.class, () -> jobDbDao.startJob("owner", "jobId"));
        verify(jobTable, never()).updateItem(any(Job.class));
        verify(sqsService, never()).sendMessage(anyString(), anyString());
    }

    @Test
    void testStartJob_sqsException() {
        Job job = new Job();
        job.setStatus("Pending");
        job.setTimeZone("UTC+08");
        job.setFormatChipValues(new ArrayList<>()); // Initialize with empty list
        
        when(jobTable.getItem(any(Key.class))).thenReturn(job);
        when(jobTable.updateItem(any(Job.class))).thenReturn(job);
        doThrow(new RuntimeException("SQS error")).when(sqsService).sendMessage(anyString(), anyString());
        
        assertThrows(RuntimeException.class, () -> jobDbDao.startJob("owner", "jobId"));
        verify(jobTable).updateItem(any(Job.class)); // Job was updated
    }

    @Test
    void testEditJob_withRunningStatus() {
        Job jobTarget = new Job();
        jobTarget.setStatus("Running");
        jobTarget.setOwner("owner");
        jobTarget.setJobId("jobId");
        
        Job jobReplacement = new Job();
        jobReplacement.setOwner("owner");
        jobReplacement.setJobId("jobId");
        
        when(jobTable.getItem(any(Key.class))).thenReturn(jobTarget);
        when(s3Service.deleteReport(anyString(), anyString())).thenReturn("deleted");
        
        jobDbDao.editJob(jobReplacement);
        
        verify(batchService).terminateAllBatchJobsForJobId(anyString(), anyString());
        verify(s3Service).deleteReport(anyString(), anyString());
        verify(jobTable).updateItem(any(Job.class));
        
        // Verify that status and prevStatus were correctly set
        assertEquals("Pending", jobReplacement.getStatus());
        assertEquals("Running", jobReplacement.getPrevStatus());
    }

    @Test
    void testEditJob_withNonRunningStatus() {
        Job jobTarget = new Job();
        jobTarget.setStatus("Completed");
        jobTarget.setOwner("owner");
        jobTarget.setJobId("jobId");
        
        Job jobReplacement = new Job();
        jobReplacement.setOwner("owner");
        jobReplacement.setJobId("jobId");
        
        when(jobTable.getItem(any(Key.class))).thenReturn(jobTarget);
        when(s3Service.deleteReport(anyString(), anyString())).thenReturn("deleted");
        
        jobDbDao.editJob(jobReplacement);
        
        // Should not terminate batch jobs since job is not running
        verify(batchService, never()).terminateAllBatchJobsForJobId(anyString(), anyString());
        verify(s3Service).deleteReport(anyString(), anyString());
        verify(jobTable).updateItem(any(Job.class));
        
        // Verify that status and prevStatus were correctly set
        assertEquals("Pending", jobReplacement.getStatus());
        assertEquals("Completed", jobReplacement.getPrevStatus());
    }

    @Test
    void testEditJob_noReportToDelete() {
        Job jobTarget = new Job();
        jobTarget.setStatus("Pending");
        jobTarget.setOwner("owner");
        jobTarget.setJobId("jobId");
        
        Job jobReplacement = new Job();
        jobReplacement.setOwner("owner");
        jobReplacement.setJobId("jobId");
        
        when(jobTable.getItem(any(Key.class))).thenReturn(jobTarget);
        when(s3Service.deleteReport(anyString(), anyString())).thenReturn(null);
        
        jobDbDao.editJob(jobReplacement);
        
        verify(jobTable).updateItem(any(Job.class));
        
        // Verify that status and prevStatus were correctly set
        assertEquals("Pending", jobReplacement.getStatus());
        assertEquals("Pending", jobReplacement.getPrevStatus());
    }

    @Test
    void testEditJob_updateItemThrowsException() {
        Job jobTarget = new Job();
        jobTarget.setStatus("Pending");
        jobTarget.setOwner("owner");
        jobTarget.setJobId("jobId");
        
        Job jobReplacement = new Job();
        jobReplacement.setOwner("owner");
        jobReplacement.setJobId("jobId");
        
        when(jobTable.getItem(any(Key.class))).thenReturn(jobTarget);
        when(s3Service.deleteReport(anyString(), anyString())).thenReturn("deleted");
        doThrow(new RuntimeException("Update error")).when(jobTable).updateItem(any(Job.class));
        
        assertDoesNotThrow(() -> jobDbDao.editJob(jobReplacement));
        verify(jobTable).updateItem(any(Job.class));
    }

    @Test
    void testEditJob_notFound() {
        when(jobTable.getItem(any(Key.class))).thenReturn(null);
        
        Job jobReplacement = new Job();
        jobReplacement.setOwner("owner");
        jobReplacement.setJobId("jobId");
        
        assertDoesNotThrow(() -> jobDbDao.editJob(jobReplacement));
        verify(jobTable, never()).updateItem(any(Job.class));
    }

    @Test
    void testDeleteJob_valid() {
        Job job = new Job();
        job.setOwner("owner");
        job.setJobId("jobId");
        
        when(jobTable.getItem(any(Key.class))).thenReturn(job);
        
        assertDoesNotThrow(() -> jobDbDao.deleteJob("owner", "jobId"));
        verify(jobTable).deleteItem(job);
    }

    @Test
    void testDeleteJob_notFound() {
        when(jobTable.getItem(any(Key.class))).thenReturn(null);
        
        assertDoesNotThrow(() -> jobDbDao.deleteJob("owner", "jobId"));
        verify(jobTable, never()).deleteItem(any(Job.class));
    }

    @Test
    void testDeleteJob_throwsException() {
        Job job = new Job();
        job.setOwner("owner");
        job.setJobId("jobId");
        
        when(jobTable.getItem(any(Key.class))).thenReturn(job);
        doThrow(new RuntimeException("Delete error")).when(jobTable).deleteItem(any(Job.class));
        
        assertDoesNotThrow(() -> jobDbDao.deleteJob("owner", "jobId"));
        verify(jobTable).deleteItem(job);
    }
}

// Helper for mocking scan result
class ScanEnhancedResult<T> implements PageIterable<T> {
    private final List<T> items;
    
    private ScanEnhancedResult(List<T> items) { 
        this.items = items; 
    }
    
    public static <T> ScanEnhancedResult<T> create(List<T> items) { 
        return new ScanEnhancedResult<>(items); 
    }
    
    @Override 
    public Iterator<Page<T>> iterator() {
        Page<T> page = Mockito.mock(Page.class);
        Mockito.when(page.items()).thenReturn(items);
        Mockito.when(page.lastEvaluatedKey()).thenReturn(null);
        return List.of(page).iterator();
    }
}