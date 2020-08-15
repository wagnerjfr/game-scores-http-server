package server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WebServerHttp {

    private static final int PORT = 8081;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new RequestsHandlerHttp());

        server.setExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));

        server.start();

        System.out.println("WebServerHttp is listening on port " + PORT);
    }
}