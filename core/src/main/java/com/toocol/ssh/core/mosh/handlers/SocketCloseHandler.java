package com.toocol.ssh.core.mosh.handlers;

import com.toocol.ssh.core.cache.MoshSessionCache;
import com.toocol.ssh.core.mosh.core.MoshSession;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.handler.NonBlockingMessageHandler;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import java.util.Optional;

import static com.toocol.ssh.core.mosh.MoshAddress.CLOSE_LOCAL_SOCKET;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/30 20:18
 * @version: 0.0.1
 */
public final class SocketCloseHandler extends NonBlockingMessageHandler {

    private final MoshSessionCache moshSessionCache = MoshSessionCache.getInstance();

    public SocketCloseHandler(Vertx vertx, Context context) {
        super(vertx, context);
    }

    @Override
    public <T> void handleInline(Message<T> message) {
        Optional.ofNullable(moshSessionCache.get(cast(message.body())))
                .ifPresent(MoshSession::close);
    }

    @Override
    public IAddress consume() {
        return CLOSE_LOCAL_SOCKET;
    }
}
