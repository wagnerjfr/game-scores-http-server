package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import controller.ScoreController;
import controller.SessionController;
import util.HttpStatusCode;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class RequestsHandlerHttp implements HttpHandler {

    private enum RequestMethodType {
        GET, POST, NONE
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        RequestMethodType requestMethodType = RequestMethodType.valueOf(httpExchange.getRequestMethod());

        URI uri = httpExchange.getRequestURI();

        Status status = new Status(HttpStatusCode.OK, "");
        String[] request = uri.getPath().split("/");

        int id = 0;
        String context = "";
        try {
            id = Integer.parseInt(request[1]);
            context = request[2];
        } catch (Exception e) {
            requestMethodType = RequestMethodType.NONE;
            status = new Status(HttpStatusCode.BAD_REQUEST, String.format("Bad request %s", e));
        }

        switch (requestMethodType) {
            case GET:
                switch (context) {
                    case "login":
                        status = processLogin(id);
                        break;

                    case "highscorelist":
                        status = new Status(HttpStatusCode.OK, ScoreController.INSTANCE.getScores(id));
                        break;

                    default:
                        status = new Status(HttpStatusCode.BAD_REQUEST, String.format("Bad request: %s", context));
                        break;
                }
                break;

            case POST:
                if (context.contains("score")) {
                    status = processAddScore(uri.toString(), httpExchange.getRequestBody());
                } else {
                    status = new Status(HttpStatusCode.BAD_REQUEST, String.format("Bad request: %s", context));
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

    private Status processLogin(int id) {
        return SessionController.INSTANCE.login(id)
            .map(sc -> new Status(HttpStatusCode.OK, sc))
            .orElse(new Status(HttpStatusCode.BAD_REQUEST, ""));
    }

    private Status processAddScore(String uri, InputStream inputStream) throws IOException {
        final int paramIndexValue = 1;
        final String equalDelimiter = "=";

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String requestBody = br.readLine();

            int levelId;
            int score;
            String sessionKey;

            // Parse request
            try {
                levelId = Integer.parseInt(uri.split("/")[paramIndexValue]);
                score = Integer.parseInt(requestBody.split(equalDelimiter)[paramIndexValue]);

                int index = uri.indexOf(equalDelimiter);
                sessionKey = uri.substring(index + 1);

            } catch (NumberFormatException e) {
                return new Status(HttpStatusCode.BAD_REQUEST, String.format("Bad request: %s", e.getMessage()));
            }

            boolean result = ScoreController.INSTANCE.addScore(sessionKey, levelId, score);

            int code = result ? HttpStatusCode.CREATED : HttpStatusCode.UNAUTHORIZED;
            return new Status(code, "");
        }
    }
}