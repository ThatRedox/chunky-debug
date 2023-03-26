package dev.thatredox.chunky.debug;

public class DebugException extends AssertionError {
    public static void debug() {
        long start = System.currentTimeMillis();
        try {
            throw new DebugException();
        } catch (DebugException e) {
            if (System.currentTimeMillis() - start < 10) {
                System.err.println("Failed to catch debug exception! " +
                        "Register `AssertionError` " +
                        "as an exception breakpoint.");
            }
        }
    }
}
