package com.cloud.server;

import com.cloud.common.transfer.AuthMessage;
import com.cloud.common.transfer.CommandMessage;
import com.cloud.common.transfer.FileMessage;
import com.cloud.common.utils.FileAbout;
import com.cloud.common.utils.FilePartitionWorker;
import com.cloud.server.protocol.LoginMap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(MainHandler.class.getName());
    private static final String rootPath = "server/repository/";
    private String clientName;
    private boolean authorized;

    public MainHandler(String clientName) {
        this.clientName = clientName;
    }

    public MainHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg != null) {

                if (msg instanceof FileAbout)
                {
                    readFileAbout(ctx, (FileAbout) msg);
                }

                if (msg instanceof FileMessage)
                {
                    writeFileMessage(ctx, (FileMessage) msg);
                }

                if (!authorized) {
                    if (msg instanceof AuthMessage)
                    {
                        authentication(ctx, (AuthMessage) msg);
                    }
                }

                if (msg instanceof CommandMessage)
                {
                    readCommandMessage(ctx, (CommandMessage) msg);
                } else {
                    ctx.fireChannelRead(msg);
                }

            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void writeFileMessage(ChannelHandlerContext ctx, FileMessage msg) throws IOException {
        // получаем файл от клиента
        FileMessage fm = msg;
        FileOutputStream fos = new FileOutputStream(rootPath + clientName + "/" + fm.getFilename());
        fos.write(fm.getData());
        fos.close();
        ServerUtilities.sendFileList(ctx.channel(), clientName);

/*
        // Write the content. https://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/http/file/package-summary.html
        ChannelFuture writeFuture;
//        ch = ctx.channel().;
        if (ch.getPipeline().get(SslHandler.class) != null) {
            // Cannot use zero-copy with HTTPS.
            writeFuture = ch.write(new ChunkedFile(raf, 0, fileLength, 8192));
         } else {
             // No encryption - use zero-copy.
             final FileRegion region =
                 new DefaultFileRegion(raf.getChannel(), 0, fileLength);
             writeFuture = ch.write(region);
             writeFuture.addListener(new ChannelFutureProgressListener() {
                 public void operationComplete(ChannelFuture future) {
                     region.releaseExternalResources();
                 }

                 public void operationProgressed(
                         ChannelFuture future, long amount, long current, long total) {
                     System.out.printf("%s: %d / %d (+%d)%n", path, current, total, amount);
                 }
             });
         }
*/
    }

    private void authentication(ChannelHandlerContext ctx, AuthMessage msg) throws InterruptedException {
        final AuthMessage am = msg;
        final String login = am.getLogin();
        final String password = am.getPassword();
        String nick = LoginMap.getUser().get(login);
        if (nick != null && password.equals(nick)) {
            authorized = true;
            CommandMessage amAuthOk = new CommandMessage(CommandMessage.CMD_MSG_AUTH_OK);
            ChannelFuture future = ctx.writeAndFlush(amAuthOk).await();
            this.clientName = login;
            ServerUtilities.sendFileList(ctx.channel(), login);
        } else {
            CommandMessage amAuthNot = new CommandMessage(CommandMessage.CMD_MSG_AUTH_NOT);
            ChannelFuture future = ctx.writeAndFlush(amAuthNot).await();
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
