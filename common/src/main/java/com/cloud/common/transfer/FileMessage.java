package com.cloud.common.transfer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends AbstractMessage {
    private String filename;
    private byte[] data;
    private String userName;

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
    public FileMessage(Path path) throws IOException {
        this.filename = path.getFileName().toString();
        this.data = Files.readAllBytes(path);
    }

    public FileMessage(Path path, String userName) throws IOException {
        this.filename = path.getFileName().toString();
        this.data = Files.readAllBytes(path);
        this.userName = userName;
    }

    public FileMessage(String filename, byte[] data, int partsCount, int partNumber) throws IOException {
        this.filename = filename;
        this.data = data;
/*
        this.partsCount = partsCount;
        this.partNumber = partNumber;
*/
    }
}
