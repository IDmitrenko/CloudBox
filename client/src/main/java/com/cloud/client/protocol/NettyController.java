package com.cloud.client.protocol;

import javafx.fxml.Initializable;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class NettyController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NettyNetwork.getOurInstance().start();
            }
        }).start();

        refreshLocalFileList();
    }

    public void pressOnSendData(ActionEvent actionEvent) {
        NettyNetwork.getOurInstance().sendData();
    }

    public void refreshLocalFileList() {
/*
        try {
            filesList.getItems().clear();
            Files.list(Paths.get("client/repository/")).map(p -> p.getFileName().toString()).forEach(o -> filesList.getItems().add(o));
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
    }
}
