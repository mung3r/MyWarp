package me.taylorkelly.mywarp.commands;

public class CommandException extends Exception {

    private static final long serialVersionUID = 5816318584241685492L;

    public CommandException() {
        super();
    }

    public CommandException(String message) {
        super(message);
    }

    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandException(Throwable cause) {
        super(cause);
    }
}
