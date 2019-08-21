package com.cloud.common.utils;

import com.cloud.common.transfer.FileMessage;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class FilePartitionWorker {
    public static final int PART_SIZE = 8192;

    public static void sendFileFromClient(Path path, ObjectEncoderOutputStream out, ProgressBar progressBar) {
        try {
            byte[] fileData = Files.readAllBytes(path);
            int partsCount = fileData.length / PART_SIZE;
            if (fileData.length % PART_SIZE != 0) {
                partsCount++;
            }
            if (progressBar != null) {
                Platform.runLater(() -> {
                    progressBar.setVisible(true);
                    progressBar.setManaged(true);
                });
            }
            for (int i = 0; i < partsCount; i++) {
                int startPosition = i * PART_SIZE;
                int endPosition = (i + 1) * PART_SIZE;
                if (endPosition > fileData.length) {
                    endPosition = fileData.length;
                }
                if (progressBar != null) {
                    final double prog = (double) i / partsCount;
                    Platform.runLater(() -> progressBar.setProgress(prog));
                }
                FileMessage fm = new FileMessage(path.getFileName().toString(),
                        Arrays.copyOfRange(fileData, startPosition, endPosition),
                        partsCount, i);
                out.writeObject(fm);
                out.flush();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (progressBar != null) {
                Platform.runLater(() -> {
                    progressBar.setVisible(true);
                    progressBar.setManaged(true);
                });
            }
        }
    }

    public static void sendFileFromServer(Path path, Channel out) {
        try {
            byte[] fileData = Files.readAllBytes(path);
            int partsCount = fileData.length / PART_SIZE;
            if (fileData.length % PART_SIZE != 0) {
                partsCount++;
            }
            for (int i = 0; i < partsCount; i++) {
                int startPosition = i * PART_SIZE;
                int endPosition = (i +1) * PART_SIZE;
                if (endPosition > fileData.length) {
                    endPosition = fileData.length;
                }
                FileMessage fm = new FileMessage(path.getFileName().toString(),
                        Arrays.copyOfRange(fileData, startPosition, endPosition), partsCount, i);
                ChannelFuture future = out.writeAndFlush(fm);
                future.await();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public static void savePartToFile(String rootPath, FileMessage fileMessage, FileWritingFinally finalAction) {
        RandomAccessFile raf = null;
        FileChannel outChannel = null;
        try {
            Path path = Paths.get(rootPath + fileMessage.getFilename());
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            raf = new RandomAccessFile(path.toFile(), "rw");
            outChannel = raf.getChannel();
            outChannel.position(fileMessage.getPartNumber() * FilePartitionWorker.PART_SIZE);
            ByteBuffer buf = ByteBuffer.allocate(fileMessage.getData().length);
            buf.put(fileMessage.getData());
            buf.flip();
            outChannel.write(buf);
            buf.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                outChannel.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                raf.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (fileMessage.getPartNumber() + 1 == fileMessage.getPartsCount()) {
            finalAction.action();
        }
    }
}
