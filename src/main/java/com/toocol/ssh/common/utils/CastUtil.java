package com.toocol.ssh.common.utils;

/**
 * @author Joezeo
 * @date 2020/12/6 16:27
 */
public class CastUtil {
    /**
     * Automatically deduce the type.
     *
     * @param obj obj
     * @param <T> generic type
     * @return result
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        if (obj == null) {
            return null;
        }
        return (T) obj;
    }
}
