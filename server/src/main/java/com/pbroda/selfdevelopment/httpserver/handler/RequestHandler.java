package com.pbroda.selfdevelopment.httpserver.handler;

import com.google.inject.Inject;
import com.pbroda.selfdevelopment.httpserver.restmodel.Request;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor(onConstructor_={@Inject})
public class RequestHandler implements HttpHandler {

    private final ExchangeProcessorFactory exchangeProcessorFactory;
    private final ValidRequestProcessor validRequestProcessor;
    private final InvalidRequestProcessor invalidRequestProcessor;

    public void handle(HttpExchange exchange) throws IOException {
        ExchangeProcessor exchangeProcessor = exchangeProcessorFactory.get(exchange);

        Optional<Request> request = exchangeProcessor.getRequestIfValid();

        if (request.isPresent()) {
            validRequestProcessor.handle(exchangeProcessor, request.get());
            return;
        }

        invalidRequestProcessor.handle(exchangeProcessor);
    }

}