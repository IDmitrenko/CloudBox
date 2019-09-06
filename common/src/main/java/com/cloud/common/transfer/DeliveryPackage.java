package com.cloud.common.transfer;

import java.io.IOException;
import java.nio.file.Path;

public class DeliveryPackage extends AbstractMessage {
    private int partsCount;
    private int partNumber;
    private String fileName;
    private String userName;

    public DeliveryPackage(String fileName, String userName, int partNumber, int partsCount) throws IOException {
        this.partsCount = partsCount;
        this.partNumber = partNumber;
        this.fileName = fileName;
        this.userName = userName;
    }

    public int getPartsCount() {
        return partsCount;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public String getUserName() {
        return userName;
    }
}
