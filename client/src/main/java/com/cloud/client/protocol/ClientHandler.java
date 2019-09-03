package com.cloud.client.protocol;

import com.cloud.common.transfer.BigFileMessage;
import com.cloud.common.transfer.CommandMessage;
import com.cloud.common.transfer.FileListMessage;
import com.cloud.common.transfer.FileMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.FileOutputStream;
import java.io.IOException;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private NettyNetwork nettyNetwork;

    public ClientHandler(NettyNetwork nettyNetwork) {
        this.nettyNetwork = nettyNetwork;
    }

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
                    nettyNetwork.waitAuthorize(true);
                } else if (cm.getType() == CommandMessage.CMD_MSG_AUTH_NOT) {
                    nettyNetwork.waitAuthorize(false);
                }
            }

            if (msg instanceof BigFileMessage) {
                nettyNetwork.writeBigFileMessage(ctx, (BigFileMessage) msg);
            } else if (msg instanceof FileMessage) {
                nettyNetwork.writeFileMessage(ctx, (FileMessage) msg);
            }

            if (msg instanceof FileListMessage) {
                FileListMessage flm = (FileListMessage) msg;
                nettyNetwork.updateFileListServer(flm);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
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
