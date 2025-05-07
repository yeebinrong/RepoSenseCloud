package com.hamburger.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.hamburger.user.middleware.JwtAuthorization;

class UserServiceApplicationTest {

    private final UserServiceApplication application = new UserServiceApplication();

    @Test
    void getServiceName_ReturnsCorrectName() {
        assertEquals("User Service", UserServiceApplication.getServiceName());
    }

    @Test
    void jwtAuthorization_RegistersCorrectUrlPattern() {
        // Act
        FilterRegistrationBean<JwtAuthorization> registrationBean = application.JwtAuthorization();

        // Assert
        assertNotNull(registrationBean);
        assertTrue(registrationBean.getUrlPatterns().contains("/api/user/*"));
        assertNotNull(registrationBean.getFilter());
    }

    // @Test
    // void corsConfigurer_ConfiguresCorrectCorsSettings() {
    //     // Arrange
    //     WebMvcConfigurer configurer = application.corsConfigurer();
    //     CorsRegistry registry = new CorsRegistry();

    //     // Act
    //     configurer.addCorsMappings(registry);

    //     // Assert - Using reflection to verify the configuration
    //     try {
    //         var field = registry.getClass().getDeclaredField("registrations");
    //         field.setAccessible(true);
    //         var registrations = field.get(registry);
    //         assertNotNull(registrations);
            
    //         // Convert to string to check configuration without exposing internal API
    //         String config = registrations.toString();
    //         assertTrue(config.contains("/**"));
    //         assertTrue(config.contains("allowedOriginPatterns='*'"));
    //         assertTrue(config.contains("allowCredentials='true'"));
    //     } catch (Exception e) {
    //         fail("Failed to verify CORS configuration: " + e.getMessage());
    //     }
    // }

    // @Test
    // void main_StartsApplicationWithCorrectProperties() {
    //     // Using try-catch to avoid system exit
    //     try {
    //         String[] args = new String[]{};
    //         UserServiceApplication.main(args);

    //         // Note: We can't easily verify SpringApplication.run() results
    //         // in a unit test, but we can verify it doesn't throw exceptions
    //     } catch (Exception e) {
    //         fail("Application failed to start: " + e.getMessage());
    //     }
    // }
}