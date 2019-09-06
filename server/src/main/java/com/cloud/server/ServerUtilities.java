package com.cloud.server;

import com.cloud.common.transfer.FileListMessage;
import com.cloud.server.protocol.MainHandler;
import io.netty.channel.Channel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerUtilities {
    public static void sendFileList(Channel channel, String username) {
        FileListMessage flm = null;
        try {
            flm = new FileListMessage(Paths.get(getUserRootPath(username)));
            channel.writeAndFlush(flm);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String getUserRootPath(String username) {
        Path path = Paths.get(MainHandler.rootPath + username);
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return path.toString();
    }
}
