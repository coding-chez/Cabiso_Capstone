package com.example.cabiso_capstone.controllers;

import com.example.cabiso_capstone.MainApplication;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

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
    }

    public void handleLogout(ActionEvent actionEvent) {
        try {
            MainApplication.changeScene("login-view.fxml");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void openRoomView(ActionEvent actionEvent) {
        System.out.println("open room view");
    }

    public void showDashboard(ActionEvent actionEvent) {

    }

    public void openTenantView(ActionEvent actionEvent) {
        System.out.println("tenant view");
    }

    public void openPaymentView(ActionEvent actionEvent) {
        System.out.println("open payment view");
    }
}