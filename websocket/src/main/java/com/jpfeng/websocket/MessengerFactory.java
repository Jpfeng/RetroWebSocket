package com.jpfeng.websocket;

import java.lang.reflect.Type;

/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/28
 */
public class MessengerFactory extends MessageLauncher.Factory {
    @Override
    public MessageLauncher<?> get(Type returnType, LaunchPad launchPad) {
        if (Messenger.class != Utils.getRawType(returnType)) {
            return null;
        }

        return new MessageLauncher<Messenger>() {
            Messenger mMessenger = new Messenger() {
                @Override
                public void terminate() {
                    super.terminate();
                    tearDown();
                }
            };

            @Override
            public Messenger prepare() {
                launchPad.add(this);
                return mMessenger;
            }

            @Override
            public void launchMessage(Message message) {
                mMessenger.dispatchMessage(message);
            }

            @Override
            public void tearDown() {
                launchPad.remove(this);
            }
        };
    }
}
