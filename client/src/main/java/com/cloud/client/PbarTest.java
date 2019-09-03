package com.cloud.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

public class PbarTest extends JFrame {
    JProgressBar progressBar;
    public static void main(String[] args) throws InterruptedException {
        PbarTest boxMainWindow = new PbarTest();
        for(int i = 1;i<5;i++) {
            final int j = i;
            SwingUtilities.invokeLater(() -> {
                try {
                    boxMainWindow.runBar(j);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            TimeUnit.SECONDS.sleep(1L);
        }
    }

    public PbarTest() throws InterruptedException {
        setTitle("CLOUD");
        setBounds(200, 200, 800, 800);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        JButton sendButtonClient = new JButton("Отправить файл");
        sendButtonClient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
