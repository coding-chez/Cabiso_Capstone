package com.example.cabiso_capstone.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class TenantDashboardController {
    public Label tenantNameHeaderLabel;
    public Label welcomeLabel;
//    public Label assignedRoomLabel;
//    public Label currentBalanceLabel;
//    public Label accountStatusLabel;
//    public Label fullNameLabel;
//    public Label usernameLabel;
//    public Label contactNumberLabel;
//    public Label monthlyRateLabel;
    public TableView recentPaymentsTable;
    public TableColumn paymentDateColumn;
    public TableColumn paymentMethodColumn;
    public TableColumn paymentStatusColumn;
    public Button overviewButton;
    public Button profileButton;
    public Button roomButton;
    public TableColumn paymentReferenceColumn;
    public TableColumn paymentAmountColumn;
    public TableColumn paymentIdColumn;
    public TableView paymentHistoryTable;
    public Label paymentRecordCountLabel;
    public VBox roomPane;
    public Label roomNumberLabel;

    public void showOverview(ActionEvent actionEvent) {
    }

    public void showProfile(ActionEvent actionEvent) {
    }

    public void showRoomDetails(ActionEvent actionEvent) {
    }

    public void showPaymentHistory(ActionEvent actionEvent) {
    }

    public void handleLogout(ActionEvent actionEvent) {
    }

    public void handleUpdateInformation(ActionEvent actionEvent) {
    }

    public void handleUpdateProfile(ActionEvent actionEvent) {
    }
}
