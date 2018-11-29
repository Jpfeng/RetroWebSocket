package com.jpfeng.websocket.launcher;

import com.jpfeng.websocket.LaunchPad;
import com.jpfeng.websocket.Message;
import com.jpfeng.websocket.MessageLauncher;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;


/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/28
 */
public class RxJava2LauncherFactory extends MessageLauncher.Factory {

    private Scheduler mScheduler;

    private RxJava2LauncherFactory(Scheduler scheduler) {
        this.mScheduler = scheduler;
    }

    public static RxJava2LauncherFactory create() {
        return new RxJava2LauncherFactory(null);
    }

    public static RxJava2LauncherFactory createWithScheduler(Scheduler scheduler) {
        if (scheduler == null) {
            throw new NullPointerException("scheduler == null");
        }
        return new RxJava2LauncherFactory(scheduler);
    }

    @Override
    public MessageLauncher<?> get(Type returnType, LaunchPad launchPad) {
        Class<?> rawType = getRawType(returnType);

        if (Completable.class == rawType || Single.class == rawType || Maybe.class == rawType) {
            // Completable 不会发射数据，Single 和 Maybe 只发射一次数据，都不适合用于此处。
            throw new IllegalStateException(rawType.getName() + "is not suitable for webSocket, " +
                    "please use Flowable or Observable.");
        }

        boolean isFlowable = Flowable.class == rawType;
        if (Observable.class != rawType && !isFlowable) {
            return null;
        }

        if (returnType instanceof ParameterizedType) {
            // 判断泛型是 <Message> 
            Type argumentType = getParameterUpperBound(0, (ParameterizedType) returnType);
            if (Message.class == argumentType) {
                return new RxJava2Launcher(mScheduler, isFlowable, launchPad);
            }
        }

        String name = isFlowable ? "Flowable" : "Observable";
        throw new IllegalStateException(name + " return type must be parameterized as "
                + name + "<Message> or " + name + "<? extends Message>");
    }
}
