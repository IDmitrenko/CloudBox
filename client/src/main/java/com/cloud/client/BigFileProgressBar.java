package com.cloud.client;

import javax.swing.*;
import java.awt.*;

public class BigFileProgressBar extends JFrame {

    static private int BOR = 10;
    final JProgressBar progressBar;
    final JFrame frame;
    private int previousValue = 0;

    public BigFileProgressBar(Frame parent) {

        frame = new JFrame("Передача больших файлов");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(BOR, BOR, BOR, BOR));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(Box.createVerticalGlue());

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        panel.add(progressBar);

        panel.add(Box.createVerticalGlue());

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panel, BorderLayout.CENTER);

        frame.setPreferredSize(new Dimension(600, 200));
        progressBar.setPreferredSize(new Dimension(500, 30));
        frame.setContentPane(progressBar);
        frame.pack();
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
        progressBar.setVisible(true);
    }

    public void setProgressBar(int newValue) {
        progressBar.setValue(newValue);
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