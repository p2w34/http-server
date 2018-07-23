package com.pbroda.selfdevelopment.httpserver

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.name.Names
import com.pbroda.selfdevelopment.httpserver.db.DatabaseConnectionFactory
import com.pbroda.selfdevelopment.httpserver.db.DbConnector
import com.pbroda.selfdevelopment.httpserver.handler.ExchangeProcessorFactory
import com.pbroda.selfdevelopment.httpserver.handler.InvalidRequestProcessor
import com.pbroda.selfdevelopment.httpserver.handler.RequestHandler
import com.pbroda.selfdevelopment.httpserver.handler.ValidRequestProcessor
import com.pbroda.selfdevelopment.httpserver.restmodel.Request
import com.pbroda.selfdevelopment.httpserver.restmodel.RequestUtils
import com.pbroda.selfdevelopment.httpserver.restmodel.Response
import com.sun.net.httpserver.HttpHandler
import io.restassured.RestAssured
import io.restassured.specification.RequestSender
import org.apache.commons.dbutils.QueryRunner
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_1
import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_2
import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_3
import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_4
import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_5
import static com.pbroda.selfdevelopment.httpserver.TestConstants.NON_EXISTING_ACCOUNT
import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_NUMBER_1
import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_NUMBER_2
import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_NUMBER_3
import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_NUMBER_4
import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_NUMBER_5
import static com.pbroda.selfdevelopment.httpserver.TestConstants.BALANCE_1
import static com.pbroda.selfdevelopment.httpserver.TestConstants.BALANCE_2
import static com.pbroda.selfdevelopment.httpserver.TestConstants.BALANCE_3
import static com.pbroda.selfdevelopment.httpserver.TestConstants.BALANCE_4
import static com.pbroda.selfdevelopment.httpserver.TestConstants.BALANCE_5
import static com.pbroda.selfdevelopment.httpserver.handler.HttpConstants.HTTP_OK
import static com.pbroda.selfdevelopment.httpserver.handler.HttpConstants.HTTP_FORBIDDEN
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus.FAILURE_DEST_ACC_DOES_NOT_EXIST
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus.FAILURE_NOT_ENOUGH_FUNDS
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus.FAILURE_SAME_SRC_AND_DEST_ACC
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus.FAILURE_SRC_ACC_DOES_NOT_EXIST
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus.SUCCESS
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestType.BALANCE
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestType.TRANSFER
import static io.restassured.RestAssured.given


@Stepwise
class HttpServerIntegrationTest extends Specification {

    static class HttpServerApplicationModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(String.class).annotatedWith(Names.named('JDBC URL'))
                    .toInstance('jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1');
            bind(DatabaseConnectionFactory.class);
            bind(DbConnector.class);

            bind(ExchangeProcessorFactory.class);
            bind(ValidRequestProcessor.class);
            bind(InvalidRequestProcessor.class);

            bind(HttpHandler.class).to(RequestHandler.class)
            bind(HttpServerApplication.class)
        }
    }

    @Shared
    def injector

    @Shared
    def httpServerApplication

    def setupSpec() {
        RestAssured.port = Integer.valueOf(8407);
        RestAssured.baseURI = "http://127.0.0.1";

        injector = Guice.createInjector(new HttpServerApplicationModule())
        httpServerApplication = injector.getInstance(HttpServerApplication.class);
    }

    def setup() throws SQLException {
        def statement = "CREATE TABLE ACCOUNTS (ACCOUNT VARCHAR(64) PRIMARY KEY," +
                        "                       BALANCE INT); " +
                        "INSERT INTO ACCOUNTS VALUES ($ACCOUNT_NUMBER_1, $BALANCE_1); " +
                        "INSERT INTO ACCOUNTS VALUES ($ACCOUNT_NUMBER_2, $BALANCE_2); " +
                        "INSERT INTO ACCOUNTS VALUES ($ACCOUNT_NUMBER_3, $BALANCE_3); " +
                        "INSERT INTO ACCOUNTS VALUES ($ACCOUNT_NUMBER_4, $BALANCE_4); " +
                        "INSERT INTO ACCOUNTS VALUES ($ACCOUNT_NUMBER_5, $BALANCE_5);"

        runTestSqlStatement(statement)
    }

    def cleanup() {
        def statement = 'DROP TABLE ACCOUNTS'

        runTestSqlStatement(statement)
    }

    def runTestSqlStatement(String statement) {
        DatabaseConnectionFactory factory = injector.getInstance(DatabaseConnectionFactory.class)
        Connection connection = factory.getConnection()
        QueryRunner runner = new QueryRunner();

        runner.update(connection, statement);
    }

    def 'should respond to single balance request'() {
        given:
        Request balanceRequest =
                Request.builder()
                        .request(BALANCE)
                        .from(fromAccount)
                        .build()

        when:
        def balanceResponse = doGet(balanceRequest)

        then:
        assertResponse(balanceResponse,
                       expectedHttpCode, balanceRequest.id, expectedResponseStatus, expectedBalance)

        where:
        fromAccount          || expectedHttpCode | expectedResponseStatus         | expectedBalance
        ACCOUNT_1            || HTTP_OK          | SUCCESS                        | BALANCE_1
        ACCOUNT_2            || HTTP_OK          | SUCCESS                        | BALANCE_2
        ACCOUNT_3            || HTTP_OK          | SUCCESS                        | BALANCE_3
        NON_EXISTING_ACCOUNT || HTTP_FORBIDDEN   | FAILURE_SRC_ACC_DOES_NOT_EXIST | null
    }

    def 'should respond to single transfer request'() {
        given:
        Request transferRequest =
                Request.builder()
                        .request(TRANSFER)
                        .from(fromAccount)
                        .to(toAccount)
                        .amount(amount)
                        .build()

        when:
        def transferResponse = doPost(transferRequest)

        then:
        assertResponse(transferResponse,
                       expectedHttpCode, transferRequest.id, expectedResponseStatus, expectedBalance)

        where:
        fromAccount          | toAccount            | amount        || expectedHttpCode | expectedResponseStatus          | expectedBalance
        ACCOUNT_1            | ACCOUNT_3            | BALANCE_1     || HTTP_OK          | SUCCESS                         | null
        ACCOUNT_2            | ACCOUNT_3            | BALANCE_2 + 1 || HTTP_FORBIDDEN   | FAILURE_NOT_ENOUGH_FUNDS        | null
        ACCOUNT_3            | ACCOUNT_3            | 1             || HTTP_FORBIDDEN   | FAILURE_SAME_SRC_AND_DEST_ACC   | null
        ACCOUNT_3            | NON_EXISTING_ACCOUNT | 1             || HTTP_FORBIDDEN   | FAILURE_DEST_ACC_DOES_NOT_EXIST | null
        NON_EXISTING_ACCOUNT | ACCOUNT_3            | 1             || HTTP_FORBIDDEN   | FAILURE_SRC_ACC_DOES_NOT_EXIST  | null
    }

    def 'should perform multiple transfer requests'() {
        given:
        def transferRequestId = UUID.randomUUID()

        def amount_4_to_5 = 1
        Request transferRequest_4_to_5 =
                Request.builder()
                        .id(transferRequestId)
                        .request(TRANSFER)
                        .from(ACCOUNT_4)
                        .to(ACCOUNT_5)
                        .amount(amount_4_to_5)
                        .build()

        def amount_5_to_4 = 2
        Request transferRequest_5_to_4 =
                Request.builder()
                        .id(transferRequestId)
                        .request(TRANSFER)
                        .from(ACCOUNT_5)
                        .to(ACCOUNT_4)
                        .amount(amount_5_to_4)
                        .build()

        Request balanceRequest_4 =
                Request.builder()
                        .request(BALANCE)
                        .from(ACCOUNT_4)
                        .build()

        Request balanceRequest_5 =
                Request.builder()
                        .request(BALANCE)
                        .from(ACCOUNT_5)
                        .build()

        def noOfRequests = 100
        def noOfRequestsTypes = 2
        def totalAmount_4_to_5 = amount_4_to_5 * noOfRequests
        def totalAmount_5_to_4 = amount_5_to_4 * noOfRequests

        def service = Executors.newFixedThreadPool(noOfRequestsTypes)

        when:
        def transferResponses = new ArrayList<RequestSender>()

        for (int i = 0; i < noOfRequestsTypes * noOfRequests; i++) {
            def request = (i % noOfRequestsTypes) ? transferRequest_4_to_5 : transferRequest_5_to_4
            service.submit {
                transferResponses.add(doPost(request))
            }
        }

        then:
        service.shutdown()
        service.awaitTermination(2, TimeUnit.SECONDS)

        transferResponses.forEach {
            transferResponse -> assertResponse(transferResponse, HTTP_OK, transferRequestId, SUCCESS, null)
        }

        // check final balances:
        def balanceResponse_4 = doGet(balanceRequest_4)
        def balanceResponse_5 = doGet(balanceRequest_5)
        assertResponse(balanceResponse_4, HTTP_OK, balanceRequest_4.id, SUCCESS, BALANCE_4 - totalAmount_4_to_5 + totalAmount_5_to_4)
        assertResponse(balanceResponse_5, HTTP_OK, balanceRequest_5.id, SUCCESS, BALANCE_5 - totalAmount_5_to_4 + totalAmount_4_to_5)

    }

    def doGet(def request) {
        def requestString = RequestUtils.toJson(request)
        def restRequest = given().body(requestString)

        restRequest.when().get('/request')
    }

    def doPost(def request) {
        def requestString = RequestUtils.toJson(request)
        def restRequest = given().body(requestString)

        restRequest.when().post('/request')
    }

    def assertResponse(def restResponse, def expectedHttpCode, def expectedId, def expectedResponseStatus, def expectedBalance) {
        restResponse.then().assertThat().statusCode(expectedHttpCode)

        def responseBody = restResponse.getBody().asString()
        def response = RequestUtils.fromJson(responseBody, Response.class)
        response.id == expectedId
        response.status == expectedResponseStatus
        response.balance == expectedBalance
    }

}
