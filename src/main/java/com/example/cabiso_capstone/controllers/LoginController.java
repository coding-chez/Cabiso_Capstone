package com.example.cabiso_capstone.controllers;

import com.example.cabiso_capstone.MainApplication;
import com.example.cabiso_capstone.database.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    public TextField usernameField;
    public PasswordField passwordField;
    public Label messageLabel;

    public void handleLogin(ActionEvent actionEvent) {

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText(
                    "Please enter your username and password."
            );
            return;
        }

        String sql =
                "SELECT user_id, role, account_status "
                        + "FROM users "
                        + "WHERE username = ? AND password = ?";

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    messageLabel.setStyle("-fx-text-fill: red;");
                    messageLabel.setText(
                            "Invalid username or password."
                    );

                    passwordField.clear();
                    passwordField.requestFocus();
                    return;
                }

                String role =
                        resultSet.getString("role");

                String accountStatus =
                        resultSet.getString("account_status");

                if (accountStatus.equalsIgnoreCase("PENDING")) {
                    messageLabel.setStyle(
                            "-fx-text-fill: orange;"
                    );

                    messageLabel.setText(
                            "Your account is awaiting administrator approval."
                    );
                    return;
                }

                if (accountStatus.equalsIgnoreCase("INACTIVE")) {
                    messageLabel.setStyle(
                            "-fx-text-fill: red;"
                    );

                    messageLabel.setText(
                            "This account is inactive. "
                                    + "Please contact the administrator."
                    );
                    return;
                }

                if (!accountStatus.equalsIgnoreCase("ACTIVE")) {
                    messageLabel.setStyle(
                            "-fx-text-fill: red;"
                    );

                    messageLabel.setText(
                            "This account is currently unavailable."
                    );
                    return;
                }

                if (role.equalsIgnoreCase("ADMIN")) {

                    MainApplication.changeScene(
                            "admin-dashboard-view.fxml"
                    );

                } else if (role.equalsIgnoreCase("TENANT")) {

                    MainApplication.changeScene(
                            "tenant-dashboard-view.fxml"
                    );

                } else {
                    messageLabel.setStyle(
                            "-fx-text-fill: red;"
                    );

                    messageLabel.setText(
                            "Unknown account role."
                    );
                }
            }

        } catch (SQLException exception) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText(
                    "Unable to connect to the database."
            );

            exception.printStackTrace();

        } catch (IOException exception) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText(
                    "Unable to open the dashboard."
            );

            exception.printStackTrace();
        }
    }

    public void openRegisterView(ActionEvent actionEvent) {
        try {
            MainApplication.changeScene(
                    "register-view.fxml"
            );
        } catch (IOException exception) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText(
                    "Unable to open registration."
            );

            exception.printStackTrace();
        }
    }
}