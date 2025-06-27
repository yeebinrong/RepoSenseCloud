package com.hamburger.user.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Body;

@Service
public class SesService {
    private final SesClient sesClient;
    private final String senderEmail = "no-reply@hamb-urger.com";

    public SesService() {
        this.sesClient = SesClient.builder()
                .region(Region.AP_SOUTHEAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public void sendResetPasswordEmail(String recipientEmail, String resetLink) {
        Destination destination = Destination.builder()
                .toAddresses(recipientEmail)
                .build();

        Content subject = Content.builder()
                .data("RepoSenseCloud Password Reset Request")
                .build();
        Content htmlBody = Content.builder()
                .data("<p>Click <a href='" + resetLink + "'>here</a> to reset your password.</p>")
                .build();
        Body body = Body.builder()
                .html(htmlBody)
                .build();

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(destination)
                .message(m -> m.subject(subject).body(body))
                .source(senderEmail)
                .build();

        SendEmailResponse response = sesClient.sendEmail(emailRequest);
        System.out.println("Reset password email sent with ID: " + response.messageId());
    }
}
