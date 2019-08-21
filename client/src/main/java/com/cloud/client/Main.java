package com.cloud.client;

import javax.swing.*;

public class Main {

    private static BoxMainWindow boxMainWindow;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                boxMainWindow = new BoxMainWindow();
            }
        });
    }
}
