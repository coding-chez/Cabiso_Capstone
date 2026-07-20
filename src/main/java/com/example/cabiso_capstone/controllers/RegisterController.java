package com.example.cabiso_capstone.controllers;

import com.example.cabiso_capstone.MainApplication;
import com.example.cabiso_capstone.database.DatabaseConnection;
import com.example.cabiso_capstone.exceptions.ValidationException;
import com.example.cabiso_capstone.validation.InputValidator;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RegisterController {

    public TextField fullNameField;
    public TextField contactNumberField;
    public TextField usernameField;
    public PasswordField passwordField;
    public PasswordField confirmPasswordField;
    public Label messageLabel;

    public void handleRegister(ActionEvent actionEvent) {

        String fullName = fullNameField.getText().trim();
        String contactNumber;

        try {
            contactNumber =
                    InputValidator.validateContactNumber(
                            contactNumberField.getText()
                    );

        } catch (ValidationException exception) {

            messageLabel.setStyle(
                    "-fx-text-fill: #c62828;"
            );

            messageLabel.setText(
                    exception.getMessage()
            );

            return;
        }
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (fullName.isEmpty()
                || contactNumber.isEmpty()
                || username.isEmpty()
                || password.isEmpty()
                || confirmPassword.isEmpty()) {

            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please complete all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Passwords do not match.");

            confirmPasswordField.clear();
            confirmPasswordField.requestFocus();
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {

            String checkUsernameSql =
                    "SELECT user_id FROM users WHERE username = ?";

            try (PreparedStatement checkStatement =
                         connection.prepareStatement(checkUsernameSql)) {

                checkStatement.setString(1, username);

                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    if (resultSet.next()) {
                        messageLabel.setStyle("-fx-text-fill: red;");
                        messageLabel.setText("Username already exists.");
                        usernameField.requestFocus();
                        return;
                    }
                }
            }

            connection.setAutoCommit(false);

            try {
                String userSql =
                        "INSERT INTO users "
                                + "(username, password, role, account_status) "
                                + "VALUES (?, ?, ?, ?)";

                int userId;

                try (PreparedStatement userStatement =
                             connection.prepareStatement(
                                     userSql,
                                     Statement.RETURN_GENERATED_KEYS
                             )) {

                    userStatement.setString(1, username);
                    userStatement.setString(2, password);
                    userStatement.setString(3, "TENANT");
                    userStatement.setString(4, "PENDING");

                    userStatement.executeUpdate();

                    try (ResultSet generatedKeys =
                                 userStatement.getGeneratedKeys()) {

                        if (!generatedKeys.next()) {
                            throw new SQLException(
                                    "Unable to retrieve new user ID."
                            );
                        }

                        userId = generatedKeys.getInt(1);
                    }
                }

                String tenantSql =
                        "INSERT INTO tenants "
                                + "(user_id, room_id, full_name, "
                                + "contact_number, status) "
                                + "VALUES (?, NULL, ?, ?, ?)";

                try (PreparedStatement tenantStatement =
                             connection.prepareStatement(tenantSql)) {

                    tenantStatement.setInt(1, userId);
                    tenantStatement.setString(2, fullName);
                    tenantStatement.setString(3, contactNumber);
                    tenantStatement.setString(4, "PENDING");

                    tenantStatement.executeUpdate();
                }

                connection.commit();

                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText(
                        "Registration submitted. "
                                + "Please wait for administrator approval."
                );

                clearFields();

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }

        } catch (SQLException exception) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Registration failed.");

            exception.printStackTrace();
        }
    }

    public void handleBackToLogin(ActionEvent actionEvent) {
        try {
            MainApplication.changeScene("login-view.fxml");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void clearFields() {
        fullNameField.clear();
        contactNumberField.clear();
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();

        fullNameField.requestFocus();
    }
}