package com.hamburger.job.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.time.Duration;

@Service
public class S3Service {
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final String bucketName = "rsc-reports-localhost";

    public S3Service() {
        this.s3Presigner = S3Presigner.builder()
                .region(Region.AP_SOUTHEAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        this.s3Client = S3Client.builder()
                .region(Region.AP_SOUTHEAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public String generateS3PresignedUrl(String owner, String jobId, int expiryMinutes) {
        String prefix = owner + "/" + jobId + "/";
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .maxKeys(1)
                .build();
        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
        if (listResponse.contents().isEmpty()) {
            return null;
        }
        String key = prefix + "reposense-report.zip";
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expiryMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }
}
