package com.cloud.common.transfer;

import java.io.IOException;
import java.nio.file.Path;

public class DeliveryPackage extends FileMessage {
    private int partsCount;
    private int partNumber;

    public DeliveryPackage(Path path, String userName, int partNumber, int partsCount) throws IOException {
        super(path, userName);
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
