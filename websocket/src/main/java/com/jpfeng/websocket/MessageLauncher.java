package com.jpfeng.websocket;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/28
 */
public interface MessageLauncher<R> {
    R prepare();
    void launchMessage(Message message);
    void tearDown();

    abstract class Factory {
        public abstract MessageLauncher<?> get(Type returnType, LaunchPad launchPad);

        protected static Class<?> getRawType(Type type) {
            return Utils.getRawType(type);
        }

        protected static Type getParameterUpperBound(int index, ParameterizedType type) {
            return Utils.getParameterUpperBound(index, type);
        }
    }
}
