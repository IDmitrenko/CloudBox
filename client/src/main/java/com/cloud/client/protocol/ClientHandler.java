package com.cloud.client.protocol;

import com.cloud.common.transfer.CommandMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg == null) {
                return;
            }
            if (msg instanceof CommandMessage) {
                CommandMessage cm = (CommandMessage) msg;
                // авторизация прошла успешно
                if (cm.getType() == CommandMessage.CMD_MSG_AUTH_OK) {


                } else if (cm.getType() == CommandMessage.CMD_MSG_AUTH_NOT) {

                }
                // Загрузить файл с сервера
                if (cm.getType() == CommandMessage.CMD_MSG_REQUEST_FILE_DOWNLOAD) {
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
