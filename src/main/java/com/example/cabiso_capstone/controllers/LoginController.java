package com.example.cabiso_capstone.controllers;

import com.example.cabiso_capstone.MainApplication;
import com.example.cabiso_capstone.database.DatabaseConnection;
import com.example.cabiso_capstone.session.SessionManager;
import com.example.cabiso_capstone.session.UserSession;

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

        if (username.isEmpty() || password.isEmpty()) {showError("Please enter your username and password.");
            return;
        }

        String sql =
                """
                SELECT
                    u.user_id,
                    u.username,
                    u.role,
                    u.account_status,
                    t.tenant_id,
                    t.full_name
                FROM users u
                LEFT JOIN tenants t
                    ON u.user_id = t.user_id
                WHERE u.username = ?
                  AND u.password = ?
                """;

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
                    showError(
                            "Invalid username or password."
                    );

                    passwordField.clear();
                    passwordField.requestFocus();
                    return;
                }

                int userId = resultSet.getInt("user_id");

                String storedUsername = resultSet.getString("username");

                String role = resultSet.getString("role");

                String accountStatus = resultSet.getString("account_status");

                if ("PENDING".equalsIgnoreCase(accountStatus)) {
                    showWarning("Your account is awaiting administrator approval.");
                    return;
                }

                if ("INACTIVE".equalsIgnoreCase(accountStatus)) {
                    showError("This account is inactive. " + "Please contact the administrator.");
                    return;
                }

                if (!"ACTIVE".equalsIgnoreCase(accountStatus)) {
                    showError("This account is currently unavailable.");
                    return;
                }

                Integer tenantId = null;

                int retrievedTenantId = resultSet.getInt("tenant_id");

                if (!resultSet.wasNull()) {
                    tenantId = retrievedTenantId;
                }

                String fullName = resultSet.getString("full_name");

                if (fullName == null || fullName.isBlank()) {
                    fullName = "Administrator";
                }

                UserSession userSession = new UserSession(userId, tenantId, storedUsername, fullName, role, accountStatus);

                SessionManager.saveSession(userSession);

                System.out.println("Session created: " + SessionManager.getSessionFilePath());

                if ("ADMIN".equalsIgnoreCase(role)) {

                    MainApplication.changeScene("admin-dashboard-view.fxml");

                } else if ("TENANT".equalsIgnoreCase(role)) {

                    MainApplication.changeScene("tenant-dashboard-view.fxml");

                } else {

                    SessionManager.deleteSession();

                    showError("Unknown account role.");
                }
            }

        } catch (SQLException exception) {

            showError("Unable to connect to the database.");

            exception.printStackTrace();

        } catch (IOException exception) {

            showError("Unable to create the login session.");

            exception.printStackTrace();
        }
    }

    public void openRegisterView(ActionEvent actionEvent) {

        try {
            MainApplication.changeScene("register-view.fxml");

        } catch (IOException exception) {

            showError("Unable to open registration.");

            exception.printStackTrace();
        }
    }

    private void showError(String message) {

        messageLabel.setStyle("-fx-text-fill: red;");

        messageLabel.setText(message);
    }

    private void showWarning(String message) {

        messageLabel.setStyle("-fx-text-fill: orange;");

        messageLabel.setText(message);
    }
}