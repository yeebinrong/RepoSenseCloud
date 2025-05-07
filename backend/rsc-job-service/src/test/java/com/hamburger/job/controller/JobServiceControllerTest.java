package com.hamburger.job.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hamburger.job.models.Job;
import com.hamburger.job.models.exceptions.StartJobException;
import com.hamburger.job.service.JobService;
import com.hamburger.job.util.JobUserAuth;
import com.hamburger.job.util.JwtHelper;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class JobServiceControllerTest {
    @Mock
    private JobService jobService;

    @Mock
    private JobUserAuth jobUserAuth;

    @Mock
    private JwtHelper jwtHelper;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private JobServiceController jobServiceController;

    private static final String MOCK_TOKEN = "mock-token";
    private static final String MOCK_OWNER = "test-user";
    private static final String MOCK_JOB_ID = "test-123";
    
    @BeforeEach
    void setUp() {
        lenient().when(jwtHelper.extractJwtFromRequest(request)).thenReturn(MOCK_TOKEN);
        lenient().when(jobUserAuth.authorizeAction(MOCK_TOKEN)).thenReturn(ResponseEntity.ok(MOCK_OWNER));
    }

    @Test
    void getJobsByPage_ValidRequest_ReturnsJobs() {
        // Arrange
        int page = 1;
        int limit = 10;
        List<Job> expectedJobs = Arrays.asList(new Job(), new Job());
        when(jobService.getJobsByPage(MOCK_OWNER, page, limit)).thenReturn(expectedJobs);

        // Act
        ResponseEntity<List<Job>> response = jobServiceController.getJobsByPage(page, limit, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedJobs, response.getBody());
        verify(jobService).getJobsByPage(MOCK_OWNER, page, limit);
    }

    @Test
    void getJobById_ExistingJob_ReturnsJob() {
        // Arrange
        Job expectedJob = new Job();
        when(jobService.getJobsById(MOCK_OWNER, MOCK_JOB_ID)).thenReturn(Optional.of(expectedJob));

        // Act
        ResponseEntity<Optional<Job>> response = jobServiceController.getJobById(MOCK_JOB_ID, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Optional.of(expectedJob), response.getBody());
    }

    @Test
    void getAllJobs_ValidRequest_ReturnsAllJobs() {
        // Arrange
        List<Job> expectedJobs = Arrays.asList(new Job(), new Job());
        when(jobService.getAllJobs(MOCK_OWNER)).thenReturn(Optional.of(expectedJobs));

        // Act
        ResponseEntity<Optional<List<Job>>> response = jobServiceController.getAllJobs(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Optional.of(expectedJobs), response.getBody());
    }

    @Test
    void createJob_ValidJob_CreatesJob() {
        // Arrange
        Job job = new Job();

        // Act
        ResponseEntity<Void> response = jobServiceController.createJob(job, request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(jobService).createJob(job);
    }

    @Test
    void startJob_ValidJob_StartsJob() throws StartJobException {
        // Arrange
        doNothing().when(jobService).startJob(MOCK_OWNER, MOCK_JOB_ID);

        // Act
        ResponseEntity<Void> response = jobServiceController.startJob(MOCK_JOB_ID, request);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(jobService).startJob(MOCK_OWNER, MOCK_JOB_ID);
    }

    @Test
    void startJob_ThrowsStartJobException_ReturnsBadRequest() throws StartJobException {
        // Arrange
        doThrow(new StartJobException("Error starting job")).when(jobService).startJob(MOCK_OWNER, MOCK_JOB_ID);

        // Act
        ResponseEntity<Void> response = jobServiceController.startJob(MOCK_JOB_ID, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateJob_ValidJob_UpdatesJob() {
        // Arrange
        Job job = new Job();
        job.setJobId(MOCK_JOB_ID);

        // Act
        ResponseEntity<Void> response = jobServiceController.updateJob(job, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(jobService).editJob(job);
    }

    @Test
    void deleteJob_ExistingJob_DeletesJob() {
        // Arrange
        doNothing().when(jobService).deleteJob(MOCK_OWNER, MOCK_JOB_ID);

        // Act
        ResponseEntity<Void> response = jobServiceController.deleteJob(MOCK_JOB_ID, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(jobService).deleteJob(MOCK_OWNER, MOCK_JOB_ID);
    }

    @Test
    void unauthorized_ReturnsUnauthorized() {
        // Arrange
        when(jobUserAuth.authorizeAction(MOCK_TOKEN)).thenReturn(ResponseEntity.ok(null));

        // Act
        ResponseEntity<Optional<List<Job>>> response = jobServiceController.getAllJobs(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getJobsByKeyword_ValidKeyword_ReturnsJobs() {
        // Arrange
        String keyword = "test";
        List<Job> expectedJobs = Arrays.asList(new Job(), new Job());
        when(jobService.getJobsByKeyword(MOCK_OWNER, keyword)).thenReturn(Optional.of(expectedJobs));

        // Act
        ResponseEntity<Optional<List<Job>>> response = jobServiceController.getJobsByKeyword(keyword, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Optional.of(expectedJobs), response.getBody());
        verify(jobService).getJobsByKeyword(MOCK_OWNER, keyword);
    }

    @Test
    void getReport_ValidJobId_ReturnsReportUrl() {
        // Arrange
        String expectedUrl = "https://example.com/report.pdf";
        when(jobService.getReport(MOCK_JOB_ID)).thenReturn(expectedUrl);

        // Act
        ResponseEntity<String> response = jobServiceController.getReport(MOCK_JOB_ID, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedUrl, response.getBody());
        verify(jobService).getReport(MOCK_JOB_ID);
    }

    @Test
    void saveJob_ValidRequest_SavesJobs() {
        // Arrange
        doNothing().when(jobService).saveJob();

        // Act
        ResponseEntity<Void> response = jobServiceController.saveJob();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(jobService).saveJob();
    }

    @Test
    void getJobsByPage_ThrowsException_ReturnsInternalServerError() {
        // Arrange
        int page = 1;
        int limit = 10;
        when(jobService.getJobsByPage(MOCK_OWNER, page, limit)).thenThrow(new RuntimeException());

        // Act
        ResponseEntity<List<Job>> response = jobServiceController.getJobsByPage(page, limit, request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void createJob_ThrowsException_ReturnsInternalServerError() {
        // Arrange
        Job job = new Job();
        doThrow(new RuntimeException()).when(jobService).createJob(job);

        // Act
        ResponseEntity<Void> response = jobServiceController.createJob(job, request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void startJob_ThrowsGenericException_ReturnsInternalServerError() throws StartJobException {
        // Arrange
        doThrow(new RuntimeException()).when(jobService).startJob(MOCK_OWNER, MOCK_JOB_ID);

        // Act
        ResponseEntity<Void> response = jobServiceController.startJob(MOCK_JOB_ID, request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void jwtExtraction_ThrowsException_ReturnsInternalServerError() {
        // Arrange
        when(jwtHelper.extractJwtFromRequest(request)).thenThrow(new RuntimeException());

        // Act
        ResponseEntity<Optional<List<Job>>> response = jobServiceController.getAllJobs(request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}