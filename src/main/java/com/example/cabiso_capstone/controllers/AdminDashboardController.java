package com.example.cabiso_capstone.controllers;

import com.example.cabiso_capstone.MainApplication;
import com.example.cabiso_capstone.database.DatabaseConnection;
import com.example.cabiso_capstone.session.SessionManager;
import com.example.cabiso_capstone.session.UserSession;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDashboardController {

    public Button dashboardButton;

    public Label totalTenantsLabel;
    public Label availableRoomsLabel;
    public Label pendingPaymentsLabel;

    public Label occupiedRoomsLabel;
    public Label totalRoomsLabel;
    public Label totalPaymentsLabel;

    public Label adminNameLabel;

    public void initialize() {

        if (!validateSession()) {
            return;
        }

        loadLoggedInAdministrator();
        loadDashboardStatistics();
    }

    private void loadLoggedInAdministrator() {

        try {
            UserSession session =
                    SessionManager.loadSession();

            if (session != null
                    && session.getFullName() != null
                    && !session.getFullName().isBlank()) {

                adminNameLabel.setText(
                        session.getFullName()
                );

            } else {
                adminNameLabel.setText(
                        "Administrator"
                );
            }

        } catch (
                IOException
                | ClassNotFoundException exception
        ) {

            adminNameLabel.setText(
                    "Administrator"
            );

            exception.printStackTrace();
        }
    }

    private void loadDashboardStatistics() {

        String sql =
                """
                SELECT

                    (
                        SELECT COUNT(*)
                        FROM tenants
                    ) AS total_tenants,

                    (
                        SELECT COUNT(*)
                        FROM rooms r
                        WHERE r.status = 'AVAILABLE'
                          AND (
                              SELECT COUNT(*)
                              FROM tenants t
                              WHERE t.room_id = r.room_id
                                AND t.status = 'ACTIVE'
                          ) < r.capacity
                    ) AS available_rooms,

                    (
                        SELECT COUNT(*)
                        FROM payments
                        WHERE status = 'PAID'
                    ) AS paid_payments,

                    (
                        SELECT COUNT(DISTINCT room_id)
                        FROM tenants
                        WHERE room_id IS NOT NULL
                          AND status = 'ACTIVE'
                    ) AS occupied_rooms,

                    (
                        SELECT COUNT(*)
                        FROM rooms
                    ) AS total_rooms,

                    (
                        SELECT COUNT(*)
                        FROM payments
                    ) AS total_payments
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            if (resultSet.next()) {

                totalTenantsLabel.setText(
                        String.valueOf(
                                resultSet.getInt(
                                        "total_tenants"
                                )
                        )
                );

                availableRoomsLabel.setText(
                        String.valueOf(
                                resultSet.getInt(
                                        "available_rooms"
                                )
                        )
                );

                pendingPaymentsLabel.setText(
                        String.valueOf(
                                resultSet.getInt(
                                        "paid_payments"
                                )
                        )
                );

                occupiedRoomsLabel.setText(
                        String.valueOf(
                                resultSet.getInt(
                                        "occupied_rooms"
                                )
                        )
                );

                totalRoomsLabel.setText(
                        String.valueOf(
                                resultSet.getInt(
                                        "total_rooms"
                                )
                        )
                );

                totalPaymentsLabel.setText(
                        String.valueOf(
                                resultSet.getInt(
                                        "total_payments"
                                )
                        )
                );
            }

        } catch (SQLException exception) {

            setDashboardStatisticsToZero();

            System.err.println(
                    "Unable to load dashboard statistics."
            );

            exception.printStackTrace();
        }
    }

    private void setDashboardStatisticsToZero() {

        totalTenantsLabel.setText("0");
        availableRoomsLabel.setText("0");
        pendingPaymentsLabel.setText("0");

        occupiedRoomsLabel.setText("0");
        totalRoomsLabel.setText("0");
        totalPaymentsLabel.setText("0");
    }

    public void handleLogout(
            ActionEvent actionEvent
    ) {

        try {
            SessionManager.deleteSession();

            System.out.println(
                    "Session deleted: "
                            + SessionManager
                            .getSessionFilePath()
            );

            MainApplication.changeScene(
                    "login-view.fxml"
            );

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void openRoomView(
            ActionEvent actionEvent
    ) {

        try {
            MainApplication.changeScene(
                    "room-view.fxml"
            );

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void showDashboard(
            ActionEvent actionEvent
    ) {

        try {
            MainApplication.changeScene(
                    "admin-dashboard-view.fxml"
            );

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void openTenantView(
            ActionEvent actionEvent
    ) {

        try {
            MainApplication.changeScene(
                    "tenant-view.fxml"
            );

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void openPaymentView(
            ActionEvent actionEvent
    ) {

        try {
            MainApplication.changeScene(
                    "payment-view.fxml"
            );

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private boolean validateSession() {

        try {
            if (!SessionManager.hasValidSession()) {

                MainApplication.changeScene(
                        "login-view.fxml"
                );

                return false;
            }

            UserSession session =
                    SessionManager.loadSession();

            if (session == null
                    || !session.isAdmin()) {

                MainApplication.changeScene(
                        "login-view.fxml"
                );

                return false;
            }

            return true;

        } catch (Exception exception) {

            try {
                SessionManager.deleteSession();

                MainApplication.changeScene(
                        "login-view.fxml"
                );

            } catch (IOException ignored) {
            }

            return false;
        }
    }
}