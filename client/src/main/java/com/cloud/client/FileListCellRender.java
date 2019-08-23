package com.cloud.client;

import javax.swing.*;
import java.awt.*;

public class FileListCellRender extends JPanel implements ListCellRenderer<FileList> {

    private final JLabel fileName;

    private final JLabel fileSize;

    private final JPanel panel;

    public FileListCellRender() {
        super();
        setLayout(new BorderLayout());

        fileName = new JLabel();
        fileSize = new JLabel();
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panel.add(fileName);
        panel.add(fileSize);

        Font f = fileName.getFont();
        fileName.setFont(f.deriveFont(f.getStyle() | Font.BOLD));

        add(panel, BorderLayout.NORTH);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends FileList> list,
                                                  FileList value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        setBackground(list.getBackground());
        fileName.setText(value.getFileName());
        fileSize.setText(value.getFileSize());
        return this;
    }
}
