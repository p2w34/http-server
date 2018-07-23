package com.pbroda.selfdevelopment.httpserver.restmodel;

public enum RequestStatus {
    SUCCESS,
    FAILURE_NOT_ENOUGH_FUNDS,
    FAILURE_SAME_SRC_AND_DEST_ACC,
    FAILURE_DEST_ACC_DOES_NOT_EXIST,
    FAILURE_SRC_ACC_DOES_NOT_EXIST,
}
