package com.jpfeng.websocket.launcher;

import com.jpfeng.websocket.LaunchPad;
import com.jpfeng.websocket.Message;
import com.jpfeng.websocket.MessageLauncher;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Scheduler;

/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/28
 */
public class RxJava2Launcher implements MessageLauncher<Object> {

    private final Scheduler mScheduler;
    private final boolean mIsFlowable;
    private final LaunchPad mLaunchPad;
    private ObservableEmitter<Message> mEmitter;

    RxJava2Launcher(Scheduler scheduler, boolean isFlowable, LaunchPad launchPad) {
        mScheduler = scheduler;
        mIsFlowable = isFlowable;
        mLaunchPad = launchPad;
    }

    @Override
    public Object prepare() {
        Observable<Message> observable = Observable.create(emitter -> {
            mEmitter = emitter;
            emitter.setCancellable(this::tearDown);
        });

        if (mScheduler != null) {
            observable = observable.subscribeOn(mScheduler);
        }

        mLaunchPad.add(this);

        if (mIsFlowable) {
            return observable.toFlowable(BackpressureStrategy.BUFFER);
        }
        return observable;
    }

    @Override
    public void launchMessage(Message message) {
        mEmitter.onNext(message);
    }

    @Override
    public void tearDown() {
        mLaunchPad.remove(this);
    }
}
