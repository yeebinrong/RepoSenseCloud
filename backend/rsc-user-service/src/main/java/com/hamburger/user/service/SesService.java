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
        String htmlContent = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 24px;">
                <div style="max-width: 480px; margin: auto; background: #fff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); padding: 32px;">
                    <h2 style="color: #2d3748;">Reset Your RepoSenseCloud Password</h2>
                    <p style="color: #4a5568;">Hello,</p>
                    <p style="color: #4a5568;">We received a request to reset your password. Click the button below to set a new password. If you did not request this, you can safely ignore this email.</p>
                    <div style="text-align: center; margin: 32px 0;">
                        <a href="%s" style="background: #3182ce; color: #fff; padding: 12px 24px; border-radius: 4px; text-decoration: none; font-weight: bold;">Reset Password</a>
                    </div>
                    <p style="color: #a0aec0; font-size: 12px;">This link will expire in 15 minutes.</p>
                    <p style="color: #a0aec0; font-size: 12px;">If you have any questions, please contact support at <a href="mailto:support@hamb-urger.com">support@hamb-urger.com</a>.</p>
                </div>
            </body>
            </html>
        """, resetLink);
        Content htmlBody = Content.builder()
                .data(htmlContent)
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
