package com.hamburger.company.reposensecloud.common.handler;

import java.util.Map;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class AbstractApiGatewayHandler implements RequestHandler<Map<String, String>, String> {

    @Override
    public String handleRequest(Map<String, String> event, Context context) {
        log.info("AbstractApiGatewayHandler received request! 4");

        // Perform authentication
        if (!authenticate(event, context)) {
            log.warn("Authentication failed for request!");
            return "Unauthorized";
        }

        // Execute the handler logic
        return performRequest(event, context);
    }

    // Abstract method for the handler logic
    public abstract String performRequest(Map<String, String> event, Context context);

    // Method to handle authentication
    protected boolean authenticate(Map<String, String> event, Context context) {
        log.info("Authentication not yet implemented and is bypassed.");
        return true;
    }
}