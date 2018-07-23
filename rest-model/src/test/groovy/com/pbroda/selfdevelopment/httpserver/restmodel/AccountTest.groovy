package com.pbroda.selfdevelopment.httpserver.restmodel

import spock.lang.Specification

import static com.pbroda.selfdevelopment.httpserver.TestConstants.ACCOUNT_NUMBER_1

class AccountTest extends Specification {

    def 'should create account' () {
        when:
        def account = new Account(ACCOUNT_NUMBER_1)

        then:
        ACCOUNT_NUMBER_1.equals(account.getAccount())
    }

    def 'should not create account'() {
        given:
        def accountNumberTooLong = ACCOUNT_NUMBER_1 + '1'

        when:
        new Account(accountNumberTooLong)

        then:
        thrown(IllegalArgumentException.class)
    }

}
