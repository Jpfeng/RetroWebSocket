package com.jpfeng.retrowebsocket.model;

import com.jpfeng.websocket.Messenger;
import com.jpfeng.websocket.annotation.Listen;

/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/28
 */
interface MainApi {
    @Listen
    Messenger listen();
}
