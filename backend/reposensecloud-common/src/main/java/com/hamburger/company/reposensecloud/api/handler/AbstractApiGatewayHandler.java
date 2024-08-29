package com.hamburger.company.reposensecloud.common.handler;

import java.util.Map;

@Log4j2
public abstract class AbstractApiGatewayHandler implements RequestHandler<Map<String, String>, String> {
    public String handleRequest(Map<String, String> event, Context context) {
        log.info("AbstractApiGatewayHandler received request: {}", event);
        return performRequest(event, context);
    }

    public abstract String performRequest(Map<String, String> event, Context context);
}