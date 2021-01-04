package sciwhiz12.cartographer.util;

public class Logging {
    private Logging() {}

    public static void debugf(String message, Object... args) {
        //        System.out.printf(message, args);
    }

    public static void logf(String message, Object... args) {
        System.out.printf(message, args);
    }

    public static void errf(String message, Object... args) {
        System.err.printf(message, args);
    }
}
