package com.pbroda.selfdevelopment.httpserver.restmodel;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Builder
@EqualsAndHashCode
@Getter
public class Response {
    UUID id;
    RequestStatus status;
    @Builder.Default Integer balance = null;
}
