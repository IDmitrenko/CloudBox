package com.cloud.common.transfer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends AbstractMessage {
    private String filename;
    private byte[] data;
    private String userName;

    public FileMessage(String s, byte[] bytes, int partsCount, int i) {

    }

//    private int partsCount;
//    private int partNumber;

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }

    public String getUserName() {
        return userName;
    }

    /*
    public int getPartsCount() {
        return partsCount;
    }

    public int getPartNumber() {
        return partNumber;
    }
*/

    public FileMessage(Path path, byte[] data) throws IOException {
        this.filename = path.getFileName().toString();
        this.data = data;
    }

    public FileMessage(Path path) throws IOException {
        this.filename = path.getFileName().toString();
        this.data = Files.readAllBytes(path);
    }

    public FileMessage(Path path, String userName) throws IOException {
        this.filename = path.getFileName().toString();
        this.data = Files.readAllBytes(path);
        this.userName = userName;
    }
}
