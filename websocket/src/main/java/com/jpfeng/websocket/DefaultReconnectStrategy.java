package com.jpfeng.websocket;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/27
 */
public class DefaultReconnectStrategy extends ReconnectStrategy {

    private static final int RECONNECT_INTERVAL = 2000;

    @Override
    public void onConnectionLost(RetroWebSocket webSocket, Throwable reason) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                performReconnect(webSocket);
            }
        };
        new Timer().schedule(task, RECONNECT_INTERVAL);
    }

    @Override
    public void onReconnected(RetroWebSocket webSocket) {
    }
}
