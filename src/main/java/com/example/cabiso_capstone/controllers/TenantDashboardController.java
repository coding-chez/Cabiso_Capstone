package com.example.cabiso_capstone.controllers;

import com.example.cabiso_capstone.MainApplication;
import com.example.cabiso_capstone.database.DatabaseConnection;
import com.example.cabiso_capstone.session.SessionManager;
import com.example.cabiso_capstone.session.UserSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TenantDashboardController {
    public Label tenantNameHeaderLabel;
    public Label welcomeLabel;

    public Button overviewButton;
    public Button profileButton;
    public Button roomButton;
    public Button paymentButton;

    public VBox overviewPane;
    public VBox profilePane;
    public VBox roomPane;
    public VBox paymentHistoryPane;

    public Label overviewRoomLabel;
    public Label overviewBalanceLabel;
    public Label overviewStatusLabel;

    public TableView<TenantPaymentRecord> recentPaymentsTable;

    public TableColumn<TenantPaymentRecord, String> recentPaymentDateColumn;
    public TableColumn<TenantPaymentRecord, String> recentAmountColumn;
    public TableColumn<TenantPaymentRecord, String> recentMethodColumn;
    public TableColumn<TenantPaymentRecord, String> recentReferenceColumn;
    public TableColumn<TenantPaymentRecord, String> recentStatusColumn;


    public TextField profileTenantIdField;
    public TextField profileFullNameField;
    public TextField profileUsernameField;
    public TextField profileContactField;
    public TextField profileStatusField;
    public TextField profileRoomField;

    public Label profileMessageLabel;

    public Label roomNumberLabel;
    public Label roomCapacityLabel;
    public Label roomOccupantsLabel;
    public Label roomAvailableSlotsLabel;
    public Label roomMonthlyRateLabel;
    public Label roomStatusLabel;

    private final ObservableList<TenantPaymentHistoryRecord> paymentHistoryRecords = FXCollections.observableArrayList();

    public TableView<TenantPaymentHistoryRecord> paymentHistoryTable;

    public TableColumn<TenantPaymentHistoryRecord, String> paymentIdColumn;

    public TableColumn<TenantPaymentHistoryRecord, String> billingMonthColumn;

    public TableColumn<TenantPaymentHistoryRecord, String> paymentDateColumn;

    public TableColumn<TenantPaymentHistoryRecord, String> paymentAmountColumn;

    public TableColumn<TenantPaymentHistoryRecord, String> paymentMethodColumn;

    public TableColumn<TenantPaymentHistoryRecord, String> paymentReferenceColumn;

    public TableColumn<TenantPaymentHistoryRecord, String> paymentStatusColumn;


    public Label paymentRecordCountLabel;


    private UserSession currentSession;
    private final ObservableList<TenantPaymentRecord> recentPaymentRecords = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        if (!validateSession()) {
            return;
        }

        initializeRecentPaymentsTable();
        initializePaymentHistoryTable();

        refreshTenantDashboard();

        showOverview(null);
    }

    private void loadFullPaymentHistory() {

        paymentHistoryRecords.clear();

        if (currentSession == null
                || currentSession.getTenantId() == null) {

            updatePaymentRecordCount();
            return;
        }

        String sql =
                """
                SELECT
                    payment_id,
                    billing_month,
                    amount,
                    payment_date,
                    payment_method,
                    reference_number,
                    status
    
                FROM payments
    
                WHERE tenant_id = ?
    
                ORDER BY
                    payment_date DESC,
                    payment_id DESC
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    currentSession.getTenantId()
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    LocalDate paymentDate = null;

                    java.sql.Date sqlDate =
                            resultSet.getDate(
                                    "payment_date"
                            );

                    if (sqlDate != null) {
                        paymentDate =
                                sqlDate.toLocalDate();
                    }

                    String paymentMethod =
                            resultSet.getString(
                                    "payment_method"
                            );

                    if (paymentMethod == null
                            || paymentMethod.isBlank()) {

                        paymentMethod = "N/A";
                    }

                    String referenceNumber =
                            resultSet.getString(
                                    "reference_number"
                            );

                    if (referenceNumber == null
                            || referenceNumber.isBlank()) {

                        referenceNumber = "N/A";
                    }

                    paymentHistoryRecords.add(
                            new TenantPaymentHistoryRecord(
                                    resultSet.getInt(
                                            "payment_id"
                                    ),
                                    resultSet.getString(
                                            "billing_month"
                                    ),
                                    paymentDate,
                                    resultSet.getDouble(
                                            "amount"
                                    ),
                                    paymentMethod,
                                    referenceNumber,
                                    resultSet.getString(
                                            "status"
                                    )
                            )
                    );
                }
            }

            paymentHistoryTable.setItems(
                    paymentHistoryRecords
            );

            paymentHistoryTable.refresh();

            updatePaymentRecordCount();

            System.out.println(
                    "Full tenant payment history loaded: "
                            + paymentHistoryRecords.size()
            );

        } catch (SQLException exception) {

            paymentHistoryRecords.clear();
            updatePaymentRecordCount();

            System.err.println(
                    "Unable to load full payment history."
            );

            exception.printStackTrace();
        }
    }
    private void updatePaymentRecordCount() {

        int recordCount =
                paymentHistoryRecords.size();

        paymentRecordCountLabel.setText(
                recordCount
                        + (
                        recordCount == 1
                                ? " record"
                                : " records"
                )
        );
    }
    private void refreshTenantDashboard() {

        loadLoggedInTenantProfile();
        loadTenantBalance();
        loadRecentPayments();
        loadTenantRoomDetails();
        loadFullPaymentHistory();
    }

    private void initializePaymentHistoryTable() {

        paymentIdColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(
                                data.getValue()
                                        .getPaymentId()
                        )
                )
        );

        billingMonthColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue()
                                .getBillingMonth()
                )
        );

        paymentDateColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue()
                                .getPaymentDate() == null
                                ? "No Date"
                                : data.getValue()
                                .getPaymentDate()
                                .toString()
                )
        );

        paymentAmountColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.format(
                                "₱%,.2f",
                                data.getValue()
                                        .getAmount()
                        )
                )
        );

        paymentMethodColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue()
                                .getPaymentMethod()
                )
        );

        paymentReferenceColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue()
                                .getReferenceNumber()
                )
        );

        paymentStatusColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue()
                                .getStatus()
                )
        );

        paymentHistoryTable.setItems(
                paymentHistoryRecords
        );

        paymentHistoryTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        paymentHistoryTable.getColumns()
                .forEach(
                        column ->
                                column.setReorderable(false)
                );
    }

    private void loadTenantRoomDetails() {

        if (currentSession == null
                || currentSession.getTenantId() == null) {

            clearRoomDetails();
            return;
        }

        String sql =
                """
                SELECT
                    r.room_number,
                    r.capacity,
                    r.monthly_rate,
                    r.status,
    
                    (
                        SELECT COUNT(*)
                        FROM tenants occupants
                        WHERE occupants.room_id = r.room_id
                          AND occupants.status = 'ACTIVE'
                    ) AS current_occupants
    
                FROM tenants t
    
                LEFT JOIN rooms r
                    ON t.room_id = r.room_id
    
                WHERE t.tenant_id = ?
                  AND t.user_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    currentSession.getTenantId()
            );

            statement.setInt(
                    2,
                    currentSession.getUserId()
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (!resultSet.next()) {

                    clearRoomDetails();
                    return;
                }

                String roomNumber =
                        resultSet.getString(
                                "room_number"
                        );

                if (roomNumber == null) {

                    clearRoomDetails();
                    return;
                }

                int capacity =
                        resultSet.getInt(
                                "capacity"
                        );

                int occupants =
                        resultSet.getInt(
                                "current_occupants"
                        );

                int availableSlots =
                        Math.max(
                                capacity - occupants,
                                0
                        );

                double monthlyRate =
                        resultSet.getDouble(
                                "monthly_rate"
                        );

                String roomStatus =
                        resultSet.getString(
                                "status"
                        );

                roomNumberLabel.setText(
                        roomNumber
                );

                roomCapacityLabel.setText(
                        String.valueOf(capacity)
                );

                roomOccupantsLabel.setText(
                        String.valueOf(occupants)
                );

                roomAvailableSlotsLabel.setText(
                        String.valueOf(
                                availableSlots
                        )
                );

                roomMonthlyRateLabel.setText(
                        String.format(
                                "₱%,.2f",
                                monthlyRate
                        )
                );

                roomStatusLabel.setText(
                        roomStatus
                );
            }

        } catch (SQLException exception) {

            clearRoomDetails();

            System.err.println(
                    "Unable to load tenant room details."
            );

            exception.printStackTrace();
        }
    }

    private void clearRoomDetails() {

        roomNumberLabel.setText(
                "Not Assigned"
        );

        roomCapacityLabel.setText(
                "0"
        );

        roomOccupantsLabel.setText(
                "0"
        );

        roomAvailableSlotsLabel.setText(
                "0"
        );

        roomMonthlyRateLabel.setText(
                "₱0.00"
        );

        roomStatusLabel.setText(
                "Unavailable"
        );
    }

    private void loadTenantBalance() {

        if (currentSession == null
                || currentSession.getTenantId() == null) {

            return;
        }

        String sql =
                """
                SELECT
                    COALESCE(r.monthly_rate, 0)
                        AS monthly_rate,
    
                    COALESCE(
                        SUM(
                            CASE
                                WHEN p.status = 'PAID'
                                THEN p.amount
                                ELSE 0
                            END
                        ),
                        0
                    ) AS total_paid
    
                FROM tenants t
    
                LEFT JOIN rooms r
                    ON t.room_id = r.room_id
    
                LEFT JOIN payments p
                    ON t.tenant_id = p.tenant_id
    
                WHERE t.tenant_id = ?
    
                GROUP BY
                    t.tenant_id,
                    r.monthly_rate
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    currentSession.getTenantId()
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (resultSet.next()) {

                    double monthlyRate =
                            resultSet.getDouble(
                                    "monthly_rate"
                            );

                    double totalPaid =
                            resultSet.getDouble(
                                    "total_paid"
                            );

                    double balance =
                            Math.max(
                                    monthlyRate - totalPaid,
                                    0
                            );

                    overviewBalanceLabel.setText(
                            String.format(
                                    "₱%,.2f",
                                    balance
                            )
                    );

                } else {

                    overviewBalanceLabel.setText(
                            "₱0.00"
                    );
                }
            }

        } catch (SQLException exception) {

            overviewBalanceLabel.setText(
                    "Unavailable"
            );

            System.err.println(
                    "Unable to load tenant balance."
            );

            exception.printStackTrace();
        }
    }

    private void initializeRecentPaymentsTable() {

        recentPaymentDateColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue()
                                .getPaymentDate() == null

                                ? "No Date"

                                : data.getValue()
                                .getPaymentDate()
                                .toString()
                )
        );

        recentAmountColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.format(
                                "₱%,.2f",
                                data.getValue()
                                        .getAmount()
                        )
                )
        );

        recentMethodColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue()
                                .getPaymentMethod()
                )
        );

        recentReferenceColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue()
                                .getReferenceNumber()
                )
        );

        recentStatusColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue()
                                .getStatus()
                )
        );

        recentPaymentsTable.setItems(
                recentPaymentRecords
        );

        recentPaymentsTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        recentPaymentsTable.getColumns()
                .forEach(
                        column ->
                                column.setReorderable(false)
                );
    }

    private void loadRecentPayments() {

        recentPaymentRecords.clear();

        if (currentSession == null
                || currentSession.getTenantId() == null) {

            return;
        }

        String sql =
                """
                SELECT
                    payment_date,
                    amount,
                    payment_method,
                    reference_number,
                    status
    
                FROM payments
    
                WHERE tenant_id = ?
    
                ORDER BY
                    payment_date DESC,
                    payment_id DESC
    
                LIMIT 5
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    currentSession.getTenantId()
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    LocalDate paymentDate = null;

                    if (resultSet.getDate(
                            "payment_date"
                    ) != null) {

                        paymentDate =
                                resultSet.getDate(
                                        "payment_date"
                                ).toLocalDate();
                    }

                    String referenceNumber =
                            resultSet.getString(
                                    "reference_number"
                            );

                    if (referenceNumber == null
                            || referenceNumber.isBlank()) {

                        referenceNumber =
                                "N/A";
                    }

                    String paymentMethod =
                            resultSet.getString(
                                    "payment_method"
                            );

                    if (paymentMethod == null
                            || paymentMethod.isBlank()) {

                        paymentMethod =
                                "N/A";
                    }

                    recentPaymentRecords.add(
                            new TenantPaymentRecord(
                                    paymentDate,
                                    resultSet.getDouble(
                                            "amount"
                                    ),
                                    paymentMethod,
                                    referenceNumber,
                                    resultSet.getString(
                                            "status"
                                    )
                            )
                    );
                }
            }

            recentPaymentsTable.setItems(
                    recentPaymentRecords
            );

            recentPaymentsTable.refresh();

            System.out.println(
                    "Recent tenant payments loaded: "
                            + recentPaymentRecords.size()
            );

        } catch (SQLException exception) {

            recentPaymentRecords.clear();

            System.err.println(
                    "Unable to load recent tenant payments."
            );

            exception.printStackTrace();
        }
    }

    private void loadLoggedInTenantProfile() {

        if (currentSession == null
                || currentSession.getTenantId() == null) {

            redirectToLogin();
            return;
        }

        String sql =
                """
                SELECT
                    t.tenant_id,
                    t.full_name,
                    t.contact_number,
                    t.status AS tenant_status,

                    u.username,
                    u.account_status,

                    r.room_number

                FROM tenants t

                INNER JOIN users u
                    ON t.user_id = u.user_id

                LEFT JOIN rooms r
                    ON t.room_id = r.room_id

                WHERE t.tenant_id = ?
                  AND t.user_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    currentSession.getTenantId()
            );

            statement.setInt(
                    2,
                    currentSession.getUserId()
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (!resultSet.next()) {

                    SessionManager.deleteSession();
                    redirectToLogin();
                    return;
                }

                String fullName =
                        resultSet.getString(
                                "full_name"
                        );

                String username =
                        resultSet.getString(
                                "username"
                        );

                String contactNumber =
                        resultSet.getString(
                                "contact_number"
                        );

                String tenantStatus =
                        resultSet.getString(
                                "tenant_status"
                        );

                String accountStatus =
                        resultSet.getString(
                                "account_status"
                        );

                String roomNumber =
                        resultSet.getString(
                                "room_number"
                        );

                if (roomNumber == null
                        || roomNumber.isBlank()) {

                    roomNumber =
                            "Not Assigned";
                }


                tenantNameHeaderLabel.setText(
                        fullName
                );

                welcomeLabel.setText(
                        "Welcome back, "
                                + fullName
                                + "!"
                );


                overviewRoomLabel.setText(
                        roomNumber
                );

                overviewStatusLabel.setText(
                        tenantStatus
                );

                overviewBalanceLabel.setText(
                        "₱0.00"
                );


                profileTenantIdField.setText(
                        String.valueOf(
                                resultSet.getInt(
                                        "tenant_id"
                                )
                        )
                );

                profileFullNameField.setText(
                        fullName
                );

                profileUsernameField.setText(
                        username
                );

                profileContactField.setText(
                        contactNumber
                );

                profileStatusField.setText(
                        accountStatus
                );

                profileRoomField.setText(
                        roomNumber
                );


                roomNumberLabel.setText(
                        roomNumber
                );

                System.out.println(
                        "Logged-in tenant loaded: "
                                + fullName
                );
            }

        } catch (SQLException exception) {

            showProfileError(
                    "Unable to load your tenant information."
            );

            exception.printStackTrace();

        } catch (IOException exception) {

            redirectToLogin();

            exception.printStackTrace();
        }
    }

    public void showOverview(
            ActionEvent actionEvent
    ) {

        loadLoggedInTenantProfile();
        loadTenantBalance();
        loadRecentPayments();

        showPane(
                overviewPane
        );

        setActiveSidebarButton(
                overviewButton
        );
    }

    public void showProfile(
            ActionEvent actionEvent
    ) {

        loadLoggedInTenantProfile();

        profileMessageLabel.setText("");

        showPane(
                profilePane
        );

        setActiveSidebarButton(
                profileButton
        );
    }

    public void showRoomDetails(
            ActionEvent actionEvent
    ) {

        loadTenantRoomDetails();

        showPane(
                roomPane
        );

        setActiveSidebarButton(
                roomButton
        );
    }

    public void showPaymentHistory(
            ActionEvent actionEvent
    ) {

        loadFullPaymentHistory();

        showPane(
                paymentHistoryPane
        );

        setActiveSidebarButton(
                paymentButton
        );
    }

    private void showPane(
            VBox selectedPane
    ) {

        VBox[] panes = {
                overviewPane,
                profilePane,
                roomPane,
                paymentHistoryPane
        };

        for (VBox pane : panes) {

            if (pane == null) {
                continue;
            }

            boolean selected =
                    pane == selectedPane;

            pane.setVisible(selected);
            pane.setManaged(selected);
        }
    }

    private void setActiveSidebarButton(
            Button selectedButton
    ) {

        Button[] buttons = {
                overviewButton,
                profileButton,
                roomButton,
                paymentButton
        };

        for (Button button : buttons) {

            if (button == null) {
                continue;
            }

            button.getStyleClass().remove(
                    "sidebar-button-active"
            );

            if (!button.getStyleClass().contains(
                    "sidebar-button"
            )) {

                button.getStyleClass().add(
                        "sidebar-button"
                );
            }
        }

        if (selectedButton == null) {
            return;
        }

        selectedButton.getStyleClass().remove(
                "sidebar-button"
        );

        if (!selectedButton
                .getStyleClass()
                .contains(
                        "sidebar-button-active"
                )) {

            selectedButton.getStyleClass().add(
                    "sidebar-button-active"
            );
        }
    }


    public void handleUpdateInformation(
            ActionEvent actionEvent
    ) {

        handleUpdateProfile(actionEvent);
    }

    public void handleUpdateProfile(
            ActionEvent actionEvent
    ) {

        if (currentSession == null
                || currentSession.getTenantId() == null) {

            showProfileError(
                    "Your session is unavailable. Please log in again."
            );

            return;
        }

        String contactNumber =
                profileContactField
                        .getText()
                        .trim();

        if (contactNumber.isEmpty()) {

            showProfileError(
                    "Please enter your contact number."
            );

            return;
        }

        if (!contactNumber.matches("[0-9+\\- ]{7,20}")) {

            showProfileError(
                    "Enter a valid contact number using 7 to 20 digits."
            );

            return;
        }

        String sql =
                """
                UPDATE tenants
                SET contact_number = ?
                WHERE tenant_id = ?
                  AND user_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    contactNumber
            );

            statement.setInt(
                    2,
                    currentSession.getTenantId()
            );

            statement.setInt(
                    3,
                    currentSession.getUserId()
            );

            int rows =
                    statement.executeUpdate();

            if (rows == 0) {

                showProfileError(
                        "Your profile could not be updated."
                );

                return;
            }

            loadLoggedInTenantProfile();
            loadTenantBalance();

            showProfileSuccess(
                    "Contact number updated successfully."
            );

        } catch (SQLException exception) {

            showProfileError(
                    "Database error while updating your profile."
            );

            exception.printStackTrace();
        }
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


    private boolean validateSession() {

        try {

            if (!SessionManager.hasValidSession()) {

                redirectToLogin();
                return false;
            }

            currentSession =
                    SessionManager.loadSession();

            if (currentSession == null
                    || !currentSession.isTenant()
                    || !currentSession.isActive()
                    || currentSession.getTenantId() == null) {

                SessionManager.deleteSession();
                redirectToLogin();

                return false;
            }

            return true;

        } catch (
                IOException
                | ClassNotFoundException exception
        ) {

            try {
                SessionManager.deleteSession();
            } catch (IOException ignored) {
            }

            redirectToLogin();

            exception.printStackTrace();

            return false;
        }
    }

    private void redirectToLogin() {

        try {
            MainApplication.changeScene(
                    "login-view.fxml"
            );

        } catch (IOException exception) {

            exception.printStackTrace();
        }
    }


    private void showProfileError(
            String message
    ) {

        profileMessageLabel.setStyle(
                "-fx-text-fill: #c62828;"
        );

        profileMessageLabel.setText(
                message
        );
    }
    private void showProfileSuccess(
            String message
    ) {

        profileMessageLabel.setStyle(
                "-fx-text-fill: #08786c;"
        );

        profileMessageLabel.setText(
                message
        );
    }

    public void handleRefreshDashboard(
            ActionEvent actionEvent
    ) {

        refreshTenantDashboard();

        System.out.println(
                "Tenant dashboard refreshed."
        );
    }


    public static class TenantPaymentRecord {

        private final LocalDate paymentDate;
        private final double amount;
        private final String paymentMethod;
        private final String referenceNumber;
        private final String status;

        public TenantPaymentRecord(
                LocalDate paymentDate,
                double amount,
                String paymentMethod,
                String referenceNumber,
                String status
        ) {
            this.paymentDate =
                    paymentDate;

            this.amount =
                    amount;

            this.paymentMethod =
                    paymentMethod;

            this.referenceNumber =
                    referenceNumber;

            this.status =
                    status;
        }

        public LocalDate getPaymentDate() {
            return paymentDate;
        }

        public double getAmount() {
            return amount;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public String getReferenceNumber() {
            return referenceNumber;
        }

        public String getStatus() {
            return status;
        }
    }

    public static class TenantPaymentHistoryRecord {

        private final int paymentId;
        private final String billingMonth;
        private final LocalDate paymentDate;
        private final double amount;
        private final String paymentMethod;
        private final String referenceNumber;
        private final String status;

        public TenantPaymentHistoryRecord(
                int paymentId,
                String billingMonth,
                LocalDate paymentDate,
                double amount,
                String paymentMethod,
                String referenceNumber,
                String status
        ) {
            this.paymentId = paymentId;
            this.billingMonth = billingMonth;
            this.paymentDate = paymentDate;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
            this.referenceNumber = referenceNumber;
            this.status = status;
        }

        public int getPaymentId() {
            return paymentId;
        }

        public String getBillingMonth() {
            return billingMonth;
        }

        public LocalDate getPaymentDate() {
            return paymentDate;
        }

        public double getAmount() {
            return amount;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public String getReferenceNumber() {
            return referenceNumber;
        }

        public String getStatus() {
            return status;
        }
    }
}