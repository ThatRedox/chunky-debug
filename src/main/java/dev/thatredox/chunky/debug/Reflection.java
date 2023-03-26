package dev.thatredox.chunky.debug;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

public class Reflection {
    private Reflection() {}

    public static <T> Optional<T> getDeclaredField(Object obj, String fieldName, Class<T> type) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            T value = type.cast(f.get(obj));
            return Optional.of(value);
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            return Optional.empty();
        }
    }

    public static <T> Optional<Method> getDeclaredMethod(Object obj, String methodName, Class<?>... params) {
        try {
            Method method = obj.getClass().getDeclaredMethod(methodName, params);
            method.setAccessible(true);
            return Optional.of(method);
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }
}
