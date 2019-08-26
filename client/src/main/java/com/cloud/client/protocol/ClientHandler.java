package com.cloud.client.protocol;

import com.cloud.common.transfer.AuthMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg == null) {
                return;
            }
            if (msg instanceof AuthMessage) {

            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
