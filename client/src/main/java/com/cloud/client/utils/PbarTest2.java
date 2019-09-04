package com.cloud.client.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PbarTest2 extends JFrame {
    JProgressBar progressBar;

    public static void main(String[] args) throws InterruptedException {
        PbarTest2 boxMainWindow = new PbarTest2();

    }

    public PbarTest2() throws InterruptedException {
        setTitle("CLOUD");
        setBounds(200, 200, 800, 800);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        JButton sendButtonClient = new JButton("Отправить файл");
        sendButtonClient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Pbar bar = new Pbar(PbarTest2.this);
                try {
                    bar.runBar(0);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

                startBar(bar);

            }
        });
        JPanel sendCommandPanel = new JPanel();
        sendCommandPanel.setLayout(new BoxLayout(sendCommandPanel, BoxLayout.X_AXIS));
        sendButtonClient.setPreferredSize(new Dimension(144, 30));
        sendCommandPanel.add(sendButtonClient);
        add(sendCommandPanel, BorderLayout.SOUTH);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        sendCommandPanel.add(progressBar);

        setVisible(true);
    }

    private void startBar(Pbar bar) {
        CompletableFuture.supplyAsync(() -> {
            for (int i = 1; i < 5; i++) {
                final int j = i;

                SwingUtilities.invokeLater(() -> {
                    try {
                        bar.runBar(j * 10);
                    } catch (InterruptedException e12) {
                        e12.printStackTrace();
                    }
                });
                try {
                    TimeUnit.SECONDS.sleep(1L);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            return null;
        });
    }

    public void runBar(int t) throws InterruptedException {
        /*for (int i = 1; i <= 10; i++) {
            progressBar.setValue(10 * i);
            TimeUnit.SECONDS.sleep(1L);
        }*/
        progressBar.setValue(t);
    }

    public static class Pbar extends JFrame {
        final JProgressBar progressBar;
        static private int BOR = 10;
        final JFrame frame;

        public void runBar(int t) throws InterruptedException {
        /*for (int i = 1; i <= 10; i++) {
            progressBar.setValue(10 * i);
            TimeUnit.SECONDS.sleep(1L);
        }*/
            progressBar.setValue(t);
        }

        public Pbar(Frame parent) {

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
    }
}
