package com.jpfeng.websocket;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/23
 */
class WebSocketClient {

    private static final int STATUS_DISCONNECTED = 0;
    private static final int STATUS_CONNECTING = 1;
    private static final int STATUS_CONNECTED = 2;
    private static final int STATUS_RECONNECTING = 3;

    private RetroWebSocket mHost;
    private WebSocket mWebSocket;
    private volatile int mStatus;

    private final Request mRequest;
    private final WebSocket.Factory mFactory;
    private final WebSocketListener mListener;

    WebSocketClient(Request request, WebSocket.Factory factory) {
        mRequest = request;
        mFactory = factory;
        mListener = generateListener();
        mStatus = STATUS_DISCONNECTED;
    }

    private WebSocketListener generateListener() {
        return new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                if (STATUS_RECONNECTING == mStatus) {
                    setStatus(STATUS_CONNECTED);
                    mHost.dispatchReconnect(response);
                } else {
                    setStatus(STATUS_CONNECTED);
                    mHost.dispatchConnect(response);
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
                WebSocketMessage message = new WebSocketMessage(Message.Type.Binary);
                message.mBinary = bytes;
                mHost.launchMessage(message);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                WebSocketMessage message = new WebSocketMessage(Message.Type.Text);
                message.mText = text;
                mHost.launchMessage(message);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                setStatus(STATUS_DISCONNECTED);
                mHost.dispatchDisconnect(code, reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                if (STATUS_CONNECTED == mStatus) {
                    setStatus(STATUS_RECONNECTING);
                    mHost.dispatchConnectionLost(t);
                }
                mHost.tryReconnect(t);
            }
        };
    }

    void attach(RetroWebSocket webSocket) {
        mHost = webSocket;
    }

    void connect() {
        setStatus(STATUS_CONNECTING);
        performConnect();
    }

    void disconnect(int code, String reason) {
        switch (mStatus) {
            case STATUS_CONNECTED:
                mWebSocket.close(code, reason);
                break;
            case STATUS_CONNECTING:
            case STATUS_RECONNECTING:
                mWebSocket.cancel();
                setStatus(STATUS_DISCONNECTED);
                mHost.dispatchDisconnect(code, reason);
                break;
            case STATUS_DISCONNECTED:
            default:
        }
    }

    void reconnect() {
        performConnect();
    }

    private void performConnect() {
        mWebSocket = mFactory.newWebSocket(mRequest, mListener);
    }

    boolean send(String text) {
        return mWebSocket.send(text);
    }

    boolean send(ByteString bytes) {
        return mWebSocket.send(bytes);
    }

    private void setStatus(int status) {
        mStatus = status;
    }

    boolean isConnected() {
        return STATUS_CONNECTED == mStatus;
    }
}
