package com.cloud.client.protocol;

import com.cloud.common.transfer.CommandMessage;
import com.cloud.common.transfer.FileListMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

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
                // TODO Загрузить файл с сервера
                if (cm.getType() == CommandMessage.CMD_MSG_REQUEST_FILE_DOWNLOAD) {

                }
            }

            if (msg instanceof FileListMessage) {
                FileListMessage flm = (FileListMessage) msg;
                // пришел список файлов клиента на сервере
                nettyNetwork.updateFileListServer(flm);
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
