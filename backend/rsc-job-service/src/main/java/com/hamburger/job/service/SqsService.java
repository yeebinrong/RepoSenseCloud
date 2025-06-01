package com.hamburger.job.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
public class SqsService {
    private final SqsClient sqsClient;
    private final String queueUrl = "https://sqs.ap-southeast-1.amazonaws.com/904233118848/rsc-sqs-queue-localhost.fifo";

    @Autowired
    public SqsService() {
        this.sqsClient = SqsClient.builder()
            .region(Region.AP_SOUTHEAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    public void sendMessage(String jobId, String messageBody) {
        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .messageGroupId("rsc-batch-localhost") // FIFO queue requires a message group ID
                .messageDeduplicationId(jobId + "_" + System.currentTimeMillis()) // Unique ID for deduplication
                .delaySeconds(0)
                .build();

        SendMessageResponse response = sqsClient.sendMessage(request);
        System.out.println("Message sent with ID: " + response.messageId());
    }
}
