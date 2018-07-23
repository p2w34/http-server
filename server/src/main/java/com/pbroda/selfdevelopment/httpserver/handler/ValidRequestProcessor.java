package com.pbroda.selfdevelopment.httpserver.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.pbroda.selfdevelopment.httpserver.db.DbConnector;
import com.pbroda.selfdevelopment.httpserver.restmodel.Request;
import com.pbroda.selfdevelopment.httpserver.restmodel.Response;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.sql.SQLException;

import static com.pbroda.selfdevelopment.httpserver.handler.HttpConstants.INTERNAL_SERVER_ERROR_CODE;
import static com.pbroda.selfdevelopment.httpserver.handler.HttpConstants.INTERNAL_SERVER_ERROR_INFO;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class ValidRequestProcessor {

    private final DbConnector dbConnector;

    void handle(ExchangeProcessor exchangeProcessor, Request request) throws IOException {

        Response response = null;

        try {
            response = dbConnector.process(request);
        } catch (SQLException ex) {
            exchangeProcessor.sendResponseHeaders(INTERNAL_SERVER_ERROR_CODE, INTERNAL_SERVER_ERROR_INFO.length());
            exchangeProcessor.setResponseBody(INTERNAL_SERVER_ERROR_INFO);
            return;
        }

        exchangeProcessor.setResponse(response);
    }

}
