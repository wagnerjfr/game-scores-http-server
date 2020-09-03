package server;

final class Status {

    private int code;
    private String message;

    Status(int code, String message) {
        this.code = code;
        this.message = message;
    }

    int getCode() {
        return code;
    }

    String getMessage() {
        return message;
    }
}
