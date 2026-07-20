package com.example.cabiso_capstone.controllers;

import com.example.cabiso_capstone.MainApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import com.example.cabiso_capstone.session.SessionManager;
import com.example.cabiso_capstone.session.UserSession;

import java.io.IOException;

public class TenantDashboardController {

    public Label tenantNameHeaderLabel;
    public Label welcomeLabel;

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

    @FXML
    public void initialize() {

        if (!validateSession()) {
            return;
        }

        // existing initialization code...
    }

    public void showOverview(ActionEvent actionEvent) {

    }

    public void showProfile(ActionEvent actionEvent) {

    }

    public void showRoomDetails(ActionEvent actionEvent) {

    }

    public void showPaymentHistory(ActionEvent actionEvent) {

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

    public void handleUpdateInformation(ActionEvent actionEvent) {

    }

    public void handleUpdateProfile(ActionEvent actionEvent) {

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

            if (!session.isTenant()) {

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