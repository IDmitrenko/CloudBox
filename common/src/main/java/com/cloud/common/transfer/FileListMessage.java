package com.cloud.common.transfer;

import com.cloud.common.utils.FileAbout;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class FileListMessage extends AbstractMessage {
    private List<FileAbout> filesList;

    public List<FileAbout> getFilesList() {
        return filesList;
    }

    public FileListMessage(Path path) throws IOException {
        filesList = Files.list(path).map(Path::toFile).map(FileAbout::new).collect(Collectors.toList());
    }
}
