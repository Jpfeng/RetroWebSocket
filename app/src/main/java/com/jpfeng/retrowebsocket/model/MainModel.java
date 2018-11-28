package com.jpfeng.retrowebsocket.model;

import com.jpfeng.websocket.Messenger;
import com.jpfeng.websocket.RetroWebSocket;

/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/28
 */
public class MainModel {

    private final MainApi mApi;

    public MainModel(RetroWebSocket webSocket) {
        mApi = webSocket.create(MainApi.class);
    }

    public Messenger getMessenger() {
        return mApi.listen();
    }
}
