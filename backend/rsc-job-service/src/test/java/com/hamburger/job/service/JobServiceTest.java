package com.hamburger.job.service;

import com.hamburger.job.models.Job;
import com.hamburger.job.models.dao.JobDbDao;
import com.hamburger.job.models.exceptions.StartJobException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    private JobService jobService;

    @Mock
    private JobDbDao jobDbDao;

    private final String OWNER = "testUser";
    private final String JOB_ID = "job123";

    @BeforeEach
    void setUp() {
        jobService = new JobService(jobDbDao);
    }

    @Test
    void getAllJobs_returnsAllJobs() {
        // Arrange
        List<Job> expectedJobs = new ArrayList<>();
        expectedJobs.add(createTestJob("job1"));
        expectedJobs.add(createTestJob("job2"));
        when(jobDbDao.getAllJobs(OWNER)).thenReturn(Optional.of(expectedJobs));

        // Act
        Optional<List<Job>> result = jobService.getAllJobs(OWNER);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        verify(jobDbDao).getAllJobs(OWNER);
    }

    @Test
    void getJobsByPage_withValidPage_returnsPagedResults() {
        // Arrange
        List<Job> allJobs = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            allJobs.add(createTestJob("job" + i));
        }
        when(jobDbDao.getAllJobs(OWNER)).thenReturn(Optional.of(allJobs));

        // Act - get page 2 with 5 items per page
        List<Job> result = jobService.getJobsByPage(OWNER, 2, 5);

        // Assert
        assertEquals(5, result.size());
        assertEquals("job6", result.get(0).getJobId());
        assertEquals("job10", result.get(4).getJobId());
        // 1. if null 2. actual retrieval
        verify(jobDbDao, times(2)).getAllJobs(OWNER);
    }
    
    @Test
    void getJobsByPage_withLastPage_returnsRemainingResults() {
        // Arrange
        List<Job> allJobs = new ArrayList<>();
        for (int i = 1; i <= 13; i++) {
            allJobs.add(createTestJob("job" + i));
        }
        when(jobDbDao.getAllJobs(OWNER)).thenReturn(Optional.of(allJobs));

        // Act - get page 3 with 5 items per page (should return 3 items)
        List<Job> result = jobService.getJobsByPage(OWNER, 3, 5);

        // Assert
        assertEquals(3, result.size());
        assertEquals("job11", result.get(0).getJobId());
        assertEquals("job13", result.get(2).getJobId());
        // 1. if null 2. actual retrieval
        verify(jobDbDao, times(2)).getAllJobs(OWNER);
    }
    
    @Test
    void getJobsByPage_withEmptyList_returnsEmptyList() {
        // Arrange
        when(jobDbDao.getAllJobs(OWNER)).thenReturn(Optional.of(new ArrayList<>()));

        // Act
        List<Job> result = jobService.getJobsByPage(OWNER, 1, 10);

        // Assert
        assertTrue(result.isEmpty());
        // 1. if null 2. actual retrieval
        verify(jobDbDao, times(2)).getAllJobs(OWNER);
    }
    
    @Test
    void getJobsByPage_withNullOptional_returnsEmptyList() {
        // Arrange
        when(jobDbDao.getAllJobs(OWNER)).thenReturn(null);

        // Act
        List<Job> result = jobService.getJobsByPage(OWNER, 1, 10);

        // Assert
        assertTrue(result.isEmpty());
        verify(jobDbDao).getAllJobs(OWNER);
    }

    @Test
    void getJobsById_withExistingId_returnsJob() {
        // Arrange
        Job expectedJob = createTestJob(JOB_ID);
        when(jobDbDao.getJobsById(OWNER, JOB_ID)).thenReturn(Optional.of(expectedJob));

        // Act
        Optional<Job> result = jobService.getJobsById(OWNER, JOB_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(JOB_ID, result.get().getJobId());
        verify(jobDbDao).getJobsById(OWNER, JOB_ID);
    }

    @Test
    void getJobsById_withNonExistingId_returnsEmptyOptional() {
        // Arrange
        when(jobDbDao.getJobsById(OWNER, JOB_ID)).thenReturn(Optional.empty());

        // Act
        Optional<Job> result = jobService.getJobsById(OWNER, JOB_ID);

        // Assert
        assertTrue(result.isEmpty());
        verify(jobDbDao).getJobsById(OWNER, JOB_ID);
    }

    @Test
    void getJobsByKeyword_withMatchingKeyword_returnsJobs() {
        // Arrange
        List<Job> expectedJobs = new ArrayList<>();
        expectedJobs.add(createTestJob("job-test-123"));
        expectedJobs.add(createTestJob("job-test-456"));
        when(jobDbDao.getJobsByKeyword(OWNER, "test")).thenReturn(Optional.of(expectedJobs));

        // Act
        Optional<List<Job>> result = jobService.getJobsByKeyword(OWNER, "test");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        verify(jobDbDao).getJobsByKeyword(OWNER, "test");
    }

    @Test
    void getJobsByKeyword_withNoMatches_returnsEmptyOptional() {
        // Arrange
        when(jobDbDao.getJobsByKeyword(OWNER, "nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<List<Job>> result = jobService.getJobsByKeyword(OWNER, "nonexistent");

        // Assert
        assertTrue(result.isEmpty());
        verify(jobDbDao).getJobsByKeyword(OWNER, "nonexistent");
    }

    @Test
    void getReport_returnsReportUrl() {
        // Arrange
        String expectedUrl = "https://example.com/report.html";
        when(jobDbDao.getReport(JOB_ID)).thenReturn(expectedUrl);

        // Act
        String result = jobService.getReport(JOB_ID);

        // Assert
        assertEquals(expectedUrl, result);
        verify(jobDbDao).getReport(JOB_ID);
    }

    @Test
    void saveJob_callsDao() {
        // Act
        jobService.saveJob();

        // Assert
        verify(jobDbDao).saveJob();
    }

    @Test
    void createJob_callsDao() {
        // Arrange
        Job job = createTestJob(JOB_ID);

        // Act
        jobService.createJob(job);

        // Assert
        verify(jobDbDao).createJob(job);
    }

    @Test
    void startJob_callsDao() {
        // Act
        jobService.startJob(OWNER, JOB_ID);

        // Assert
        verify(jobDbDao).startJob(OWNER, JOB_ID);
    }

    @Test
    void editJob_callsDao() {
        // Arrange
        Job job = createTestJob(JOB_ID);

        // Act
        jobService.editJob(job);

        // Assert
        verify(jobDbDao).editJob(job);
    }

    @Test
    void deleteJob_callsDao() {
        // Act
        jobService.deleteJob(OWNER, JOB_ID);

        // Assert
        verify(jobDbDao).deleteJob(OWNER, JOB_ID);
    }

    /**
     * Helper method to create a test Job instance
     */
    private Job createTestJob(String jobId) {
        Job job = new Job();
        job.setJobId(jobId);
        job.setOwner(OWNER);
        job.setJobName("Test Job " + jobId);
        return job;
    }
}