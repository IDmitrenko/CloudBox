package com.cloud.client;

import javax.swing.*;
import java.awt.*;

public class BigFileProgressBar extends JFrame {

    static private int BOR = 10;
    private JProgressBar progressBar;
    final JFrame frame;
    private int previousValue = 0;

    public BigFileProgressBar(Frame parent) {

        frame = new JFrame("Передача больших файлов");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(BOR, BOR, BOR, BOR));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(Box.createVerticalGlue());

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        panel.add(progressBar);

        panel.add(Box.createVerticalGlue());

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panel, BorderLayout.CENTER);

        frame.setPreferredSize(new Dimension(600, 200));
        frame.pack();
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
    }

    public void setProgressBar(int newValue) {
        this.progressBar.setValue(newValue);
        progressBar.updateUI();
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