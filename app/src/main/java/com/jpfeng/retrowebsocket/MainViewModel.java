package com.jpfeng.retrowebsocket;

import android.app.Application;
import android.text.TextUtils;

import com.jpfeng.retrowebsocket.Record.RecordType;
import com.jpfeng.retrowebsocket.databinding.ActivityMainBinding;
import com.jpfeng.retrowebsocket.model.MainModel;
import com.jpfeng.websocket.ConnectionListener;
import com.jpfeng.websocket.Messenger;
import com.jpfeng.websocket.RetroWebSocket;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import okhttp3.Response;

/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/20
 */
public class MainViewModel extends AndroidViewModel {
    private static final String DEFAULT_WSS_ADDRESS = "wss://echo.websocket.org";
    private static final String DEFAULT_MESSAGE = "Hello WebSocket!";

    private final MutableLiveData<Boolean> mConnecting = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mConnected = new MutableLiveData<>();
    private final MutableLiveData<String> mAddress = new MutableLiveData<>();
    private final MutableLiveData<String> mMessage = new MutableLiveData<>();
    private final MutableLiveData<String> mHint = new MutableLiveData<>();

    private final List<Record> mMessageList = new ArrayList<>();
    private final MutableLiveData<List<Record>> mData = new MutableLiveData<>();

    private RetroWebSocket mWebSocket;
    private final SimpleDateFormat mDateFormat;

    public MainViewModel(@NonNull Application application) {
        super(application);
        mConnecting.setValue(false);
        mConnected.setValue(false);
        mAddress.setValue(DEFAULT_WSS_ADDRESS);
        mMessage.setValue(DEFAULT_MESSAGE);
        mDateFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
    }

    LiveData<Boolean> isConnecting() {
        return mConnecting;
    }

    LiveData<Boolean> isConnected() {
        return mConnected;
    }

    LiveData<String> getAddress() {
        return mAddress;
    }

    LiveData<String> getMessage() {
        return mMessage;
    }

    LiveData<String> getHint() {
        return mHint;
    }

    LiveData<List<Record>> getListData() {
        return mData;
    }

    void updateField(ActivityMainBinding observable, int id) {
        switch (id) {
            case BR.address:
                setAddress(observable.getAddress());
                break;
            case BR.message:
                setMessage(observable.getMessage());
                break;
            default:
        }
    }

    private void setAddress(String address) {
        if (!TextUtils.equals(address, this.mAddress.getValue())) {
            this.mAddress.setValue(address);
        }
    }

    private void setMessage(String message) {
        if (!TextUtils.equals(message, this.mMessage.getValue())) {
            this.mMessage.setValue(message);
        }
    }

    void changeConnection() {
        boolean connective = null != mConnected.getValue() && mConnected.getValue();

        if (connective) {
            mWebSocket.disconnect(1000, "");

        } else {
            mConnecting.setValue(true);
            mWebSocket = new RetroWebSocket.Builder()
                    .url(mAddress.getValue())
                    .build();

            Messenger messenger = new MainModel(mWebSocket).getMessenger();
            messenger.registerReceiver(message
                    -> addMessage(generateRecord(message.text(), Record.TYPE_MESSAGE_RECEIVE)));

            mWebSocket.addConnectionListener(new ConnectionListener() {
                @Override
                public void onConnected(RetroWebSocket client, Response response) {
                    super.onConnected(client, response);
                    mConnected.postValue(true);
                    mConnecting.postValue(false);
                    addMessage(generateRecord("", Record.TYPE_CONNECTED));
                }

                @Override
                public void onConnectionLost(RetroWebSocket client, Throwable reason) {
                    super.onConnectionLost(client, reason);
                    mConnecting.postValue(true);
                    addMessage(generateRecord("", Record.TYPE_CONNECTION_LOST));
                }

                @Override
                public void onReconnected(RetroWebSocket client, Response response) {
                    super.onReconnected(client, response);
                    mConnecting.postValue(false);
                    addMessage(generateRecord("", Record.TYPE_RECONNECTED));
                }

                @Override
                public void onDisconnected(RetroWebSocket client, int code, String reason) {
                    super.onDisconnected(client, code, reason);
                    mConnected.postValue(false);
                    mConnecting.postValue(false);
                    addMessage(generateRecord("", Record.TYPE_DISCONNECTED));
                    client.removeConnectionListener(this);
                }
            });

            mWebSocket.connect();
        }
    }

    void sendMessage() {
        String text = mMessage.getValue();
        if (TextUtils.isEmpty(text)) {
            mHint.setValue(getApplication().getString(R.string.hint_empty_message));
            return;
        }
        if (mWebSocket.send(text)) {
            addMessage(generateRecord(text, Record.TYPE_MESSAGE_SEND));
            mMessage.setValue("");
        } else {
            mHint.setValue(getApplication().getString(R.string.hint_send_fail));
        }
    }

    void clearHint() {
        mHint.setValue("");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    private Record generateRecord(String rawMessage, @RecordType int type) {
        Record record = new Record();
        long timeMillis = System.currentTimeMillis();
        record.setTimeMillis(timeMillis);
        record.setRawMessage(rawMessage);
        record.setType(type);
        record.setTimeString(formatTime(timeMillis));
        record.setMessage(formatMessage(rawMessage, type));
        record.generateId();
        return record;
    }

    private String formatTime(long timeMillis) {
        return mDateFormat.format(new Date(timeMillis));
    }

    private String formatMessage(String rawMessage, @RecordType int type) {
        switch (type) {
            case Record.TYPE_MESSAGE_SEND:
                return getApplication().getString(R.string.template_send, rawMessage);
            case Record.TYPE_MESSAGE_RECEIVE:
                return getApplication().getString(R.string.template_receive, rawMessage);
            case Record.TYPE_CONNECTED:
                return getApplication().getString(R.string.template_connect);
            case Record.TYPE_DISCONNECTED:
                return getApplication().getString(R.string.template_disconnect);
            case Record.TYPE_CONNECTION_LOST:
                return getApplication().getString(R.string.template_connection_lost);
            case Record.TYPE_RECONNECTED:
                return getApplication().getString(R.string.template_reconnect);
            default:
                return rawMessage;
        }
    }

    private void addMessage(Record message) {
        mMessageList.add(0, message);
        mData.postValue(new ArrayList<>(mMessageList));
    }
}
