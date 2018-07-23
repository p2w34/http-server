package com.pbroda.selfdevelopment.httpserver.handler;

import com.google.inject.Singleton;
import com.sun.net.httpserver.HttpExchange;

@Singleton
public class ExchangeProcessorFactory {

    public ExchangeProcessor get(HttpExchange exchange) {
        return new ExchangeProcessor(exchange);
    }

}
