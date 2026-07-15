package com.example.cabiso_capstone;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Dormitory Management System");
        stage.setScene(scene);
       // stage.setFullScreen(true);
        stage.setMaxHeight(780);
        stage.setMaxWidth(1080);
        stage.show();
    }
}
