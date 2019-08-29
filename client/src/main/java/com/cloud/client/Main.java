package com.cloud.client;

import javax.swing.*;
import java.io.IOException;

public class Main {

    private static MainWindow boxMainWindow;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    boxMainWindow = new MainWindow();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
