package com.jpfeng.websocket;

import java.io.Serializable;

import okio.ByteString;

/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/28
 */
public class WebSocketMessage implements Message, Serializable {

    Type type;
    String text;
    ByteString binary;

    @Override
    public Type type() {
        return type;
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public ByteString binary() {
        return binary;
    }
}
