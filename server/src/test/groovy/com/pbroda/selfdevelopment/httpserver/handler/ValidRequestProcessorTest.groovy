package com.pbroda.selfdevelopment.httpserver.handler

import com.pbroda.selfdevelopment.httpserver.db.DbConnector
import com.pbroda.selfdevelopment.httpserver.restmodel.Request
import com.pbroda.selfdevelopment.httpserver.restmodel.Response
import spock.lang.Specification

import java.sql.SQLException

import static com.pbroda.selfdevelopment.httpserver.handler.HttpConstants.INTERNAL_SERVER_ERROR_CODE
import static com.pbroda.selfdevelopment.httpserver.handler.HttpConstants.INTERNAL_SERVER_ERROR_INFO

class ValidRequestProcessorTest extends Specification {

    def dbConnector = Mock(DbConnector.class)

    def exchangeProcessor = Mock(ExchangeProcessor.class)
    def request = Mock(Request.class)

    def validRequestProcessor

    def setup() {
        validRequestProcessor = new ValidRequestProcessor(dbConnector)
    }

    def 'should set HTTP response with json payload when request to the database is made'() {
        given:
        def response = Mock(Response.class)
        dbConnector.process(_) >> response

        when:
        validRequestProcessor.handle(exchangeProcessor, request)

        then:
        1 * exchangeProcessor.setResponse(response)
    }

    def 'should set HTTP error response when an exception related to the database is thrown'() {
        given:
        dbConnector.process(_) >> { throw new SQLException() }

        when:
        validRequestProcessor.handle(exchangeProcessor, request)

        then:
        1 * exchangeProcessor.sendResponseHeaders(INTERNAL_SERVER_ERROR_CODE, INTERNAL_SERVER_ERROR_INFO.length())
        1 * exchangeProcessor.setResponseBody(INTERNAL_SERVER_ERROR_INFO)
    }

}
