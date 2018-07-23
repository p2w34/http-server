package com.pbroda.selfdevelopment.httpserver

import com.pbroda.selfdevelopment.httpserver.restmodel.Account

import java.sql.Timestamp

class TestConstants {

    def static TEST_ID_STRING = '2ac64666-8e87-4243-a0df-bad59ed1be9e'
    def static TEST_ID = UUID.fromString(TEST_ID_STRING)

    def static TEST_TIMESTAMP_STRING = '2018-07-15 15:25:42.443'
    def static TEST_TIMESTAMP = Timestamp.valueOf(TEST_TIMESTAMP_STRING)

    def static ACCOUNT_NUMBER_1 = '1001'
    def static ACCOUNT_NUMBER_2 = '1002'
    def static ACCOUNT_NUMBER_3 = '1003'
    def static ACCOUNT_NUMBER_4 = '1004'
    def static ACCOUNT_NUMBER_5 = '1005'
    def static TEST_NON_EXISTING_ACCOUNT_STRING = '9999'

    def static ACCOUNT_1 = new Account(ACCOUNT_NUMBER_1)
    def static ACCOUNT_2 = new Account(ACCOUNT_NUMBER_2)
    def static ACCOUNT_3 = new Account(ACCOUNT_NUMBER_3)
    def static ACCOUNT_4 = new Account(ACCOUNT_NUMBER_4)
    def static ACCOUNT_5 = new Account(ACCOUNT_NUMBER_5)
    def static NON_EXISTING_ACCOUNT = new Account(TEST_NON_EXISTING_ACCOUNT_STRING)

    def static BALANCE_1 = 1001
    def static BALANCE_2 = 1002
    def static BALANCE_3 = 1003
    def static BALANCE_4 = 1004
    def static BALANCE_5 = 1005

    def static TEST_AMOUNT = 123

}

