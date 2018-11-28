package com.jpfeng.websocket;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/28
 */
public class LaunchPad {

    private final List<MessageLauncher> mLaunchers;

    LaunchPad() {
        mLaunchers = new ArrayList<>();
    }

    public void add(MessageLauncher launcher) {
        synchronized (mLaunchers) {
            mLaunchers.add(launcher);
        }
    }

    public void remove(MessageLauncher launcher) {
        synchronized (mLaunchers) {
            mLaunchers.remove(launcher);
        }
    }

    void launch(Message message) {
        MessageLauncher[] launchers = collectLaunchers();
        for (MessageLauncher launcher : launchers) {
            launcher.launchMessage(message);
        }
    }

    private MessageLauncher[] collectLaunchers() {
        MessageLauncher[] launchers = new MessageLauncher[]{};
        synchronized (mLaunchers) {
            if (mLaunchers.size() > 0) {
                launchers = mLaunchers.toArray(launchers);
            }
        }
        return launchers;
    }
}
