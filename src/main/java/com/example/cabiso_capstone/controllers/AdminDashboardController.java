package com.example.cabiso_capstone.controllers;

import com.example.cabiso_capstone.MainApplication;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.example.cabiso_capstone.session.SessionManager;
import com.example.cabiso_capstone.session.UserSession;
import java.io.IOException;

public class AdminDashboardController {

    public Button dashboardButton;
    public Label pendingPaymentsLabel;
    public Label availableRoomsLabel;
    public Label totalTenantsLabel;
    public Label totalRoomsLabel;
    public Label totalPaymentsLabel;
    public Label adminNameLabel;
    public Label occupiedRoomsLabel;

    public void initialize() {
        // Temporary values
        totalTenantsLabel.setText("2");
        availableRoomsLabel.setText("5");
        pendingPaymentsLabel.setText("1");
        if (!validateSession()) {
            return;
        }
    }

    public void handleLogout(ActionEvent actionEvent) {

        try {
            SessionManager.deleteSession();

            System.out.println(
                    "Session deleted: "
                            + SessionManager.getSessionFilePath()
            );

            MainApplication.changeScene(
                    "login-view.fxml"
            );

        } catch (IOException exception) {

            exception.printStackTrace();
        }
    }

    public void openRoomView(ActionEvent actionEvent) {
        try {
            MainApplication.changeScene("room-view.fxml");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void showDashboard(ActionEvent actionEvent) {
        try {
            MainApplication.changeScene("admin-dashboard-view.fxml");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void openTenantView(ActionEvent actionEvent) {
        try {
            MainApplication.changeScene("tenant-view.fxml");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void openPaymentView(ActionEvent actionEvent) {
        try {
            MainApplication.changeScene("payment-view.fxml");
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

            UserSession session = SessionManager.loadSession();
            if (!session.isAdmin()) {

                MainApplication.changeScene(
                        "login-view.fxml"
                );

                return false;
            }
            if (session == null) {

                MainApplication.changeScene(
                        "login-view.fxml"
                );

                return false;
            }

            return true;

        } catch (Exception exception) {

            try {

                MainApplication.changeScene(
                        "login-view.fxml"
                );

            } catch (IOException ignored) {
            }

            return false;
        }

    }
}