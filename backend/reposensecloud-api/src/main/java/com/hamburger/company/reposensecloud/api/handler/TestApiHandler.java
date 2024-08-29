package com.hamburger.company.reposensecloud.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import com.hamburger.company.reposensecloud.common.handler.AbstractApiGatewayHandler;

@Log4j2
public class TestApiHandler extends AbstractApiGatewayHandler {
    @Override
    public String performRequest(Map<String, String> event, Context context) {
        log.info("TestApiHandler received request!");
        return "Hello, Serverless Framework!";
    }
}