package com.jpfeng.websocket;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/23
 */
class Utils {

    private Utils() {
    }

    static <T> T checkNotNull(T object, String message) {
        if (null == object) {
            throw new NullPointerException(message);
        }
        return object;
    }

    static String checkUrl(String url) {
        if (null == url) {
            throw new NullPointerException("url == null");
        }
        if (url.regionMatches(true, 0, "ws://", 0, 5)
                || url.regionMatches(true, 0, "wss://", 0, 6)) {
            return url;
        } else {
            throw new IllegalArgumentException("invalid WebSocket url");
        }
    }

    static Class<?> getRawType(Type type) {
        checkNotNull(type, "type == null");

        if (type instanceof Class<?>) {
            // Type is a normal class.
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            // I'm not exactly sure why getRawType() returns Type instead of Class. Neal isn't either but
            // suspects some pathological case related to nested classes exists.
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) throw new IllegalArgumentException();
            return (Class<?>) rawType;
        }
        if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();
        }
        if (type instanceof TypeVariable) {
            // We could use the variable's bounds, but that won't work if there are multiple. Having a raw
            // type that's more general than necessary is okay.
            return Object.class;
        }
        if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);
        }

        throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                + "GenericArrayType, but <" + type + "> is of type " + type.getClass().getName());
    }

    static Type getParameterUpperBound(int index, ParameterizedType type) {
        Type[] types = type.getActualTypeArguments();
        if (index < 0 || index >= types.length) {
            throw new IllegalArgumentException(
                    "Index " + index + " not in range [0," + types.length + ") for " + type);
        }
        Type paramType = types[index];
        if (paramType instanceof WildcardType) {
            return ((WildcardType) paramType).getUpperBounds()[0];
        }
        return paramType;
    }
}
