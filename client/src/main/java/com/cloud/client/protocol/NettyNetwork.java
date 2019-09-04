package com.cloud.client.protocol;

import com.cloud.client.AuthException;
import com.cloud.client.BigFileProgressBar;
import com.cloud.client.ListFileReciever;
import com.cloud.client.MainWindow;
import com.cloud.common.transfer.AbstractMessage;
import com.cloud.common.transfer.AuthMessage;
import com.cloud.common.transfer.BigFileMessage;
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

import javax.swing.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class NettyNetwork {
    private volatile boolean isAuth = false;

    private ListFileReciever listFileReciever;

    public void setListFileReciever(ListFileReciever listFileReciever) {
        this.listFileReciever = listFileReciever;
    }

    private JFrame mainFrame;

    public void setMainFrame(JFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    private ExecutorService executorService;
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
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
    private Object[][] arrClient;
    private static final int maxObjectSize = 101 * 1024 * 1024;
    private static final int largeFileSize = 1024 * 1024 * 100;
    private static final String rootPath = "client/repository";

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
                            new ClientHandler(ourInstance, executorService)
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
            if (isConnectionOpened()) {
                currentChannel.writeAndFlush(msg).await();
            }
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
        FileOutputStream fos = new FileOutputStream(rootPath + "/" + msg.getFilename());
        fos.write(msg.getData());
        fos.flush();
        fos.close();
        clientListFile();
    }

    public void writeBigFileMessage(ChannelHandlerContext ctx, BigFileMessage msg) throws IOException, InterruptedException {
        int partNumber = msg.getPartNumber();
        final BigFileProgressBar bfbp = new BigFileProgressBar(mainFrame);
        if (partNumber == 0) {
            deleteFile(msg.getFilename());
            bfbp.setPreviousValue(0);
        }
        File file = new File(rootPath + "/" + msg.getFilename());
        RandomAccessFile ra = new RandomAccessFile(file, "rw");
        ra.seek(file.length());
        ra.write(msg.getData());
        ra.close();
        partNumber++;
        final int setValue = (100 * partNumber) / msg.getPartsCount();
        if (setValue > bfbp.getPreviousValue()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    bfbp.setProgressBar(setValue);
                }
            });
            bfbp.setPreviousValue(setValue);
        }
        if (msg.getPartsCount() == partNumber) {
            TimeUnit.SECONDS.sleep(1L);
            bfbp.close();
            clientListFile();
        }
    }

    public void deleteFile(String nameFile) {
        Path path = Paths.get(rootPath + "/" + nameFile);
        if (nameFile != null && !nameFile.trim().isEmpty() && Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        clientListFile();
    }

    public boolean bigFile(Path path) {
        return path.toFile().length() > largeFileSize;
    }

    public void sendBigFile(Path path) throws IOException, InterruptedException {
        final BigFileProgressBar bfpb = new BigFileProgressBar(mainFrame);
        long fileSize = path.toFile().length();
        int bytesIn1mb = largeFileSize;
        int currentPosition = 0;
        int partNumber = 0;
        bfpb.setPreviousValue(0);
        int partsCount = (int) (fileSize / (bytesIn1mb));
        RandomAccessFile ra = new RandomAccessFile(path.toString(), "r");
        while (currentPosition < fileSize) {
            byte[] data = new byte[Math.min(bytesIn1mb, (int) (fileSize - currentPosition))];
            ra.seek(currentPosition);
            int readBytes = ra.read(data);
            BigFileMessage filePart = new BigFileMessage(path, MainWindow.getUserName(), partNumber, partsCount, data);
            sendMsg(filePart);
            partNumber++;
            currentPosition += readBytes;
            final int setValue = (100 * partNumber) / partsCount;
            if (setValue > bfpb.getPreviousValue()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        bfpb.setProgressBar(setValue);
                    }
                });
                bfpb.setPreviousValue(setValue);
            }
            if (partNumber == partsCount) {
                TimeUnit.SECONDS.sleep(1L);
                bfpb.close();
            }
        }
    }

    public void sendSmallFile(Path path) throws IOException {
        FileMessage fm = new FileMessage(path, MainWindow.getUserName());
        sendMsg(fm);
    }

    public void clientListFile() {
        FileListMessage fll = getListFileClient();
        List<FileAbout> filesList = fll.getFilesList();
        rows = filesList.size();
        if (rows == 0) {
            rows = 1;
            arrClient = new String[rows][cols];
            arrClient[0][0] = "     ";
            arrClient[0][1] = "     ";
        } else {
            arrClient = new String[rows][cols];
            for (int i = 0; i < rows; i++) {
                arrClient[i][0] = filesList.get(i).getName();
                arrClient[i][1] = String.valueOf(filesList.get(i).getSize()) + " bytes";
            }
        }
        listFileReciever.updateFileListLocal(arrClient);
    }

    public String getClientRootPath() {
        Path path = Paths.get(rootPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return path.toString();
    }

    public FileListMessage getListFileClient() {
        FileListMessage fll = null;
        try {
            fll = new FileListMessage(Paths.get(getClientRootPath()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return fll;
    }

    public boolean isConnectionOpened() {
        return currentChannel != null && currentChannel.isActive();
    }

    public void closeConnection() {
        currentChannel.close();
    }
}
