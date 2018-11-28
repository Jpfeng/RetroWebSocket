package com.jpfeng.websocket;

import okio.ByteString;

/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/28
 */
public interface Message {
    Type type();
    String text();
    ByteString binary();

    enum Type {
        Text,
        Binary
    }
}
