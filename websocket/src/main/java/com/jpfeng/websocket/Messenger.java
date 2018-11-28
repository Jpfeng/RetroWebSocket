package com.jpfeng.websocket;

import java.util.ArrayList;

/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/26
 */
public class Messenger {

    private final ArrayList<Receiver> mReceiverList;
    private volatile boolean mTerminated;

    Messenger() {
        mReceiverList = new ArrayList<>();
        mTerminated = false;
    }

    public void registerReceiver(Receiver receiver) {
        synchronized (mReceiverList) {
            if (mTerminated) {
                return;
            }
            mReceiverList.add(receiver);
        }
    }

    public void unregisterReceiver(Receiver receiver) {
        synchronized (mReceiverList) {
            mReceiverList.remove(receiver);
        }
    }

    public void terminate() {
        mTerminated = true;
        synchronized (mReceiverList) {
            mReceiverList.clear();
        }
    }

    public boolean isTerminated() {
        return mTerminated;
    }

    void dispatchMessage(Message message) {
        Receiver[] receivers = collectReceiver();
        for (Receiver receiver : receivers) {
            if (mTerminated) {
                return;
            }
            receiver.onMessage(message);
        }
    }

    private Receiver[] collectReceiver() {
        Receiver[] receivers = new Receiver[]{};
        synchronized (mReceiverList) {
            receivers = mReceiverList.toArray(receivers);
        }
        return receivers;
    }

    public interface Receiver {
        void onMessage(Message message);
    }
}
