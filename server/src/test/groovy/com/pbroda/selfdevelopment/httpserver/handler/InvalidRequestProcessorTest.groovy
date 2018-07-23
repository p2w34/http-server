package com.pbroda.selfdevelopment.httpserver.handler

import spock.lang.Specification

import static com.pbroda.selfdevelopment.httpserver.handler.HttpConstants.INVALID_REQUEST_FORMAT_ERROR_CODE
import static com.pbroda.selfdevelopment.httpserver.handler.HttpConstants.INVALID_REQUEST_FORMAT_ERROR_INFO


class InvalidRequestProcessorTest extends Specification {

    def 'should set HTTP error response'() {
        given:
        def exchangeProcessor = Mock(ExchangeProcessor.class)
        def invalidRequestProcessor = new InvalidRequestProcessor()

        when:
        invalidRequestProcessor.handle(exchangeProcessor)

        then:
        1 * exchangeProcessor.sendResponseHeaders(INVALID_REQUEST_FORMAT_ERROR_CODE, INVALID_REQUEST_FORMAT_ERROR_INFO.length())
        1 * exchangeProcessor.setResponseBody(INVALID_REQUEST_FORMAT_ERROR_INFO)
    }

}
