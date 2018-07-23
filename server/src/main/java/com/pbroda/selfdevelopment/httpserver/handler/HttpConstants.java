package com.pbroda.selfdevelopment.httpserver.handler;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class HttpConstants {
    static final int HTTP_OK = 200;
    static final int HTTP_FORBIDDEN = 403;

    static final int INVALID_REQUEST_FORMAT_ERROR_CODE = 400;
    static final String INVALID_REQUEST_FORMAT_ERROR_INFO = "Invalid request format";

    static final int INTERNAL_SERVER_ERROR_CODE = 500;
    static final String INTERNAL_SERVER_ERROR_INFO = "Internal server error";

    static final String GET = "GET";
    static final String POST = "POST";
}
