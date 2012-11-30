package me.taylorkelly.mywarp.dataconnections;

public class DataConnectionException extends Exception {

    private static final long serialVersionUID = 1L;

    public DataConnectionException() {
        super();
    }

    public DataConnectionException(String message) {
        super(message);
    }

    public DataConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataConnectionException(Throwable cause) {
        super(cause);
    }
}
