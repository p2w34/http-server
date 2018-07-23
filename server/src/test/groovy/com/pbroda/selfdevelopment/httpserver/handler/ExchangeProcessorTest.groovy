package com.pbroda.selfdevelopment.httpserver.handler

import com.pbroda.selfdevelopment.httpserver.restmodel.Request
import com.pbroda.selfdevelopment.httpserver.restmodel.RequestUtils
import com.pbroda.selfdevelopment.httpserver.restmodel.Response
import com.sun.net.httpserver.HttpExchange
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.StandardCharsets

import static com.pbroda.selfdevelopment.httpserver.handler.HttpConstants.HTTP_FORBIDDEN
import static com.pbroda.selfdevelopment.httpserver.handler.HttpConstants.HTTP_OK
import static com.pbroda.selfdevelopment.httpserver.handler.HttpConstants.GET
import static com.pbroda.selfdevelopment.httpserver.handler.HttpConstants.POST
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestType.BALANCE
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestType.TRANSFER
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus.FAILURE_NOT_ENOUGH_FUNDS
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus.SUCCESS
import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_1
import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_2
import static com.pbroda.selfdevelopment.httpserver.TestConstants.TEST_AMOUNT
import static java.util.Optional.ofNullable

class ExchangeProcessorTest extends Specification {

    @Shared
    def balanceRequest =
            ofNullable(Request.builder()
                    .request(BALANCE)
                    .from(ACCOUNT_1)
                    .to(ACCOUNT_2)
                    .amount(TEST_AMOUNT)
                    .build())
    @Shared
    def transferRequest =
            ofNullable(Request.builder()
                    .request(TRANSFER)
                    .from(ACCOUNT_1)
                    .to(ACCOUNT_2)
                    .amount(TEST_AMOUNT)
                    .build())

    @Shared
    def emptyRequest = new Optional<Request>()

    @Shared
    def successfulResponse = Response.builder().status(SUCCESS).build()

    @Shared
    def failedResponse = Response.builder().status(FAILURE_NOT_ENOUGH_FUNDS).build()


    def 'should get valid request otherwise empty request'() {
        given:
        def exchange = Stub(HttpExchange.class)

        when:
        exchange.getRequestMethod() >> {return requestMethod}
        exchange.getRequestBody() >> {return new ByteArrayInputStream(RequestUtils.toJson(request.get())
                                                                                  .getBytes(StandardCharsets.UTF_8))}

        then:
        def exchangeProcessor = new ExchangeProcessor(exchange)
        def result = exchangeProcessor.getRequestIfValid()
        result.equals(expectedResult)
        
        where:
        requestMethod | request         || expectedResult
        GET           | balanceRequest  || balanceRequest
        POST          | transferRequest || transferRequest
        GET           | transferRequest || emptyRequest
        POST          | balanceRequest  || emptyRequest
        'PUT'         | balanceRequest  || emptyRequest
        'PUT'         | balanceRequest  || emptyRequest
    }

    def 'should set response headers and body'() {
        given:
        def exchange = Mock(HttpExchange.class)
        exchange.getResponseBody() >> {new ByteArrayOutputStream()}

        def exchangeProcessor = Spy(ExchangeProcessor.class, constructorArgs: [exchange])

        String responseBody = RequestUtils.toJson(response)
        long expectedResponseLength = responseBody.length()

        when:
        exchangeProcessor.setResponse(response)

        then:
        1 * exchange.sendResponseHeaders(expectedCode, expectedResponseLength)
        1 * exchangeProcessor.setResponseBody(responseBody)

        where:
        response           || expectedCode
        successfulResponse || HTTP_OK
        failedResponse     || HTTP_FORBIDDEN
    }

}
