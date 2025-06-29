package com.hamburger.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SesServiceTest {
    @Mock
    private SesClient sesClient;

    @InjectMocks
    private SesService sesService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        sesService = new SesService();
        
        java.lang.reflect.Field field = SesService.class.getDeclaredField("sesClient");
        field.setAccessible(true);
        field.set(sesService, sesClient);
    }

    @Test
    void testSendResetPasswordEmail() {
        String recipient = "name@email.com";
        String resetLink = System.getenv("FRONTEND_ORIGIN") + "/reset?email=" + recipient + "&token=valid_token";
        SendEmailResponse mockResponse = SendEmailResponse.builder().messageId("msg").build();
        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(mockResponse);

        sesService.sendResetPasswordEmail(recipient, resetLink);

        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient, times(1)).sendEmail(captor.capture());
        SendEmailRequest request = captor.getValue();
        assertEquals("no-reply@hamb-urger.com", request.source());
        assertTrue(request.destination().toAddresses().contains(recipient));
        assertEquals("RepoSenseCloud Password Reset Request", request.message().subject().data());
        assertTrue(request.message().body().html().data().contains(resetLink));
    }
}
