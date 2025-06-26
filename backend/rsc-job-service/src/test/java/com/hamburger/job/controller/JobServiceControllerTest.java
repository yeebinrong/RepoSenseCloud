package com.hamburger.job.controller;

import com.hamburger.job.models.Job;
import com.hamburger.job.models.exceptions.StartJobException;
import com.hamburger.job.service.JobService;
import com.hamburger.job.service.S3Service;
import com.hamburger.job.util.JobUserAuth;
import com.hamburger.job.util.JwtHelper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class JobServiceControllerTest {

    @Mock
    private JobService jobService;
    @Mock
    private JobUserAuth jobUserAuth;
    @Mock
    private JwtHelper jwtHelper;
    @Mock
    private S3Service s3Service;
    
    private JobServiceController jobServiceController;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jobServiceController = new JobServiceController(jobService, jobUserAuth, jwtHelper, s3Service);
        request = new MockHttpServletRequest();
        
        // Common setup for authentication
        try {
            when(jwtHelper.extractJwtFromRequest(any(HttpServletRequest.class))).thenReturn("mockToken");
            when(jobUserAuth.authorizeAction(anyString())).thenReturn(ResponseEntity.ok("testUser"));
        } catch (Exception e) {
            fail("Mocking setup failed: " + e.getMessage());
        }
    }

    // Test for getJobsByPage endpoint
    @Test
    void getJobsByPage_Success() {
        // Arrange
        List<Job> jobs = new ArrayList<>();
        jobs.add(new Job());
        jobs.add(new Job());
        when(jobService.getJobsByPage(anyString(), anyInt(), anyInt())).thenReturn(jobs);
        
        // Act
        ResponseEntity<List<Job>> response = jobServiceController.getJobsByPage(1, 10, request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(jobService).getJobsByPage("testUser", 1, 10);
    }

    @Test
    void getJobsByPage_Unauthorized() {
        // Arrange
        when(jobUserAuth.authorizeAction(anyString())).thenReturn(ResponseEntity.ok(null));
        
        // Act
        ResponseEntity<List<Job>> response = jobServiceController.getJobsByPage(1, 10, request);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(jobService, never()).getJobsByPage(anyString(), anyInt(), anyInt());
    }

    @Test
    void getJobsByPage_Exception() {
        // Arrange
        when(jobService.getJobsByPage(anyString(), anyInt(), anyInt())).thenThrow(new RuntimeException("Test Exception"));
        
        // Act
        ResponseEntity<List<Job>> response = jobServiceController.getJobsByPage(1, 10, request);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // Test for getJobById endpoint
    @Test
    void getJobById_Success() {
        // Arrange
        Job job = new Job();
        job.setJobId("job123");
        when(jobService.getJobsById(anyString(), anyString())).thenReturn(Optional.of(job));
        
        // Act
        ResponseEntity<Optional<Job>> response = jobServiceController.getJobById("job123", request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isPresent());
        assertEquals("job123", response.getBody().get().getJobId());
        verify(jobService).getJobsById("testUser", "job123");
    }

    @Test
    void getJobById_NotFound() {
        // Arrange
        when(jobService.getJobsById(anyString(), anyString())).thenReturn(Optional.empty());
        
        // Act
        ResponseEntity<Optional<Job>> response = jobServiceController.getJobById("nonExistentJob", request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getJobById_Unauthorized() {
        // Arrange
        when(jobUserAuth.authorizeAction(anyString())).thenReturn(ResponseEntity.ok(null));
        
        // Act
        ResponseEntity<Optional<Job>> response = jobServiceController.getJobById("job123", request);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(jobService, never()).getJobsById(anyString(), anyString());
    }

    @Test
    void getJobById_Exception() {
        // Arrange
        when(jobService.getJobsById(anyString(), anyString())).thenThrow(new RuntimeException("Test Exception"));
        
        // Act
        ResponseEntity<Optional<Job>> response = jobServiceController.getJobById("job123", request);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // Test for getAllJobs endpoint
    @Test
    void getAllJobs_Success() {
        // Arrange
        List<Job> jobs = List.of(new Job(), new Job(), new Job());
        when(jobService.getAllJobs(anyString())).thenReturn(Optional.of(jobs));
        
        // Act
        ResponseEntity<Optional<List<Job>>> response = jobServiceController.getAllJobs(request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isPresent());
        assertEquals(3, response.getBody().get().size());
        verify(jobService).getAllJobs("testUser");
    }

    @Test
    void getAllJobs_NoJobs() {
        // Arrange
        when(jobService.getAllJobs(anyString())).thenReturn(Optional.of(new ArrayList<>()));
        
        // Act
        ResponseEntity<Optional<List<Job>>> response = jobServiceController.getAllJobs(request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isPresent());
        assertEquals(0, response.getBody().get().size());
    }

    @Test
    void getAllJobs_Unauthorized() {
        // Arrange
        when(jobUserAuth.authorizeAction(anyString())).thenReturn(ResponseEntity.ok(null));
        
        // Act
        ResponseEntity<Optional<List<Job>>> response = jobServiceController.getAllJobs(request);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(jobService, never()).getAllJobs(anyString());
    }

    @Test
    void getAllJobs_Exception() {
        // Arrange
        when(jobService.getAllJobs(anyString())).thenThrow(new RuntimeException("Test Exception"));
        
        // Act
        ResponseEntity<Optional<List<Job>>> response = jobServiceController.getAllJobs(request);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // Test for getJobsByKeyword endpoint
    @Test
    void getJobsByKeyword_Success() {
        // Arrange
        List<Job> jobs = List.of(new Job());
        when(jobService.getJobsByKeyword(anyString(), anyString())).thenReturn(Optional.of(jobs));
        
        // Act
        ResponseEntity<Optional<List<Job>>> response = jobServiceController.getJobsByKeyword("test", request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isPresent());
        assertEquals(1, response.getBody().get().size());
        verify(jobService).getJobsByKeyword("testUser", "test");
    }

    @Test
    void getJobsByKeyword_NoResults() {
        // Arrange
        when(jobService.getJobsByKeyword(anyString(), anyString())).thenReturn(Optional.of(new ArrayList<>()));
        
        // Act
        ResponseEntity<Optional<List<Job>>> response = jobServiceController.getJobsByKeyword("nonexistent", request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isPresent());
        assertEquals(0, response.getBody().get().size());
    }

    @Test
    void getJobsByKeyword_Unauthorized() {
        // Arrange
        when(jobUserAuth.authorizeAction(anyString())).thenReturn(ResponseEntity.ok(null));
        
        // Act
        ResponseEntity<Optional<List<Job>>> response = jobServiceController.getJobsByKeyword("test", request);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(jobService, never()).getJobsByKeyword(anyString(), anyString());
    }

    @Test
    void getJobsByKeyword_Exception() {
        // Arrange
        when(jobService.getJobsByKeyword(anyString(), anyString())).thenThrow(new RuntimeException("Test Exception"));
        
        // Act
        ResponseEntity<Optional<List<Job>>> response = jobServiceController.getJobsByKeyword("test", request);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // Test for saveJob endpoint
    @Test
    void saveJob_Success() {
        // Arrange
        doNothing().when(jobService).saveJob();
        
        // Act
        ResponseEntity<Void> response = jobServiceController.saveJob();
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(jobService).saveJob();
    }

    @Test
    void saveJob_Exception() {
        // Arrange
        doThrow(new RuntimeException("Test Exception")).when(jobService).saveJob();
        
        // Act
        ResponseEntity<Void> response = jobServiceController.saveJob();
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // Test for createJob endpoint
    @Test
    void createJob_Success() {
        // Arrange
        Job job = new Job();
        doNothing().when(jobService).createJob(any(Job.class));
        
        // Act
        ResponseEntity<Void> response = jobServiceController.createJob(job, request);
        
        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("testUser", job.getOwner()); // Check owner was set
        verify(jobService).createJob(job);
    }

    @Test
    void createJob_Unauthorized() {
        // Arrange
        when(jobUserAuth.authorizeAction(anyString())).thenReturn(ResponseEntity.ok(null));
        Job job = new Job();
        
        // Act
        ResponseEntity<Void> response = jobServiceController.createJob(job, request);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(jobService, never()).createJob(any(Job.class));
    }

    @Test
    void createJob_Exception() {
        // Arrange
        Job job = new Job();
        doThrow(new RuntimeException("Test Exception")).when(jobService).createJob(any(Job.class));
        
        // Act
        ResponseEntity<Void> response = jobServiceController.createJob(job, request);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // Test for startJob endpoint
    @Test
    void startJob_Success() {
        // Arrange
        doNothing().when(jobService).startJob(anyString(), anyString());
        
        // Act
        ResponseEntity<Void> response = jobServiceController.startJob("job123", request);
        
        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(jobService).startJob("testUser", "job123");
    }

    @Test
    void startJob_Unauthorized() {
        // Arrange
        when(jobUserAuth.authorizeAction(anyString())).thenReturn(ResponseEntity.ok(null));
        
        // Act
        ResponseEntity<Void> response = jobServiceController.startJob("job123", request);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(jobService, never()).startJob(anyString(), anyString());
    }

    @Test
    void startJob_BadRequest() {
        // Arrange
        doThrow(new StartJobException("Cannot start job")).when(jobService).startJob(anyString(), anyString());
        
        // Act
        ResponseEntity<Void> response = jobServiceController.startJob("job123", request);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void startJob_InternalServerError() {
        // Arrange
        doThrow(new RuntimeException("Test Exception")).when(jobService).startJob(anyString(), anyString());
        
        // Act
        ResponseEntity<Void> response = jobServiceController.startJob("job123", request);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // Test for updateJob endpoint
    @Test
    void updateJob_Success() {
        // Arrange
        Job job = new Job();
        job.setJobId("job123");
        doNothing().when(jobService).editJob(any(Job.class));
        
        // Act
        ResponseEntity<Void> response = jobServiceController.updateJob(job, request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("testUser", job.getOwner()); // Check owner was set
        verify(jobService).editJob(job);
    }

    @Test
    void updateJob_Unauthorized() {
        // Arrange
        when(jobUserAuth.authorizeAction(anyString())).thenReturn(ResponseEntity.ok(null));
        Job job = new Job();
        
        // Act
        ResponseEntity<Void> response = jobServiceController.updateJob(job, request);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(jobService, never()).editJob(any(Job.class));
    }

    @Test
    void updateJob_Exception() {
        // Arrange
        Job job = new Job();
        doThrow(new RuntimeException("Test Exception")).when(jobService).editJob(any(Job.class));
        
        // Act
        ResponseEntity<Void> response = jobServiceController.updateJob(job, request);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // Test for deleteJob endpoint
    @Test
    void deleteJob_Success() {
        // Arrange
        doNothing().when(jobService).deleteJob(anyString(), anyString());
        
        // Act
        ResponseEntity<Void> response = jobServiceController.deleteJob("job123", request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(jobService).deleteJob("testUser", "job123");
    }

    @Test
    void deleteJob_Unauthorized() {
        // Arrange
        when(jobUserAuth.authorizeAction(anyString())).thenReturn(ResponseEntity.ok(null));
        
        // Act
        ResponseEntity<Void> response = jobServiceController.deleteJob("job123", request);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(jobService, never()).deleteJob(anyString(), anyString());
    }

    @Test
    void deleteJob_Exception() {
        // Arrange
        doThrow(new RuntimeException("Test Exception")).when(jobService).deleteJob(anyString(), anyString());
        
        // Act
        ResponseEntity<Void> response = jobServiceController.deleteJob("job123", request);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // Test for getS3PresignedUrl endpoint
    @Test
    void getS3PresignedUrl_Success() {
        // Arrange
        when(s3Service.generateS3PresignedUrl(anyString(), anyString(), anyInt())).thenReturn("https://test-url.com");
        
        // Act
        ResponseEntity<String> response = jobServiceController.getS3PresignedUrl("job123", request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("https://test-url.com", response.getBody());
        verify(s3Service).generateS3PresignedUrl("testUser", "job123", 15);
    }

    @Test
    void getS3PresignedUrl_NotFound() {
        // Arrange
        when(s3Service.generateS3PresignedUrl(anyString(), anyString(), anyInt())).thenReturn(null);
        
        // Act
        ResponseEntity<String> response = jobServiceController.getS3PresignedUrl("job123", request);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Folder or report not found", response.getBody());
    }

    @Test
    void getS3PresignedUrl_Unauthorized() {
        // Arrange
        when(jobUserAuth.authorizeAction(anyString())).thenReturn(ResponseEntity.ok(null));
        
        // Act
        ResponseEntity<String> response = jobServiceController.getS3PresignedUrl("job123", request);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(s3Service, never()).generateS3PresignedUrl(anyString(), anyString(), anyInt());
    }

    @Test
    void getS3PresignedUrl_Exception() {
        // Arrange
        when(s3Service.generateS3PresignedUrl(anyString(), anyString(), anyInt())).thenThrow(new RuntimeException("Test Exception"));
        
        // Act
        ResponseEntity<String> response = jobServiceController.getS3PresignedUrl("job123", request);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // Test for JWT extraction exception
    @Test
    void jwtExtractionFails() throws Exception {
        // Arrange
        when(jwtHelper.extractJwtFromRequest(any(HttpServletRequest.class))).thenThrow(new RuntimeException("JWT extraction failed"));
        
        // Act
        ResponseEntity<List<Job>> response = jobServiceController.getJobsByPage(1, 10, request);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(jobService, never()).getJobsByPage(anyString(), anyInt(), anyInt());
    }

    // Test for authentication exception
    @Test
    void authenticationFails() throws Exception {
        // Arrange
        when(jobUserAuth.authorizeAction(anyString())).thenThrow(new RuntimeException("Authentication failed"));
        
        // Act
        ResponseEntity<List<Job>> response = jobServiceController.getJobsByPage(1, 10, request);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(jobService, never()).getJobsByPage(anyString(), anyInt(), anyInt());
    }
}