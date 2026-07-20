package com.example.cabiso_capstone.controllers;

import com.example.cabiso_capstone.MainApplication;
import com.example.cabiso_capstone.database.DatabaseConnection;
import com.example.cabiso_capstone.model.Payment;
import com.example.cabiso_capstone.model.Room;
import com.example.cabiso_capstone.model.Tenant;
import com.example.cabiso_capstone.session.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class PaymentController {
    public TextField searchField;

    public ComboBox<String> filterStatusComboBox;

    public Label recordCountLabel;

    public TableView<Payment> paymentTable;

    public TableColumn<Payment, Integer> paymentIdColumn;

    public TableColumn<Payment, String> tenantNameColumn;

    public TableColumn<Payment, String> roomColumn;

    public TableColumn<Payment, String> billingMonthColumn;

    public TableColumn<Payment, Double> amountColumn;

    public TableColumn<Payment, LocalDate> paymentDateColumn;

    public TableColumn<Payment, String> paymentMethodColumn;

    public TableColumn<Payment, String> statusColumn;

    public TextField paymentIdField;

    public ComboBox<Tenant> tenantComboBox;

    public ComboBox<String> billingMonthComboBox;

    public TextField amountField;

    public DatePicker paymentDatePicker;

    public ComboBox<String> paymentMethodComboBox;

    public ComboBox<String> statusComboBox;

    public TextField roomField;

    public TextField currentBalanceField;

    public TextField referenceNumberField;

    public TextArea remarksArea;

    public Label formMessageLabel;

    private final ObservableList<Tenant> activeTenantList = FXCollections.observableArrayList();

    private final ObservableList<Payment> paymentList = FXCollections.observableArrayList();

    public void handleRecordPayment(ActionEvent actionEvent) {

        Tenant selectedTenant =
                tenantComboBox.getValue();

        String billingMonth =
                (String) billingMonthComboBox.getValue();

        String amountText =
                amountField.getText().trim();

        LocalDate paymentDate =
                paymentDatePicker.getValue();

        String paymentMethod =
                (String) paymentMethodComboBox.getValue();

        String paymentStatus =
                (String) statusComboBox.getValue();

        String referenceNumber =
                referenceNumberField.getText().trim();

        String remarks =
                remarksArea.getText().trim();

        if (selectedTenant == null
                || billingMonth == null
                || amountText.isEmpty()
                || paymentDate == null
                || paymentMethod == null
                || paymentStatus == null) {

            showFormError(
                    "Please complete all required payment information."
            );

            return;
        }

        double amount;

        try {

            amount =
                    Double.parseDouble(amountText);

        } catch (NumberFormatException exception) {

            showFormError(
                    "Payment amount must be a valid number."
            );

            return;
        }

        if (amount <= 0) {

            showFormError(
                    "Payment amount must be greater than zero."
            );

            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            if (paymentAlreadyExists(
                    connection,
                    selectedTenant.getTenantId(),
                    billingMonth
            )) {

                showFormError(
                        "This tenant already has a recorded payment for the selected billing month."
                );

                return;
            }
            String sql =

                    """
                    INSERT INTO payments
                    (
                        tenant_id,
                        billing_month,
                        amount,
                        payment_date,
                        payment_method,
                        reference_number,
                        remarks,
                        status
                    )
                    VALUES
                    (?, ?, ?, ?, ?, ?, ?, ?)
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(sql)
            ) {

                statement.setInt(
                        1,
                        selectedTenant.getTenantId()
                );

                statement.setString(
                        2,
                        billingMonth
                );

                statement.setDouble(
                        3,
                        amount
                );

                statement.setDate(
                        4,
                        java.sql.Date.valueOf(paymentDate)
                );

                statement.setString(
                        5,
                        paymentMethod
                );

                if (referenceNumber.isBlank()) {

                    statement.setNull(
                            6,
                            Types.VARCHAR
                    );

                } else {

                    statement.setString(
                            6,
                            referenceNumber
                    );
                }

                if (remarks.isBlank()) {

                    statement.setNull(
                            7,
                            Types.VARCHAR
                    );

                } else {

                    statement.setString(
                            7,
                            remarks
                    );
                }

                statement.setString(
                        8,
                        paymentStatus
                );

                int rows =
                        statement.executeUpdate();

                if (rows == 0) {

                    showFormError(
                            "Payment could not be recorded."
                    );

                    return;
                }

                loadPayments();
                if (tenantComboBox.getValue() != null) {

                    displaySelectedTenantDetails(
                            tenantComboBox.getValue()
                    );

                }

                paymentTable.refresh();

                recordCountLabel.setText(
                        paymentList.size()
                                + (paymentList.size() == 1
                                ? " record"
                                : " records")
                );

                handleClear(null);

                showFormSuccess(
                        "Payment recorded successfully."
                );

            }

        } catch (SQLException exception) {

            showFormError(
                    "Database error while recording the payment."
            );

            exception.printStackTrace();
        }

    }

    private void showFormSuccess(String message) {

        formMessageLabel.setStyle("-fx-text-fill: #1c655c;");

        formMessageLabel.setText(message);

    }

    public void handleUpdatePayment(ActionEvent actionEvent) {

        Payment selectedPayment =
                paymentTable.getSelectionModel().getSelectedItem();

        if (selectedPayment == null) {
            showFormError("Please select a payment to update.");
            return;
        }

        Tenant selectedTenant =
                tenantComboBox.getValue();

        String billingMonth =
                (String) billingMonthComboBox.getValue();

        String amountText =
                amountField.getText().trim();

        LocalDate paymentDate =
                paymentDatePicker.getValue();

        String paymentMethod =
                (String) paymentMethodComboBox.getValue();

        String paymentStatus =
                (String) statusComboBox.getValue();

        String referenceNumber =
                referenceNumberField.getText().trim();

        String remarks =
                remarksArea.getText().trim();

        if (selectedTenant == null
                || billingMonth == null
                || amountText.isEmpty()
                || paymentDate == null
                || paymentMethod == null
                || paymentStatus == null) {

            showFormError(
                    "Please complete all required payment information."
            );
            return;
        }

        double amount;

        try {

            amount = Double.parseDouble(amountText);

        } catch (NumberFormatException exception) {

            showFormError(
                    "Payment amount must be a valid number."
            );

            return;
        }

        if (amount <= 0) {

            showFormError(
                    "Payment amount must be greater than zero."
            );

            return;
        }

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {

            if (paymentExistsForAnotherRecord(
                    connection,
                    selectedTenant.getTenantId(),
                    billingMonth,
                    selectedPayment.getPaymentId()
            )) {

                showFormError(
                        "This tenant already has a recorded payment for the selected billing month."
                );

                return;
            }

            String sql =
                    """
                    UPDATE payments
                    SET
                        tenant_id = ?,
                        billing_month = ?,
                        amount = ?,
                        payment_date = ?,
                        payment_method = ?,
                        reference_number = ?,
                        remarks = ?,
                        status = ?
                    WHERE payment_id = ?
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(sql)
            ) {

                statement.setInt(1,
                        selectedTenant.getTenantId());

                statement.setString(2,
                        billingMonth);

                statement.setDouble(3,
                        amount);

                statement.setDate(
                        4,
                        Date.valueOf(paymentDate)
                );

                statement.setString(
                        5,
                        paymentMethod
                );

                if (referenceNumber.isBlank()) {

                    statement.setNull(
                            6,
                            Types.VARCHAR
                    );

                } else {

                    statement.setString(
                            6,
                            referenceNumber
                    );
                }

                if (remarks.isBlank()) {

                    statement.setNull(
                            7,
                            Types.VARCHAR
                    );

                } else {

                    statement.setString(
                            7,
                            remarks
                    );
                }

                statement.setString(
                        8,
                        paymentStatus
                );

                statement.setInt(
                        9,
                        selectedPayment.getPaymentId()
                );

                int rows =
                        statement.executeUpdate();

                if (rows == 0) {

                    showFormError(
                            "The selected payment could not be updated."
                    );

                    return;
                }

                loadPayments();
                if (tenantComboBox.getValue() != null) {

                    displaySelectedTenantDetails(
                            tenantComboBox.getValue()
                    );

                }

                paymentTable.refresh();

                handleClear(null);

                showFormSuccess(
                        "Payment updated successfully."
                );

            }

        } catch (SQLException exception) {

            showFormError(
                    "Database error while updating the payment."
            );

            exception.printStackTrace();
        }

    }

    private boolean paymentExistsForAnotherRecord(
            Connection connection,
            int tenantId,
            String billingMonth,
            int paymentId
    ) throws SQLException {

        String sql =
                """
                SELECT payment_id
                FROM payments
                WHERE tenant_id = ?
                  AND billing_month = ?
                  AND status = 'PAID'
                  AND payment_id <> ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, tenantId);
            statement.setString(2, billingMonth);
            statement.setInt(3, paymentId);

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                return resultSet.next();

            }

        }

    }

    public void handleDeletePayment(ActionEvent actionEvent) {

        Payment selectedPayment =
                paymentTable.getSelectionModel().getSelectedItem();

        if (selectedPayment == null) {

            showFormError(
                    "Please select a payment to void."
            );

            return;
        }

        Alert confirmation =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Void Payment");

        confirmation.setHeaderText("Confirm Void Payment");

        confirmation.setContentText(
                "Are you sure you want to void this payment?\n\n"
                        + "This action cannot be undone."
        );

        Optional<ButtonType> result =
                confirmation.showAndWait();

        if (result.isEmpty()
                || result.get() != ButtonType.OK) {

            return;
        }

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {

            String sql =
                    """
                    UPDATE payments
                    SET status = 'VOID'
                    WHERE payment_id = ?
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(sql)
            ) {

                statement.setInt(
                        1,
                        selectedPayment.getPaymentId()
                );

                int rows =
                        statement.executeUpdate();

                if (rows == 0) {

                    showFormError(
                            "Unable to void the selected payment."
                    );

                    return;
                }

                loadPayments();
                if (tenantComboBox.getValue() != null) {

                    displaySelectedTenantDetails(
                            tenantComboBox.getValue()
                    );

                }

                paymentTable.refresh();

                handleClear(null);

                showFormSuccess(
                        "Payment has been voided successfully."
                );

            }

        } catch (SQLException exception) {

            showFormError(
                    "Database error while voiding the payment."
            );

            exception.printStackTrace();
        }

    }

    public void handleClear(ActionEvent actionEvent) {

        paymentTable.getSelectionModel().clearSelection();

        paymentIdField.clear();

        tenantComboBox.setValue(null);

        billingMonthComboBox.setValue(null);

        roomField.clear();

        amountField.clear();

        paymentDatePicker.setValue(null);

        paymentMethodComboBox.setValue(null);

        currentBalanceField.clear();

        referenceNumberField.clear();

        remarksArea.clear();

        statusComboBox.setValue("PAID");

        formMessageLabel.setText("");

    }

    public void handleRefresh(ActionEvent actionEvent) {

        searchField.clear();

        filterStatusComboBox.setValue("ALL");

        loadPayments();
        if (tenantComboBox.getValue() != null) {

            displaySelectedTenantDetails(
                    tenantComboBox.getValue()
            );

        }

        paymentTable.setItems(paymentList);

        paymentTable.refresh();

        handleClear(null);

        showFormSuccess(
                "Payment records refreshed."
        );

    }

    public void handleSearch(ActionEvent actionEvent) {

        applyFilters();

    }

    public void openDashboard(ActionEvent actionEvent) {

        try {
            MainApplication.changeScene(
                    "admin-dashboard-view.fxml"
            );

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void openTenantView(ActionEvent actionEvent) {

        try {
            MainApplication.changeScene(
                    "tenant-view.fxml"
            );

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void openRoomView(ActionEvent actionEvent) {

        try {
            MainApplication.changeScene(
                    "room-view.fxml"
            );

        } catch (IOException exception) {
            exception.printStackTrace();
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

    public void initialize() {

        tenantComboBox.setItems(activeTenantList);

        paymentMethodComboBox.getItems().addAll(
                "CASH",
                "GCASH",
                "BANK TRANSFER"
        );

        statusComboBox.getItems().addAll(
                "PAID",
                "VOID"
        );

        statusComboBox.setValue("PAID");

        filterStatusComboBox.getItems().addAll(
                "ALL",
                "PAID",
                "VOID"
        );

        filterStatusComboBox.setValue("ALL");

        billingMonthComboBox.getItems().addAll(
                "January 2026",
                "February 2026",
                "March 2026",
                "April 2026",
                "May 2026",
                "June 2026",
                "July 2026",
                "August 2026",
                "September 2026",
                "October 2026",
                "November 2026",
                "December 2026"
        );

        loadActiveTenants();

        tenantComboBox.valueProperty().addListener(
                (observable, oldTenant, selectedTenant) -> {

                    if (selectedTenant != null) {
                        displaySelectedTenantDetails(selectedTenant);
                    } else {
                        roomField.clear();
                        currentBalanceField.clear();
                    }
                }
        );
        paymentIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("paymentId")
        );

        tenantNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue()
                                .getTenant()
                                .getFullName()
                )
        );

        roomColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(

                        cellData.getValue()
                                .getTenant()
                                .getAssignedRoom() == null

                                ? "Not Assigned"

                                : cellData.getValue()
                                .getTenant()
                                .getAssignedRoom()
                                .getRoomNumber()
                )
        );

        billingMonthColumn.setCellValueFactory(
                new PropertyValueFactory<>("billingMonth")
        );

        amountColumn.setCellValueFactory(
                new PropertyValueFactory<>("amount")
        );

        paymentDateColumn.setCellValueFactory(
                new PropertyValueFactory<>("paymentDate")
        );

        paymentMethodColumn.setCellValueFactory(
                new PropertyValueFactory<>("paymentMethod")
        );

        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        paymentTable.setItems(paymentList);

        loadPayments();
        if (tenantComboBox.getValue() != null) {

            displaySelectedTenantDetails(
                    tenantComboBox.getValue()
            );

        }

        paymentTable.getSelectionModel()
                .selectedItemProperty()
                .addListener(

                        (observable,
                         oldPayment,
                         selectedPayment) -> {

                            if (selectedPayment != null) {

                                populatePaymentForm(
                                        selectedPayment
                                );
                            }

                        }

                );

        searchField.textProperty().addListener(

                (observable,
                 oldValue,
                 newValue) -> applyFilters()

        );

        filterStatusComboBox.valueProperty().addListener(

                (observable,
                 oldValue,
                 newValue) -> applyFilters()

        );
    }

    private double calculateCurrentBalance(
            Connection connection,
            int tenantId
    ) throws SQLException {

        String sql =
                """
                SELECT
                    r.monthly_rate,
                    COALESCE(SUM(
                        CASE
                            WHEN p.status = 'PAID'
                            THEN p.amount
                            ELSE 0
                        END
                    ),0) AS total_paid
                FROM tenants t
    
                LEFT JOIN rooms r
                    ON t.room_id = r.room_id
    
                LEFT JOIN payments p
                    ON t.tenant_id = p.tenant_id
    
                WHERE t.tenant_id = ?
    
                GROUP BY r.monthly_rate
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    tenantId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (resultSet.next()) {

                    double monthlyRate =
                            resultSet.getDouble("monthly_rate");

                    double totalPaid =
                            resultSet.getDouble("total_paid");

                    double balance =
                            monthlyRate - totalPaid;

                    return Math.max(balance, 0);

                }

            }

        }

        return 0;
    }

    private void populatePaymentForm(
            Payment payment
    ) {

        paymentIdField.setText(
                String.valueOf(
                        payment.getPaymentId()
                )
        );

        tenantComboBox.setValue(
                payment.getTenant()
        );

        billingMonthComboBox.setValue(
                payment.getBillingMonth()
        );

        amountField.setText(
                String.valueOf(
                        payment.getAmount()
                )
        );

        paymentDatePicker.setValue(
                payment.getPaymentDate()
        );

        paymentMethodComboBox.setValue(
                payment.getPaymentMethod()
        );

        referenceNumberField.setText(
                payment.getReferenceNumber()
                        == null
                        ? ""
                        : payment.getReferenceNumber()
        );

        remarksArea.setText(
                payment.getRemarks()
                        == null
                        ? ""
                        : payment.getRemarks()
        );

        statusComboBox.setValue(
                payment.getStatus()
        );

        displaySelectedTenantDetails(
                payment.getTenant()
        );

    }

    private void loadPayments() {

        paymentList.clear();

        String sql =
                """
                SELECT
                    p.*,
                    t.full_name,
                    t.room_id,
                    r.room_number
                FROM payments p
    
                JOIN tenants t
                  ON p.tenant_id = t.tenant_id
    
                LEFT JOIN rooms r
                  ON t.room_id = r.room_id
    
                ORDER BY p.payment_date DESC
                """;

        try (

                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()

        ) {

            while (resultSet.next()) {

                Room room = null;

                if (resultSet.getObject("room_id") != null) {

                    room = new Room();

                    room.setRoomId(
                            resultSet.getInt("room_id")
                    );

                    room.setRoomNumber(
                            resultSet.getString("room_number")
                    );
                }

                Tenant tenant = new Tenant();

                tenant.setTenantId(
                        resultSet.getInt("tenant_id")
                );

                tenant.setFullName(
                        resultSet.getString("full_name")
                );

                tenant.setAssignedRoom(room);

                tenant.setBalance(0.0);

                tenant.setStatus("ACTIVE");

                paymentList.add(

                        new Payment(

                                resultSet.getInt("payment_id"),

                                tenant,

                                resultSet.getString("billing_month"),

                                resultSet.getDouble("amount"),

                                resultSet.getDate("payment_date")
                                        .toLocalDate(),

                                resultSet.getString("payment_method"),

                                resultSet.getString("reference_number"),

                                resultSet.getString("remarks"),

                                resultSet.getString("status")
                        )
                );
            }

            recordCountLabel.setText(
                    paymentList.size() + " records"
            );

        } catch (SQLException exception) {

            showFormError(
                    "Unable to load payment records."
            );

            exception.printStackTrace();
        }
    }

    private boolean paymentAlreadyExists(
            Connection connection,
            int tenantId,
            String billingMonth
    ) throws SQLException {

        String sql =
                """
                SELECT payment_id
                FROM payments
                WHERE tenant_id = ?
                  AND billing_month = ?
                  AND status = 'PAID'
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    tenantId
            );

            statement.setString(
                    2,
                    billingMonth
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                return resultSet.next();

            }

        }

    }

    private void loadActiveTenants() {

        activeTenantList.clear();

        String sql =
                """
                SELECT
                    t.tenant_id,
                    u.user_id,
                    t.full_name,
                    u.username,
                    t.contact_number,
                    t.room_id,
                    r.room_number
                FROM tenants t
                JOIN users u
                    ON t.user_id = u.user_id
                LEFT JOIN rooms r
                    ON t.room_id = r.room_id
                WHERE t.status = 'ACTIVE'
                  AND u.account_status = 'ACTIVE'
                ORDER BY t.full_name
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {

                Room assignedRoom = null;

                int roomId =
                        resultSet.getInt("room_id");

                if (!resultSet.wasNull()) {

                    assignedRoom = new Room();

                    assignedRoom.setRoomId(roomId);

                    assignedRoom.setRoomNumber(
                            resultSet.getString("room_number")
                    );
                }

                Tenant tenant = new Tenant(
                        resultSet.getInt("tenant_id"),
                        resultSet.getInt("user_id"),
                        resultSet.getString("full_name"),
                        resultSet.getString("username"),
                        "",
                        resultSet.getString("contact_number"),
                        assignedRoom,
                        0.0,
                        "ACTIVE"
                );

                activeTenantList.add(tenant);
            }

        } catch (SQLException exception) {

            showFormError(
                    "Unable to load active tenants."
            );

            exception.printStackTrace();
        }
    }

    private void showFormError(String message) {

        formMessageLabel.setStyle(
                "-fx-text-fill: #d32f2f;"
        );

        formMessageLabel.setText(message);

    }

    private void displaySelectedTenantDetails(
            Tenant tenant
    ) {

        if (tenant.getAssignedRoom() != null) {

            roomField.setText(
                    tenant.getAssignedRoom()
                            .getRoomNumber()
            );

        } else {

            roomField.setText(
                    "Not Assigned"
            );

        }

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {

            double balance =
                    calculateCurrentBalance(
                            connection,
                            tenant.getTenantId()
                    );

            tenant.setBalance(balance);

            currentBalanceField.setText(

                    String.format(
                            "₱ %,.2f",
                            balance
                    )

            );

        } catch (SQLException exception) {

            currentBalanceField.setText(
                    "Unavailable"
            );

        }

    }

    private void applyFilters() {

        String keyword =
                searchField.getText()
                        .trim()
                        .toLowerCase();

        String selectedStatus =
                filterStatusComboBox.getValue() == null
                        ? "ALL"
                        : filterStatusComboBox.getValue().toString();

        ObservableList<Payment> filteredPayments =
                FXCollections.observableArrayList();

        for (Payment payment : paymentList) {

            boolean matchesKeyword =

                    payment.getTenant()
                            .getFullName()
                            .toLowerCase()
                            .contains(keyword)

                            ||

                            payment.getBillingMonth()
                                    .toLowerCase()
                                    .contains(keyword)

                            ||

                            (
                                    payment.getReferenceNumber() != null

                                            &&

                                            payment.getReferenceNumber()
                                                    .toLowerCase()
                                                    .contains(keyword)
                            );

            boolean matchesStatus =

                    selectedStatus.equalsIgnoreCase("ALL")

                            ||

                            payment.getStatus()
                                    .equalsIgnoreCase(selectedStatus);

            if (matchesKeyword && matchesStatus) {

                filteredPayments.add(payment);

            }

        }

        paymentTable.setItems(filteredPayments);

        recordCountLabel.setText(

                filteredPayments.size()

                        + (filteredPayments.size() == 1
                        ? " record"
                        : " records")

        );

    }
}
