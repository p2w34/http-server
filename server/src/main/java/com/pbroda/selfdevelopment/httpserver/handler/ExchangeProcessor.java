package com.pbroda.selfdevelopment.httpserver.handler;

import com.pbroda.selfdevelopment.httpserver.restmodel.Request;
import com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus;
import com.pbroda.selfdevelopment.httpserver.restmodel.RequestUtils;
import com.pbroda.selfdevelopment.httpserver.restmodel.Response;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.pbroda.selfdevelopment.httpserver.handler.HttpConstants.HTTP_FORBIDDEN;
import static com.pbroda.selfdevelopment.httpserver.handler.HttpConstants.HTTP_OK;
import static com.pbroda.selfdevelopment.httpserver.handler.HttpConstants.GET;
import static com.pbroda.selfdevelopment.httpserver.handler.HttpConstants.POST;
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestType.BALANCE;
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestType.TRANSFER;
import static java.util.Optional.ofNullable;

public class ExchangeProcessor {

    private HttpExchange exchange;
    private Optional<Request> request;
    private Response response;

    public ExchangeProcessor(HttpExchange exchange) {
        this.exchange = exchange;
    }

    Optional<Request> getRequestIfValid() throws IOException {
        String body = getRequestBody();
        request = ofNullable(RequestUtils.fromJson(body, Request.class));

        if (isValidRestRequest()) {
            return request;
        }

        return Optional.empty();
    }

    void setResponse(Response response) throws IOException {
        this.response = response;
        String responseString = RequestUtils.toJson(response);
        sendResponseHeaders(getHttpResponseCode(), responseString.length());
        setResponseBody(responseString);
    }

    void sendResponseHeaders(int responseCode, long responseLength) throws IOException {
        exchange.sendResponseHeaders(responseCode, responseLength);
    }

    void setResponseBody(String src) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(exchange.getResponseBody()))) {
            bw.write(src);
        }
    }

    private int getHttpResponseCode() {
        int httpResponseCode = HTTP_FORBIDDEN;
        if (response.getStatus() == RequestStatus.SUCCESS) {
            httpResponseCode = HTTP_OK;
        }
        return httpResponseCode;
    }

    private String getRequestBody() throws IOException {
        String result;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            result = br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        return result;
    }

    private boolean isValidRestRequest() {
        String requestMethod = exchange.getRequestMethod().toUpperCase();

        if (requestMethod.equals(GET) && request.get().getRequest() == BALANCE) {
            return true;
        }

        if (requestMethod.equals(POST) && request.get().getRequest() == TRANSFER) {
            return true;
        }

        return false;
    }

}
