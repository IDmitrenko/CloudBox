package com.cloud.common.transfer;

import java.io.IOException;
import java.nio.file.Path;

public class BigFileMessage extends FileMessage {
    private int partsCount;
    private int partNumber;

    public BigFileMessage(Path path, String userName, int partNumber, int partsCount, byte[] data) throws IOException {
        super(path, userName, data);
        this.partsCount = partsCount;
        this.partNumber = partNumber;
    }

    public int getPartsCount() {
        return partsCount;
    }

    public int getPartNumber() {
        return partNumber;
    }
}
