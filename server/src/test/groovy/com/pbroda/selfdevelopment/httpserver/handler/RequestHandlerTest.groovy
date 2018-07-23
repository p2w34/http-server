package com.pbroda.selfdevelopment.httpserver.handler

import com.pbroda.selfdevelopment.httpserver.restmodel.Request
import com.sun.net.httpserver.HttpExchange
import spock.lang.Specification

import static java.util.Optional.ofNullable

class RequestHandlerTest extends Specification {

    def validRequestProcessor
    def invalidRequestProcessor

    def exchange = Stub(HttpExchange.class)

    def setup() {
        validRequestProcessor = Mock(ValidRequestProcessor.class)
        invalidRequestProcessor = Mock(InvalidRequestProcessor.class)
    }

    def 'should handle valid request'() {
        given:
        def validRequest = ofNullable(Stub(Request.class))

        def exchangeProcessor = Stub(ExchangeProcessor.class)
        exchangeProcessor.getRequestIfValid() >> validRequest

        def exchangeProcessorFactory = Stub(ExchangeProcessorFactory.class)
        exchangeProcessorFactory.get(exchange) >> exchangeProcessor

        def requestHandler = new RequestHandler(exchangeProcessorFactory, validRequestProcessor, invalidRequestProcessor)

        when:
        requestHandler.handle(exchange)

        then:
        1 * validRequestProcessor.handle(_ as ExchangeProcessor, _ as Request)
        0 * invalidRequestProcessor.handle(_)
    }

    def 'should handle invalid request'() {
        given:
        def invalidRequest = new Optional<Request>()

        def exchangeProcessor = Stub(ExchangeProcessor.class)
        exchangeProcessor.getRequestIfValid() >> invalidRequest

        def exchangeProcessorFactory = Stub(ExchangeProcessorFactory.class)
        exchangeProcessorFactory.get(exchange) >> exchangeProcessor

        def requestHandler = new RequestHandler(exchangeProcessorFactory, validRequestProcessor, invalidRequestProcessor)

        when:
        requestHandler.handle(exchange)

        then:
        0 * validRequestProcessor.handle(_ , _)
        1 * invalidRequestProcessor.handle(_ as ExchangeProcessor)
    }

}
