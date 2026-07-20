package com.example.cabiso_capstone;

import com.example.cabiso_capstone.session.SessionManager;
import com.example.cabiso_capstone.session.UserSession;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    private static Stage mainStage;
    @Override
    public void start(Stage stage) throws IOException {

        mainStage = stage;


        String startingView = "login-view.fxml";

        try {

            if (SessionManager.hasValidSession()) {

                UserSession session =
                        SessionManager.loadSession();

                if (session != null && session.isAdmin()) {

                    startingView =
                            "admin-dashboard-view.fxml";

                } else if (session != null && session.isTenant()) {

                    startingView =
                            "tenant-dashboard-view.fxml";
                }
            }

        } catch (Exception exception) {

            try {
                SessionManager.deleteSession();
            } catch (IOException ignored) {
            }

            startingView = "login-view.fxml";
        }

        FXMLLoader loader =
                new FXMLLoader(
                        MainApplication.class.getResource(
                                startingView
                        )
                );

        Scene scene =
                new Scene(loader.load());

        mainStage.setTitle(
                "CABANA Dormitory Management System"
        );

        mainStage.setScene(scene);
        mainStage.show();
        mainStage.setResizable(false);
    }

    public static void changeScene(String fxmlFile) throws IOException {
        FXMLLoader fxmlLoader =
                new FXMLLoader(MainApplication.class.getResource(fxmlFile));

        Scene scene = new Scene(fxmlLoader.load());

        mainStage.setScene(scene);
        mainStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
