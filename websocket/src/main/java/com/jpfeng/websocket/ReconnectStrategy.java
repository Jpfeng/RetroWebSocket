package com.jpfeng.websocket;

/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/27
 */
public abstract class ReconnectStrategy {
    public abstract void onConnectionLost(RetroWebSocket webSocket, Throwable reason);
    public abstract void onReconnected(RetroWebSocket webSocket);

    protected void performReconnect(RetroWebSocket webSocket) {
        webSocket.performReconnect();
    }
}
