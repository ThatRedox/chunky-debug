package dev.thatredox.chunky.debug.hash;

public interface Hasher {
    void write_int(int value);
    long finish();
}
