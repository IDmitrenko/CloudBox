package com.cloud.server;

import com.cloud.common.transfer.AuthMessage;
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(MainHandler.class.getName());
    private String clientName;
    private boolean authorized = false;

    public MainHandler(String clientName) {
        this.clientName = clientName;
    }

    public MainHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) {
                return;
            }

            if (msg instanceof FileAbout) {
                // получаем сообщение (объект) имя файла
                FileAbout fa = (FileAbout) msg;
                if (Files.exists(Paths.get("server/repository/" + fa.getName()))) {
                    FileMessage fm = new FileMessage(Paths.get("server/repository/" + fa.getName()));
                    // отправляем запрошенный файл с сервера (ответ)
                    ctx.writeAndFlush(fa);
                }
            }

            if (msg instanceof FileMessage) {
                // получаем файл от клиента

            }

            if (msg instanceof CommandMessage) {
                CommandMessage cm = (CommandMessage) msg;
                if (cm.getType() == CommandMessage.CMD_MSG_REQUEST_FILES_LIST) {
                    ServerUtilities.sendFileList(ctx.channel(), clientName);
                }
                if (cm.getType() == CommandMessage.CMD_MSG_REQUEST_SERVER_DELETE_FILE) {
                    Path pathA = Paths.get(((File) cm.getAttachment()[0]).getAbsolutePath());

                    Path path = Paths.get("server/repository/" + clientName + "/" + pathA.getFileName());
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

            if (!authorized) {
                if (msg instanceof AuthMessage) {
                    AuthMessage am = (AuthMessage) msg;
                    String login = am.getLogin();
                    String password = am.getPassword();
                    String nick = LoginMap.getUser().get(login);
                    if (nick != null && password.equals(nick)) {
                        authorized = true;
                        CommandMessage amAuthOk = new CommandMessage(CommandMessage.CMD_MSG_AUTH_OK);
                        ChannelFuture future = ctx.writeAndFlush(amAuthOk).await();
                        this.clientName = login;
                        ServerUtilities.sendFileList(ctx.channel(), login);
                        // ctx.pipeline().addLast(new ServerHandler(login));
                    } else {
                        CommandMessage amAuthNot = new CommandMessage(CommandMessage.CMD_MSG_AUTH_NOT);
                        ChannelFuture future = ctx.writeAndFlush(amAuthNot).await();
//                        ReferenceCountUtil.release(msg);
                    }
                } else {
                    ctx.fireChannelRead(msg);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
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
