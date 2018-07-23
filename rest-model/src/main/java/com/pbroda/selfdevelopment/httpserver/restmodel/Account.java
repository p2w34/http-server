package com.pbroda.selfdevelopment.httpserver.restmodel;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Getter
public class Account {

    public final int ACCOUNT_LENGTH = 4;

    private final String account;

    public Account(String account) throws IllegalArgumentException {

        if (account == null || account.length() != ACCOUNT_LENGTH) {
            throw new IllegalArgumentException();
        }

        this.account = account;
    }

}
