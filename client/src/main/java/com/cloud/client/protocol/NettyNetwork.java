package com.cloud.client.protocol;

import com.cloud.common.transfer.AuthMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;

public class NettyNetwork {
    private static NettyNetwork ourInstance = new NettyNetwork();

    public static NettyNetwork getOurInstance() {
        return ourInstance;
    }

    public NettyNetwork() {
    }

    private Channel currentChannel;

    public Channel getCurrentChannel() {
        return currentChannel;
    }

    public void start() {
        EventLoopGroup group = new NioEventLoopGroup();
        // на клиенте исрользуется один объект EventLoopGroup
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group);
            clientBootstrap.channel(NioSocketChannel.class);
            clientBootstrap.remoteAddress(new InetSocketAddress("localhost", 8189));
            clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    // получение и обработка ответа
                    socketChannel.pipeline().addLast(
                            new ClientHandler()
                    );
                    // TODO нужно добавить Handler для получения ответа
                    // channel для обмена между сервером и клиентом
                    currentChannel = socketChannel;


                }
            });
            ChannelFuture channelFuture = clientBootstrap.connect().sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void sendData() {
        ByteBufAllocator allocator = new PooledByteBufAllocator();
        ByteBuf buf = allocator.buffer(16);

        for (int i = 65; i < 75; i++) {
            for (int j = 0; j < 4; j++) {
                if (buf.isWritable()) {
                    buf.writeByte(i);
                } else {
                    try {
                        currentChannel.writeAndFlush(buf).await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    buf.clear();
                    buf.retain();
                }
            }
        }

    }

    public void authorize(AuthMessage am) throws IOException {
        ByteBufAllocator allocator = new PooledByteBufAllocator();
        ByteBuf buf = allocator.buffer(16);

        buf.writeInt(am.getLogin().length());
        buf.writeBytes(am.getLogin().getBytes());

        buf.writeInt(am.getPassword().length());
        buf.writeBytes(am.getPassword().getBytes());

        try {
            currentChannel.writeAndFlush(buf).await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

    }

    public boolean isConnectionOpened() {
        return currentChannel != null && currentChannel.isActive();
    }

    public void closeConnection() {
        currentChannel.close();
    }
}
