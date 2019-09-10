package com.cloud.client.utils;

import javax.swing.*;
import java.awt.*;

public class BigFileProgressBar extends JFrame {

    static private int BOR = 20;
    final JProgressBar progressBar;
    final JFrame frame;
    private int previousValue = 0;

    public BigFileProgressBar(Frame parent) {

        frame = new JFrame("Передача больших файлов");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(600, 100));

        final JPanel panel = new JPanel();

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(BorderLayout.CENTER, progressBar);
        panel.setBorder(BorderFactory.createEmptyBorder(BOR, BOR, BOR, BOR));
        panel.add(Box.createVerticalGlue());
        panel.setSize(600, 50);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panel, BorderLayout.CENTER);

        progressBar.setPreferredSize(new Dimension(500, 20));

        frame.pack();
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
    }

    public void setProgressBar(int newValue) {
        progressBar.setValue(newValue);
    }

    public void close() {
        frame.dispose();
    }

    public int getPreviousValue() {
        return previousValue;
    }

    public void setPreviousValue(int previousValue) {
        this.previousValue = previousValue;
    }
}