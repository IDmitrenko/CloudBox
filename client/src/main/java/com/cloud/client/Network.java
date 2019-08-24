package com.cloud.client;

import com.cloud.common.transfer.AbstractMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;

public class Network {
    private static Socket socket;
    private static ObjectEncoderOutputStream out; // отправка сообщения
    private static ObjectDecoderInputStream in;   // получение объекта (нкжен channel)
    
    public static void start() {
        try {
            socket = new Socket("localhost", 8189);
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream(), 50 * 1024 * 1024);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void stop() {
        try {
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean sendMsg(AbstractMessage msg) {
        // отправка сообщения (объекта) о том, что мы хотим получить файл
        try {
            out.writeObject(msg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static AbstractMessage readObject() throws ClassNotFoundException, IOException {
        // считывание объекта
        Object obj = in.readObject();
        return (AbstractMessage) obj;
    }
    
    public void authorize(String login, String password) throws IOException {

    }

}
