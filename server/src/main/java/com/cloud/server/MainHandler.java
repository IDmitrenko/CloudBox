package com.cloud.server;

import com.cloud.common.transfer.AuthMessage;
import com.cloud.common.transfer.BigFileMessage;
import com.cloud.common.transfer.CommandMessage;
import com.cloud.common.transfer.FileMessage;
import com.cloud.common.utils.FileAbout;
import com.cloud.common.utils.FilePartitionWorker;
import com.cloud.server.protocol.LoginMap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(MainHandler.class.getName());
    private static final String rootPath = "server/repository/";
    private String clientName;
    private boolean authorized;
    private boolean isProgressBar;

    public MainHandler(String clientName) {
        this.clientName = clientName;
    }

    public MainHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg != null) {

                if (msg instanceof FileAbout) {
                    readFileAbout(ctx, (FileAbout) msg);
                }

                if (msg instanceof BigFileMessage) {
                    writeBigFileMessage(ctx, (BigFileMessage) msg);
                } else if (msg instanceof FileMessage) {
                    writeFileMessage(ctx, (FileMessage) msg);
                }

                if (!authorized) {
                    if (msg instanceof AuthMessage) {
                        authentication(ctx, (AuthMessage) msg);
                    }
                }

                if (msg instanceof CommandMessage) {
                    readCommandMessage(ctx, (CommandMessage) msg);
                } else {
                    ctx.fireChannelRead(msg);
                }

            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void writeBigFileMessage(ChannelHandlerContext ctx, BigFileMessage msg) throws IOException {
        File file = new File(rootPath + clientName + "/" + msg.getFilename());
        RandomAccessFile ra = new RandomAccessFile(file, "rw");
        if (msg.getPartNumber() == 0) {
            ra.seek(0);
        } else {
            ra.seek(file.length());
        }
        ra.write(msg.getData());
        ra.close();
        ServerUtilities.sendFileList(ctx.channel(), clientName);
    }

    private void writeFileMessage(ChannelHandlerContext ctx, FileMessage msg) throws IOException {
        // получаем файл от клиента
        FileMessage fm = msg;
        FileOutputStream fos = new FileOutputStream(rootPath + clientName + "/" + fm.getFilename());
        fos.write(fm.getData());
        fos.flush();
        fos.close();
        ServerUtilities.sendFileList(ctx.channel(), clientName);
    }

    private void authentication(ChannelHandlerContext ctx, AuthMessage msg) throws InterruptedException {
        final AuthMessage am = msg;
        final String login = am.getLogin();
        final String password = am.getPassword();
        String nick = LoginMap.getUser().get(login);
        if (nick != null && password.equals(nick)) {
            authorized = true;
            CommandMessage amAuthOk = new CommandMessage(CommandMessage.CMD_MSG_AUTH_OK);
            ctx.writeAndFlush(amAuthOk).await();
            this.clientName = login;
            ServerUtilities.sendFileList(ctx.channel(), login);
        } else {
            CommandMessage amAuthNot = new CommandMessage(CommandMessage.CMD_MSG_AUTH_NOT);
            ctx.writeAndFlush(amAuthNot).await();
//                        ReferenceCountUtil.release(msg);
        }
    }

    private void readCommandMessage(ChannelHandlerContext ctx, CommandMessage msg) throws IOException {
        CommandMessage cm = msg;
        if (cm.getType() == CommandMessage.CMD_MSG_REQUEST_FILES_LIST) {
            ServerUtilities.sendFileList(ctx.channel(), clientName);
        }
        if (cm.getType() == CommandMessage.CMD_MSG_REQUEST_SERVER_DELETE_FILE) {
            Path pathA = Paths.get(((File) cm.getAttachment()[0]).getAbsolutePath());

            Path path = Paths.get(rootPath + clientName + "/" + pathA.getFileName());
            Files.delete(path);
            ServerUtilities.sendFileList(ctx.channel(), clientName);
        }
        if (cm.getType() == CommandMessage.CMD_MSG_REQUEST_FILE_DOWNLOAD) {
            try {
                Path path = Paths.get(((File) cm.getAttachment()[0]).getAbsolutePath());
                FilePartitionWorker.sendFileFromServer(path, ctx.channel());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void readFileAbout(ChannelHandlerContext ctx, FileAbout msg) throws IOException {
        // получаем сообщение (объект) имя файла
        FileAbout fa = msg;
        if (Files.exists(Paths.get(rootPath + fa.getName()))) {
            FileMessage fm = new FileMessage(Paths.get(rootPath + fa.getName()));
            // отправляем запрошенный файл с сервера (ответ)
            ctx.writeAndFlush(fa);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
