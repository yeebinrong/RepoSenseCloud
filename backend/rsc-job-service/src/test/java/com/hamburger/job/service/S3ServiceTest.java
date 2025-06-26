package com.hamburger.job.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class S3ServiceTest {
    
    @Mock
    private S3Presigner s3Presigner;
    
    @Mock
    private S3Client s3Client;
    
    private S3Service s3Service;
    
    private final String TEST_BUCKET_NAME = "rsc-reports-localhost";
    private final String TEST_OWNER = "testuser";
    private final String TEST_JOB_ID = "job123";
    private final int TEST_EXPIRY_MINUTES = 15;
    private final String TEST_PRESIGNED_URL = "https://s3-test-url.com/file.zip";
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        s3Service = new S3Service();
        // Replace the real S3 clients with mocks
        ReflectionTestUtils.setField(s3Service, "s3Presigner", s3Presigner);
        ReflectionTestUtils.setField(s3Service, "s3Client", s3Client);
        ReflectionTestUtils.setField(s3Service, "bucketName", TEST_BUCKET_NAME);
    }
    
    @Test
    void generateS3PresignedUrl_withExistingFile_returnsPresignedUrl() throws Exception {
        // Arrange
        S3Object s3Object = S3Object.builder().key(TEST_OWNER + "/" + TEST_JOB_ID + "/reposense-report.zip").build();
        List<S3Object> objectList = List.of(s3Object);
        
        ListObjectsV2Response listResponse = ListObjectsV2Response.builder()
                .contents(objectList)
                .build();
        
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listResponse);
        
        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        URL mockUrl = new URL(TEST_PRESIGNED_URL);
        when(presignedRequest.url()).thenReturn(mockUrl);
        
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);
        
        // Act
        String result = s3Service.generateS3PresignedUrl(TEST_OWNER, TEST_JOB_ID, TEST_EXPIRY_MINUTES);
        
        // Assert
        assertNotNull(result);
        assertEquals(TEST_PRESIGNED_URL, result);
        verify(s3Client).listObjectsV2(any(ListObjectsV2Request.class));
        verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
    }
    
    @Test
    void generateS3PresignedUrl_withNoFiles_returnsNull() {
        // Arrange
        ListObjectsV2Response emptyListResponse = ListObjectsV2Response.builder()
                .contents(new ArrayList<>())
                .build();
        
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(emptyListResponse);
        
        // Act
        String result = s3Service.generateS3PresignedUrl(TEST_OWNER, TEST_JOB_ID, TEST_EXPIRY_MINUTES);
        
        // Assert
        assertNull(result);
        verify(s3Client).listObjectsV2(any(ListObjectsV2Request.class));
        verify(s3Presigner, never()).presignGetObject(any(GetObjectPresignRequest.class));
    }
    
    @Test
    void deleteReport_withExistingFile_deletesFileAndReturnsKey() {
        // Arrange
        String expectedKey = TEST_OWNER + "/" + TEST_JOB_ID + "/reposense-report.zip";
        S3Object s3Object = S3Object.builder().key(expectedKey).build();
        List<S3Object> objectList = List.of(s3Object);
        
        ListObjectsV2Response listResponse = ListObjectsV2Response.builder()
                .contents(objectList)
                .build();
        
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listResponse);
        when(s3Client.deleteObject(any(Consumer.class))).thenReturn(DeleteObjectResponse.builder().build());
        
        // Act
        String result = s3Service.deleteReport(TEST_OWNER, TEST_JOB_ID);
        
        // Assert
        assertNotNull(result);
        assertEquals(expectedKey, result);
        verify(s3Client).listObjectsV2(any(ListObjectsV2Request.class));
        verify(s3Client).deleteObject(any(Consumer.class));
    }
    
    @Test
    void deleteReport_withNoFiles_returnsNull() {
        // Arrange
        ListObjectsV2Response emptyListResponse = ListObjectsV2Response.builder()
                .contents(new ArrayList<>())
                .build();
        
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(emptyListResponse);
        
        // Act
        String result = s3Service.deleteReport(TEST_OWNER, TEST_JOB_ID);
        
        // Assert
        assertNull(result);
        verify(s3Client).listObjectsV2(any(ListObjectsV2Request.class));
        verify(s3Client, never()).deleteObject(any(Consumer.class));
    }
    
    @Test
    void generateS3PresignedUrl_whenExceptionThrown_propagatesException() {
        // Arrange
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenThrow(new RuntimeException("Test exception"));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            s3Service.generateS3PresignedUrl(TEST_OWNER, TEST_JOB_ID, TEST_EXPIRY_MINUTES);
        });
    }
    
    @Test
    void deleteReport_whenExceptionThrown_propagatesException() {
        // Arrange
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenThrow(new RuntimeException("Test exception"));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            s3Service.deleteReport(TEST_OWNER, TEST_JOB_ID);
        });
    }
}