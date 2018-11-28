package com.jpfeng.websocket;

import okhttp3.Response;

/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/26
 */
public abstract class ConnectionListener {
    public void onConnected(RetroWebSocket client, Response response) {
    }

    public void onConnectionLost(RetroWebSocket client, Throwable reason) {
    }

    public void onReconnected(RetroWebSocket client, Response response) {
    }

    public void onDisconnected(RetroWebSocket client, int code, String reason) {
    }
}
