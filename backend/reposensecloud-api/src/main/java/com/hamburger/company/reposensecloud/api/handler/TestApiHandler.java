package com.hamburger.company.reposensecloud.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;

@Log4j2
public class TestApiHandler extends AbstractApiGatewayHandler {
    @Override
    public String performRequest(Map<String, String> event, Context context) {
        log.info("TestApiHandler received request: {}", event);
        return "Hello, Serverless Framework v4!";
    }
}