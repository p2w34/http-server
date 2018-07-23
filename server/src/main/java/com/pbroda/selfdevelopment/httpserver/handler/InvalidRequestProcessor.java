package com.pbroda.selfdevelopment.httpserver.handler;

import com.google.inject.Singleton;
import lombok.NoArgsConstructor;

import java.io.IOException;

import static com.pbroda.selfdevelopment.httpserver.handler.HttpConstants.INVALID_REQUEST_FORMAT_ERROR_CODE;
import static com.pbroda.selfdevelopment.httpserver.handler.HttpConstants.INVALID_REQUEST_FORMAT_ERROR_INFO;

@Singleton
@NoArgsConstructor
public class InvalidRequestProcessor {

    void handle(ExchangeProcessor exchangeProcessor) throws IOException {
        exchangeProcessor.sendResponseHeaders(INVALID_REQUEST_FORMAT_ERROR_CODE, INVALID_REQUEST_FORMAT_ERROR_INFO.length());
        exchangeProcessor.setResponseBody(INVALID_REQUEST_FORMAT_ERROR_INFO);
    }

}
