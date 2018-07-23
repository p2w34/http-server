package com.pbroda.selfdevelopment.httpserver;

import com.google.inject.Inject;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpServerApplication {

    private HttpServer server;

    @Inject
    public HttpServerApplication(HttpHandler requestHandler) throws IOException {
        server = HttpServer.create(new InetSocketAddress(8407), 0);
        server.createContext("/request", requestHandler);
        server.setExecutor(null); // creates a default executor
        server.start();
    }


}
