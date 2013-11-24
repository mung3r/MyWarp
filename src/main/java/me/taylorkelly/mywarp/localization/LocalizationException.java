package me.taylorkelly.mywarp.localization;

public class LocalizationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 7145096788413242658L;

    public LocalizationException() {
        super();
    }

    public LocalizationException(String message) {
        super(message);
    }

    public LocalizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public LocalizationException(Throwable cause) {
        super(cause);
    }

}
