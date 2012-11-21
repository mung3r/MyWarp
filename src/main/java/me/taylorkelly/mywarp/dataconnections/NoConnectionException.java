package me.taylorkelly.mywarp.dataconnections;

public class NoConnectionException extends Exception {

    private static final long serialVersionUID = 1L;

    public NoConnectionException() {
        super();
    }

    public NoConnectionException(String message) {
        super(message);
    }

    public NoConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoConnectionException(Throwable cause) {
        super(cause);
    }
}
