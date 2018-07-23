package com.pbroda.selfdevelopment.httpserver.restmodel

import spock.lang.Specification

import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestType.TRANSFER
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus.SUCCESS
import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_1
import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_2
import static com.pbroda.selfdevelopment.httpserver.TestConstants.BALANCE_1
import static com.pbroda.selfdevelopment.httpserver.TestConstants.TEST_AMOUNT
import static com.pbroda.selfdevelopment.httpserver.TestConstants.TEST_ID
import static com.pbroda.selfdevelopment.httpserver.TestConstants.TEST_TIMESTAMP

class RequestUtilsTest extends Specification {

    def 'should serialize and deserialize request' () {
        given:
        Request request =
                Request.builder()
                        .id(TEST_ID)
                        .timestamp(TEST_TIMESTAMP)
                        .request(TRANSFER)
                        .from(ACCOUNT_1)
                        .to(ACCOUNT_2)
                        .amount(TEST_AMOUNT)
                        .build()


        expect:
        String requestString = RequestUtils.toJson(request)
        Request deserializedRequest = RequestUtils.fromJson(requestString, Request.class)

        deserializedRequest.equals(request)
    }

    def 'should serialize and deserialize response' () {
        given:
        Response response =
                Response.builder()
                        .id(TEST_ID)
                        .status(SUCCESS)
                        .balance(BALANCE_1)
                        .build()

        expect:
        String responseString = RequestUtils.toJson(response)
        Response deserializedResponse = RequestUtils.fromJson(responseString, Response.class)

        deserializedResponse.equals(response)
    }

}
