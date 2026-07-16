package com.example.cabiso_capstone.controllers;

import com.example.cabiso_capstone.MainApplication;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    public TextField usernameField;
    public PasswordField passwordField;
    public Label messageLabel;

    public void handleLogin(ActionEvent actionEvent) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please enter your username and password");
            return;
        }

        try {
            if (username.equals("admin")
                    && password.equals("admin123")) {

                MainApplication.changeScene(
                        "admin-dashboard-view.fxml"
                );

            } else if (username.equals("tenant")
                    && password.equals("tenant123")) {

                MainApplication.changeScene(
                        "tenant-dashboard-view.fxml"
                );

            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Invalid username or password");

                passwordField.clear();
                passwordField.requestFocus();
            }

        } catch (IOException exception) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Unable to open dashboard");

            exception.printStackTrace();
        }
    }
}