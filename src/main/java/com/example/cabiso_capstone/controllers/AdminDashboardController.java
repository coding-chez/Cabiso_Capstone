package com.example.cabiso_capstone.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class AdminDashboardController {

    public Button dashboardButton;
    public Label pendingPaymentsLabel;
    public Label availableRoomsLabel;
    public Label totalTenantsLabel;
    public Label totalRoomsLabel;
    public Label totalPaymentsLabel;
    public Label adminNameLabel;
    public Label occupiedRoomsLabel;

    public void initialize(){
        //temp values
        totalTenantsLabel.setText("2");
        availableRoomsLabel.setText("5");
        pendingPaymentsLabel.setText("1");
    }

    public void handleLogout(ActionEvent actionEvent) {
        System.out.println("logout"); //checking in 123
    }

    public void openRoomView(ActionEvent actionEvent) {
        System.out.println("open room view"); //checking in 123
    }

    public void showDashboard(ActionEvent actionEvent) {

    }

    public void openTenantView(ActionEvent actionEvent) {
        System.out.println("tenant view"); //checking in 123
    }

    public void openPaymentView(ActionEvent actionEvent) {
        System.out.println("open payment view"); //checking in 123
    }
}
