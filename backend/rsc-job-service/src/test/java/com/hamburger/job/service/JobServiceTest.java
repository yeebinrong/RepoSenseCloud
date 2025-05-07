package com.hamburger.job.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.hamburger.job.models.Job;
import com.hamburger.job.models.dao.JobDbDao;

public class JobServiceTest {

    @Mock
    private JobDbDao jobDbDao;

    @InjectMocks
    private JobService jobService;

    private static final String OWNER = "test-owner";
    private static final String JOB_ID = "test-job-123";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllJobs_ReturnsJobs() {
        // Arrange
        List<Job> expectedJobs = Arrays.asList(new Job(), new Job());
        when(jobDbDao.getAllJobs(OWNER)).thenReturn(Optional.of(expectedJobs));

        // Act
        Optional<List<Job>> result = jobService.getAllJobs(OWNER);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedJobs, result.get());
        verify(jobDbDao).getAllJobs(OWNER);
    }

    @Test
    void getJobsByPage_WithValidInput_ReturnsPagedJobs() {
        // Arrange
        List<Job> allJobs = Arrays.asList(new Job(), new Job(), new Job(), new Job());
        when(jobDbDao.getAllJobs(OWNER)).thenReturn(Optional.of(allJobs));

        // Act
        List<Job> result = jobService.getJobsByPage(OWNER, 1, 2);

        // Assert
        assertEquals(2, result.size());
        assertEquals(allJobs.subList(0, 2), result);
    }

    @Test
    void getJobsByPage_WithEmptyJobs_ReturnsEmptyList() {
        // Arrange
        when(jobDbDao.getAllJobs(OWNER)).thenReturn(null);

        // Act
        List<Job> result = jobService.getJobsByPage(OWNER, 1, 10);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getJobsById_ExistingJob_ReturnsJob() {
        // Arrange
        Job expectedJob = new Job();
        when(jobDbDao.getJobsById(OWNER, JOB_ID)).thenReturn(Optional.of(expectedJob));

        // Act
        Optional<Job> result = jobService.getJobsById(OWNER, JOB_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedJob, result.get());
    }

    @Test
    void getJobsByKeyword_WithMatches_ReturnsMatchingJobs() {
        // Arrange
        List<Job> expectedJobs = Arrays.asList(new Job(), new Job());
        when(jobDbDao.getJobsByKeyword(OWNER, "test")).thenReturn(Optional.of(expectedJobs));

        // Act
        Optional<List<Job>> result = jobService.getJobsByKeyword(OWNER, "test");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedJobs, result.get());
    }

    @Test
    void getReport_ValidJobId_ReturnsReportUrl() {
        // Arrange
        String expectedUrl = "https://example.com/report.pdf";
        when(jobDbDao.getReport(JOB_ID)).thenReturn(expectedUrl);

        // Act
        String result = jobService.getReport(JOB_ID);

        // Assert
        assertEquals(expectedUrl, result);
    }

    @Test
    void saveJob_CallsDaoMethod() {
        // Act
        jobService.saveJob();

        // Assert
        verify(jobDbDao).saveJob();
    }

    @Test
    void createJob_ValidJob_CallsDaoMethod() {
        // Arrange
        Job job = new Job();

        // Act
        jobService.createJob(job);

        // Assert
        verify(jobDbDao).createJob(job);
    }

    @Test
    void startJob_ValidJob_CallsDaoMethod() {
        // Act
        jobService.startJob(OWNER, JOB_ID);

        // Assert
        verify(jobDbDao).startJob(OWNER, JOB_ID);
    }

    @Test
    void editJob_ValidJob_CallsDaoMethod() {
        // Arrange
        Job job = new Job();

        // Act
        jobService.editJob(job);

        // Assert
        verify(jobDbDao).editJob(job);
    }

    @Test
    void deleteJob_ValidJob_CallsDaoMethod() {
        // Act
        jobService.deleteJob(OWNER, JOB_ID);

        // Assert
        verify(jobDbDao).deleteJob(OWNER, JOB_ID);
    }
}