package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.ScoreController;
import controller.SessionController;
import controller.Status;
import util.HttpStatusCode;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class RequestsHandlerHttp implements HttpHandler {

    private enum RequestMethodType {
        GET, POST
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        RequestMethodType requestMethodType = RequestMethodType.valueOf(httpExchange.getRequestMethod());

        URI uri = httpExchange.getRequestURI();

        Status status = new Status(HttpStatusCode.OK, "");
        String[] request = uri.getPath().split("/");

        final String id = request[1];
        final String context = request[2];

        switch (requestMethodType) {
            case GET:
                switch (context) {
                    case "login":
                        status = SessionController.INSTANCE.login(id);
                        break;

                    case "highscorelist":
                        status = ScoreController.INSTANCE.getScores(id);
                        break;

                    default:
                        status = new Status(HttpStatusCode.BAD_REQUEST, String.format("Bad request: %s", context));
                        break;
                }
                break;

            case POST:
                try (BufferedReader br = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), StandardCharsets.UTF_8))) {
                    String requestBody = br.readLine();

                    if (context.contains("score")) {
                        status = ScoreController.INSTANCE.addScore(uri.toString(), requestBody);
                    } else {
                        status = new Status(HttpStatusCode.BAD_REQUEST, String.format("Bad request: %s", context));
                    }
                }
                break;

            default:
                break;
        }

        httpExchange.sendResponseHeaders(status.getCode(), status.getMessage().length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(status.getMessage().getBytes());
        os.close();
    }
}