package com.jpfeng.retrowebsocket;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/20
 */
public class Record implements Serializable {

    public static final int TYPE_MESSAGE_SEND = 1;
    public static final int TYPE_MESSAGE_RECEIVE = 2;
    public static final int TYPE_CONNECTED = 3;
    public static final int TYPE_DISCONNECTED = 4;
    public static final int TYPE_CONNECTION_LOST = 5;
    public static final int TYPE_RECONNECTED = 6;

    private int id;
    private long timeMillis;
    private String timeString;
    private String rawMessage;
    private String message;
    @RecordType
    private int type;

    public int getId() {
        return id;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }

    public String getTimeString() {
        return timeString;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public void setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @RecordType
    public int getType() {
        return type;
    }

    public void setType(@RecordType int type) {
        this.type = type;
    }

    public void generateId() {
        this.id = hashCode();
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_MESSAGE_SEND, TYPE_MESSAGE_RECEIVE, TYPE_CONNECTED,
            TYPE_DISCONNECTED, TYPE_CONNECTION_LOST, TYPE_RECONNECTED})
    public @interface RecordType {
    }
}
