package com.pbroda.selfdevelopment.httpserver.db;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.pbroda.selfdevelopment.httpserver.restmodel.Request;
import com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus;
import com.pbroda.selfdevelopment.httpserver.restmodel.Response;
import lombok.RequiredArgsConstructor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus.FAILURE_DEST_ACC_DOES_NOT_EXIST;
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus.FAILURE_NOT_ENOUGH_FUNDS;
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus.FAILURE_SAME_SRC_AND_DEST_ACC;
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus.FAILURE_SRC_ACC_DOES_NOT_EXIST;
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestStatus.SUCCESS;
import static com.pbroda.selfdevelopment.httpserver.restmodel.RequestType.BALANCE;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class DbConnector {

    private final static Integer EXPECTED_NO_OF_ROWS_TO_BE_UPDATED = 2;

    private final DatabaseConnectionFactory databaseConnectionFactory;

    public synchronized Response process(Request request) throws SQLException {

        try (Connection connection = databaseConnectionFactory.getConnection()) {

            RequestStatus requestStatus = SUCCESS;
            if (request.getRequest() == BALANCE) {
                Integer balance = getBalance(connection, request.getFrom().getAccount());

                if (balance == null) {
                    return getFailureResponse(request.getId(), FAILURE_SRC_ACC_DOES_NOT_EXIST);
                } else {
                    return Response.builder().status(requestStatus)
                            .id(request.getId())
                            .balance(balance)
                            .build();
                }

            } else { // TRANSFER
                if (request.getFrom().equals(request.getTo())) {
                    return getFailureResponse(request.getId(), FAILURE_SAME_SRC_AND_DEST_ACC);
                }

                Integer balanceFrom = getBalance(connection, request.getFrom().getAccount());
                if (balanceFrom == null) {
                    return getFailureResponse(request.getId(), FAILURE_SRC_ACC_DOES_NOT_EXIST);
                }

                Integer balanceTo = getBalance(connection, request.getTo().getAccount());
                if (balanceTo == null) {
                    return getFailureResponse(request.getId(), FAILURE_DEST_ACC_DOES_NOT_EXIST);
                }

                if (request.getAmount() > balanceFrom) {
                    return getFailureResponse(request.getId(), FAILURE_NOT_ENOUGH_FUNDS);
                }

                if (transfer(connection, request, balanceFrom, balanceTo) != EXPECTED_NO_OF_ROWS_TO_BE_UPDATED) {
                    throw new SQLException();
                }

                return Response.builder().status(SUCCESS)
                        .id(request.getId())
                        .balance(null)
                        .build();
            }
        }
    }

    private Integer getBalance(Connection connection, String account) throws SQLException {
        ScalarHandler<Integer> scalarHandler = new ScalarHandler<>();
        QueryRunner runner = new QueryRunner();
        String q = "SELECT BALANCE FROM ACCOUNTS WHERE ACCOUNT=?";

        return runner.query(connection, q, scalarHandler, account);
    }

    private Integer transfer(Connection connection, Request request, Integer balanceFrom, Integer balanceTo) throws SQLException {
        QueryRunner runner = new QueryRunner();

        String q = "UPDATE ACCOUNTS SET BALANCE = CASE ACCOUNT " +
                   "WHEN ? THEN ? " +
                   "WHEN ? THEN ? " +
                   "ELSE BALANCE END WHERE ACCOUNT IN (?, ?)";

        return runner.update(connection, q, request.getFrom().getAccount(), balanceFrom - request.getAmount(),
                                            request.getTo().getAccount(), balanceTo + request.getAmount(),
                                            request.getFrom().getAccount(), request.getTo().getAccount());
    }

    private Response getFailureResponse(UUID id, RequestStatus status) {
        return Response.builder().status(status)
                .id(id)
                .balance(null)
                .build();
    }

}
