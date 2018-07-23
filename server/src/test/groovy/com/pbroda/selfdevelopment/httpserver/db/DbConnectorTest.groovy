package com.pbroda.selfdevelopment.httpserver.db

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.name.Names
import com.pbroda.selfdevelopment.httpserver.restmodel.Request
import com.pbroda.selfdevelopment.httpserver.restmodel.Response
import org.apache.commons.dbutils.QueryRunner
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Connection
import java.sql.SQLException

import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus.SUCCESS
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus.FAILURE_DEST_ACC_DOES_NOT_EXIST
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus.FAILURE_NOT_ENOUGH_FUNDS
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus.FAILURE_SAME_SRC_AND_DEST_ACC
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus.FAILURE_SRC_ACC_DOES_NOT_EXIST
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestType.BALANCE
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestType.TRANSFER
import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_1
import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_2
import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_3
import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_NUMBER_1
import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_NUMBER_2
import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_NUMBER_3
import static com.pbroda.selfdevelopment.httpserver.TestConstants.BALANCE_1
import static com.pbroda.selfdevelopment.httpserver.TestConstants.BALANCE_2
import static com.pbroda.selfdevelopment.httpserver.TestConstants.BALANCE_3
import static com.pbroda.selfdevelopment.httpserver.TestConstants.NON_EXISTING_ACCOUNT

class DbConnectorTest extends Specification {

    static class DbConnectorTestModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(String.class).annotatedWith(Names.named('JDBC URL'))
                    .toInstance('jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1') // DB_CLOSE_DELAY=-1 keeps the DB in the memory
            bind(DatabaseConnectionFactory.class)
        }

    }

    @Shared
    def injector

    @Shared
    def dbConnector

    def setupSpec() {
        injector = Guice.createInjector(new DbConnectorTestModule())
        dbConnector = injector.getInstance(DbConnector.class);
    }

    def setup() throws SQLException {
        def statement = "CREATE TABLE ACCOUNTS (ACCOUNT VARCHAR(64) PRIMARY KEY," +
                        "                       BALANCE INT); " +
                        "INSERT INTO ACCOUNTS VALUES ($ACCOUNT_NUMBER_1, $BALANCE_1); " +
                        "INSERT INTO ACCOUNTS VALUES ($ACCOUNT_NUMBER_2, $BALANCE_2); " +
                        "INSERT INTO ACCOUNTS VALUES ($ACCOUNT_NUMBER_3, $BALANCE_3);"

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

    def 'should provide balance request'() {
        given:
        Request requestBalance =
                Request.builder()
                        .request(BALANCE)
                        .from(fromAccount)
                        .build()

        when:
        Response responseBalance = dbConnector.process(requestBalance)

        then:
        responseBalance.id == requestBalance.id
        responseBalance.status == expectedResponseStatus
        responseBalance.balance == expectedBalance

        where:
        fromAccount          || expectedResponseStatus         | expectedBalance
        ACCOUNT_1            || SUCCESS                        | BALANCE_1
        ACCOUNT_2            || SUCCESS                        | BALANCE_2
        ACCOUNT_3            || SUCCESS                        | BALANCE_3
        NON_EXISTING_ACCOUNT || FAILURE_SRC_ACC_DOES_NOT_EXIST | null
    }

    def 'should transfer funds between accounts'() {
        given:
        Request requestTransfer =
                Request.builder()
                        .request(TRANSFER)
                        .from(fromAccount)
                        .to(toAccount)
                        .amount(amount)
                        .build()

        Request requestBalance1 =
                Request.builder()
                        .request(BALANCE)
                        .from(ACCOUNT_1)
                        .build()

        Request requestBalance2 =
                Request.builder()
                        .request(BALANCE)
                        .from(ACCOUNT_2)
                        .build()

        Request requestBalance3 =
                Request.builder()
                        .request(BALANCE)
                        .from(ACCOUNT_3)
                        .build()

        when:
        Response responseTransfer = dbConnector.process(requestTransfer)

        Response responseBalance1 = dbConnector.process(requestBalance1)
        Response responseBalance2 = dbConnector.process(requestBalance2)
        Response responseBalance3 = dbConnector.process(requestBalance3)

        then:
        responseTransfer.id == requestTransfer.id
        responseTransfer.status == expectedResponseStatus
        responseTransfer.balance == expectedBalance

        responseBalance1.balance == expectedEndBalance1
        responseBalance2.balance == expectedEndBalance2
        responseBalance3.balance == expectedEndBalance3

        where:
        fromAccount          | toAccount            | amount        || expectedResponseStatus          | expectedBalance | expectedEndBalance1 | expectedEndBalance2 | expectedEndBalance3
        ACCOUNT_1            | ACCOUNT_3            | BALANCE_1     || SUCCESS                         | null            | 0                   | BALANCE_2           | BALANCE_1 + BALANCE_3
        ACCOUNT_2            | ACCOUNT_3            | BALANCE_2 + 1 || FAILURE_NOT_ENOUGH_FUNDS        | null            | BALANCE_1           | BALANCE_2           | BALANCE_3
        ACCOUNT_3            | ACCOUNT_3            | 1             || FAILURE_SAME_SRC_AND_DEST_ACC   | null            | BALANCE_1           | BALANCE_2           | BALANCE_3
        ACCOUNT_3            | NON_EXISTING_ACCOUNT | 1             || FAILURE_DEST_ACC_DOES_NOT_EXIST | null            | BALANCE_1           | BALANCE_2           | BALANCE_3
        NON_EXISTING_ACCOUNT | ACCOUNT_3            | 1             || FAILURE_SRC_ACC_DOES_NOT_EXIST  | null            | BALANCE_1           | BALANCE_2           | BALANCE_3
    }

}
