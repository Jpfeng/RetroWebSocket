package com.jpfeng.websocket;

import com.jpfeng.websocket.annotation.Listen;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;
import retrofit2.Retrofit;

/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/22
 */
public final class RetroWebSocket {

    private final Retrofit mRetrofit;
    private final LaunchPad mLaunchPad;
    private final WebSocketClient mClient;
    private final ReconnectStrategy mStrategy;
    private final List<ConnectionListener> mListeners;
    private final List<MessageLauncher.Factory> mLauncherFactories;
    private final Map<Method, MessageLauncher<?>> mMessageLauncherCache;

    private RetroWebSocket(Request request, WebSocket.Factory factory, ReconnectStrategy strategy,
                           Retrofit retrofit, List<MessageLauncher.Factory> launcherFactories,
                           List<ConnectionListener> listeners) {
        mClient = new WebSocketClient(request, factory);
        mRetrofit = retrofit;
        mStrategy = strategy;
        mListeners = listeners;
        mLaunchPad = new LaunchPad();
        mLauncherFactories = launcherFactories;
        mMessageLauncherCache = new ConcurrentHashMap<>();
        mClient.attach(this);
    }

    public void connect() {
        mClient.connect();
    }

    public void disconnect(int code, String reason) {
        mClient.disconnect(code, reason);
    }

    public boolean send(String text) {
        return mClient.send(text);
    }

    public boolean send(ByteString bytes) {
        return mClient.send(bytes);
    }

    public boolean isConnected() {
        return mClient.isConnected();
    }

    public <T> T create(final Class<T> service) {
        return create(service, mRetrofit);
    }

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<T> service, final Retrofit retrofit) {
        Utils.checkNotNull(retrofit, "retrofit required");
        final T retrofitService = retrofit.create(service);
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args)
                            throws Throwable {
                        // If the method is a method from Object then defer to normal invocation.
                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }
                        if (method.isAnnotationPresent(Listen.class)) {
                            return loadMessageLauncher(method).prepare();
                        } else {
                            return method.invoke(retrofitService, args);
                        }
                    }
                });
    }

    public void addConnectionListener(ConnectionListener listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

    public void removeConnectionListener(ConnectionListener listener) {
        synchronized (mListeners) {
            mListeners.remove(listener);
        }
    }

    void launchMessage(Message message) {
        mLaunchPad.launch(message);
    }

    void performReconnect() {
        mClient.reconnect();
    }

    void tryReconnect(Throwable reason) {
        mStrategy.onConnectionLost(this, reason);
    }

    void dispatchConnect(Response response) {
        ConnectionListener[] listeners = collectConnectionListeners();
        for (ConnectionListener listener : listeners) {
            listener.onConnected(this, response);
        }
    }

    void dispatchConnectionLost(Throwable reason) {
        ConnectionListener[] listeners = collectConnectionListeners();
        for (ConnectionListener listener : listeners) {
            listener.onConnectionLost(this, reason);
        }
    }

    void dispatchReconnect(Response response) {
        mStrategy.onReconnected(this);
        ConnectionListener[] listeners = collectConnectionListeners();
        for (ConnectionListener listener : listeners) {
            listener.onReconnected(this, response);
        }
    }

    void dispatchDisconnect(int code, String reason) {
        ConnectionListener[] listeners = collectConnectionListeners();
        for (ConnectionListener listener : listeners) {
            listener.onDisconnected(this, code, reason);
        }
    }

    private MessageLauncher<?> loadMessageLauncher(Method method) {
        MessageLauncher<?> result = mMessageLauncherCache.get(method);
        if (null != result) {
            return result;
        }
        Type returnType = method.getGenericReturnType();
        for (MessageLauncher.Factory factory : mLauncherFactories) {
            MessageLauncher<?> launcher = factory.get(returnType, mLaunchPad);
            if (null != launcher) {
                mMessageLauncherCache.put(method, launcher);
                return launcher;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate message launcher for ")
                .append(returnType)
                .append(".\n");
        builder.append("\tTried:");
        for (MessageLauncher.Factory factory : mLauncherFactories) {
            builder.append("\n\t * ").append(factory.getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    private ConnectionListener[] collectConnectionListeners() {
        ConnectionListener[] listeners = new ConnectionListener[]{};
        synchronized (mListeners) {
            if (mListeners.size() > 0) {
                listeners = mListeners.toArray(listeners);
            }
        }
        return listeners;
    }

    public static class Builder {

        private String mUrl;
        private Retrofit mRetrofit;
        private ReconnectStrategy mStrategy;
        private WebSocket.Factory mWebSocketFactory;
        private final List<ConnectionListener> mConnectionList = new ArrayList<>();
        private final List<MessageLauncher.Factory> mLauncherFactories = new ArrayList<>();

        public Builder client(OkHttpClient client) {
            return webSocketFactory(Utils.checkNotNull(client, "okHttpClient == null"));
        }

        public Builder webSocketFactory(WebSocket.Factory factory) {
            mWebSocketFactory = Utils.checkNotNull(factory, "webSocketFactory == null");
            return this;
        }

        public Builder retrofit(Retrofit retrofit) {
            mRetrofit = Utils.checkNotNull(retrofit, "retrofit == null");
            return this;
        }

        public Builder url(String url) {
            mUrl = Utils.checkUrl(url);
            return this;
        }

        public Builder reconnectStrategy(ReconnectStrategy strategy) {
            mStrategy = Utils.checkNotNull(strategy, "reconnectStrategy == null");
            return this;
        }

        public Builder addLauncherFactory(MessageLauncher.Factory factory) {
            mLauncherFactories.add(Utils.checkNotNull(factory, "factory == null"));
            return this;
        }

        public Builder addConnectionListener(ConnectionListener listener) {
            mConnectionList.add(Utils.checkNotNull(listener, "connectionListener == null"));
            return this;
        }

        public RetroWebSocket build() {
            if (null == mUrl) {
                throw new IllegalStateException("WebSocket URL required");
            }

            ReconnectStrategy strategy = mStrategy;
            if (null == strategy) {
                strategy = new DefaultReconnectStrategy();
            }

            WebSocket.Factory socketFactory = mWebSocketFactory;
            if (null == socketFactory) {
                socketFactory = new OkHttpClient();
            }

            List<MessageLauncher.Factory> launcherFactories = new ArrayList<>(mLauncherFactories);
            launcherFactories.add(new MessengerFactory());

            List<ConnectionListener> connectionList = new ArrayList<>(mConnectionList);

            Request request = new Request.Builder().url(mUrl).build();
            return new RetroWebSocket(request, socketFactory, strategy, mRetrofit,
                    launcherFactories, connectionList);
        }

        public RetroWebSocket connect() {
            RetroWebSocket webSocket = build();
            webSocket.connect();
            return webSocket;
        }
    }
}
