package com.jpfeng.websocket;

import java.io.Serializable;

import okio.ByteString;

/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/28
 */
class WebSocketMessage implements Message, Serializable {

    String mText;
    ByteString mBinary;
    private final Type mType;

    WebSocketMessage(Type type) {
        mType = type;
    }

    @Override
    public Type type() {
        return mType;
    }

    @Override
    public String text() {
        return mText;
    }

    @Override
    public ByteString binary() {
        return mBinary;
    }
}
