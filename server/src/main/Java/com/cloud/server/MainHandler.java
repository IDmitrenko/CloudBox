package com.cloud.server;

import com.cloud.common.transfer.FileMessage;
import com.cloud.common.utils.FileAbout;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Files;
import java.nio.file.Paths;

public class MainHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) {
                return;
            }
            if (msg instanceof FileAbout) {
                // получаем сообщение (объект)
                FileAbout fa = (FileAbout) msg;
                if (Files.exists(Paths.get("server_storage/" + fa.getName()))) {
                    FileMessage fm = new FileMessage(Paths.get("server_storage/" + fa.getName()));
                    // отправляем запрошенный файл с сервера (ответ)
                    ctx.writeAndFlush(fa);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
