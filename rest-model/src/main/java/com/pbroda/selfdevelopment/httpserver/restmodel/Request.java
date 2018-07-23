package com.pbroda.selfdevelopment.httpserver.restmodel;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.UUID;

@Builder
@Getter
@EqualsAndHashCode
public class Request {
    @Builder.Default UUID id = UUID.randomUUID();
    @Builder.Default Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    RequestType request;
    Account from;
    Account to;
    Integer amount;
}
