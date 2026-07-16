package com.example.cabiso_capstone.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    public TextField usernameField;
    public PasswordField passwordField;
    public Label messageLabel;

    public void handleLogin(ActionEvent actionEvent) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();


        if(username.isEmpty() || password.isEmpty()){
            messageLabel.setText("Please enter your username and password");
            return;
        }

        if (username.equals("admin") && password.equals("admin123")){
            messageLabel.setStyle("-fx-text-fill: green");
            messageLabel.setText("Administrator login successful!");
        } else if (username.equals("tenant") && password.equals("tenant123")){
            messageLabel.setStyle("-fx-text-fill: green");
            messageLabel.setText("Tenant login successful!");
        } else {
            messageLabel.setStyle("-fx-text-fill: red");
            messageLabel.setText("Invalid username or password");
            passwordField.clear();
            passwordField.requestFocus();
        }


    }
}
