package com.toocol.ssh.core.term.handlers;

import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.utils.Tuple2;
import com.toocol.ssh.core.term.commands.TermioCommand;
import com.toocol.ssh.core.term.core.Printer;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.term.TermAddress.EXECUTE_OUTSIDE;


/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:09
 */
public final class ExecuteCommandHandler extends AbstractMessageHandler {

    public ExecuteCommandHandler(Vertx vertx, Context context) {
        super(vertx, context);
    }

    @Override
    public IAddress consume() {
        return EXECUTE_OUTSIDE;
    }

    @Override
    public <T> void handle(Message<T> message) {
        String cmd = String.valueOf(message.body());
        Tuple2<Boolean, String> resultAndMessage = new Tuple2<>();
        TermioCommand.cmdOf(cmd)
                .ifPresent(termioCommand -> {
                    try {
                        termioCommand.processCmd(eventBus, cmd, resultAndMessage);
                    } catch (Exception e) {
                        Printer.printErr("Execute command failed, message = " + e.getMessage());
                    }
                });
        message.reply(resultAndMessage._2());
    }
}