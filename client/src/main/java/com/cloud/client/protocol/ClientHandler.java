package com.cloud.client.protocol;

import com.cloud.common.transfer.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(ClientHandler.class.getName());

    private NettyNetwork nettyNetwork;
    private ExecutorService executorService;

    public ClientHandler(NettyNetwork nettyNetwork, ExecutorService executorService) {
        this.nettyNetwork = nettyNetwork;
        this.executorService = executorService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg != null) {

                if (msg instanceof CommandMessage) {
                    CommandMessage cm = (CommandMessage) msg;
                    if (cm.getType() == CommandMessage.CMD_MSG_AUTH_OK) {
                        nettyNetwork.waitAuthorize(true);
                    } else if (cm.getType() == CommandMessage.CMD_MSG_AUTH_NOT) {
                        nettyNetwork.waitAuthorize(false);
                    }
                }

                if (msg instanceof BigFileMessage) {
                    executorService.submit(() -> {
                        try {
                            nettyNetwork.writeBigFileMessage(ctx, (BigFileMessage) msg);
                        } catch (IOException | InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    });
                } else if (msg instanceof FileMessage) {
                    nettyNetwork.writeFileMessage(ctx, (FileMessage) msg);
                }

                if (msg instanceof DeliveryPackage) {
                    DeliveryPackage dp = (DeliveryPackage) msg;
                    int partNumber = dp.getPartNumber();
                    logger.info("Пришло подтверждение сервера о приеме части " +
                            partNumber + " файла " + dp.getFileName());
                    nettyNetwork.waitingPackageDelivery(true);
                }

                if (msg instanceof FileListMessage) {
                    FileListMessage flm = (FileListMessage) msg;
                    nettyNetwork.updateFileListServer(flm);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

/*
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
*/

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }

/*
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }
*/
}
