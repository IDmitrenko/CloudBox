package com.cloud.client.protocol;

import com.cloud.client.AuthException;
import com.cloud.client.ListFileReciever;
import com.cloud.common.transfer.AbstractMessage;
import com.cloud.common.transfer.AuthMessage;
import com.cloud.common.transfer.FileListMessage;
import com.cloud.common.transfer.FileMessage;
import com.cloud.common.utils.FileAbout;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

public class NettyNetwork {
    private volatile boolean isAuth = false;

    private ListFileReciever listFileReciever;

    public void setListFileReciever(ListFileReciever listFileReciever) {
        this.listFileReciever = listFileReciever;
    }

    public NettyNetwork() {

    }

    private Object lock = new Object();

    private static NettyNetwork ourInstance = new NettyNetwork();

    public static NettyNetwork getOurInstance() {
        return ourInstance;
    }

    private Channel currentChannel;

    private static int cols = 2;
    private int rows;
    private Object[][] arrServer;
    private static final int maxObjectSize = 101 * 1024 * 1024;

    public Channel getCurrentChannel() {
        return currentChannel;
    }

    public void start() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group);
            clientBootstrap.channel(NioSocketChannel.class);
            clientBootstrap.remoteAddress(new InetSocketAddress("127.0.0.1", 8189));
            clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(
                            new ObjectDecoder(maxObjectSize, ClassResolvers.cacheDisabled(null)),
                            new ObjectEncoder(),
                            new ClientHandler(ourInstance)
                    );
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

    public void authorize(AuthMessage am) throws IOException,
            AuthException, InterruptedException {
        sendMsg(am);
        synchronized (lock) {
            lock.wait();
        }
        if (!isAuth) {
            throw new AuthException("Клиент " + am.getLogin() + " в системе не зарегистрирован!");
        }
    }

    public void waitAuthorize(boolean isAuthServer) {
        isAuth = isAuthServer;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    /**
     * Method to send message through network.
     *
     * @param msg AbstractMessage
     */
    public void sendMsg(final AbstractMessage msg) {
        try {
            currentChannel.writeAndFlush(msg).await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void updateFileListServer(FileListMessage fls) {
        List<FileAbout> fileList = fls.getFilesList();
        rows = fileList.size();
        if (rows == 0) {
            rows = 1;
            arrServer = new String[rows][cols];
            arrServer[0][0] = "     ";
            arrServer[0][1] = "     ";
        } else {
            arrServer = new String[rows][cols];
            for (int i = 0; i < rows; i++) {
                arrServer[i][0] = fileList.get(i).getName();
                arrServer[i][1] = String.valueOf(fileList.get(i).getSize()) + " bytes";
            }
        }
        listFileReciever.updateFileListServer(arrServer);
    }

    public void writeFileMessage(ChannelHandlerContext ctx, FileMessage msg) throws IOException {
        FileMessage fm = msg;
        FileOutputStream fos = new FileOutputStream(listFileReciever.getRootpath() + "/" + fm.getFilename());
        fos.write(fm.getData());
        fos.flush();
        fos.close();
// обновить файлы на клиенте
    }

    public boolean isConnectionOpened() {
        return currentChannel != null && currentChannel.isActive();
    }

    public void closeConnection() {
        currentChannel.close();
    }
}
