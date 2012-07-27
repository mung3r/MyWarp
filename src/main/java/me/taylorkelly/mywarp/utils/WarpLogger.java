package me.taylorkelly.mywarp.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WarpLogger {

    public static final Logger log = Logger.getLogger("Minecraft");

    public static void severe(String string, Exception ex) {
        log.log(Level.SEVERE, "[MyWarp] " + string, ex);
    }

    public static void severe(String string) {
        log.log(Level.SEVERE, "[MyWarp] " + string);
    }

    public static void info(String string) {
        log.log(Level.INFO, "[MyWarp] " + string);
    }

    public static void warning(String string) {
        log.log(Level.WARNING, "[MyWarp] " + string);
    }
}
