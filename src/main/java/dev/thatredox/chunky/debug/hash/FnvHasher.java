package dev.thatredox.chunky.debug.hash;

public class FnvHasher implements Hasher {
    private final static long OFFSET_BASIS = 0xcbf29ce484222325L;
    private final static long FNV_PRIME = 1099511628211L;
    private long hash = OFFSET_BASIS;

    public void write_byte(byte value) {
        hash = (hash * FNV_PRIME) ^ Byte.toUnsignedLong(value);
    }

    @Override
    public void write_int(int value) {
        write_byte((byte) (value >> 24));
        write_byte((byte) (value >> 16));
        write_byte((byte) (value >> 8));
        write_byte((byte) value);
    }

    @Override
    public long finish() {
        return hash;
    }
}
