package com.example.cabiso_capstone.controllers;

import com.example.cabiso_capstone.MainApplication;
import com.example.cabiso_capstone.database.DatabaseConnection;
import com.example.cabiso_capstone.session.SessionManager;
import com.example.cabiso_capstone.session.UserSession;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;

import java.time.LocalDate;

public class AdminDashboardController {

    public Button dashboardButton;

    public Label totalTenantsLabel;
    public Label availableRoomsLabel;
    public Label pendingPaymentsLabel;

    public Label occupiedRoomsLabel;
    public Label totalRoomsLabel;
    public Label totalPaymentsLabel;

    public Label adminNameLabel;
    public Label occupiedBedsLabel;
    public Label availableSlotsLabel;
    public Label occupancyRateLabel;

    public Label monthlyRevenueLabel;
    public Label totalRevenueLabel;
    public Label voidPaymentsLabel;
    public TableView<RecentTenantActivity> recentTenantTable;

    public TableColumn<RecentTenantActivity, String> recentTenantNameColumn;

    public TableColumn<RecentTenantActivity, String> recentTenantRoomColumn;

    public TableColumn<RecentTenantActivity, String> recentTenantStatusColumn;


    public TableView<RecentPaymentActivity> recentPaymentTable;

    public TableColumn<RecentPaymentActivity, String> recentPaymentTenantColumn;

    public TableColumn<RecentPaymentActivity, String> recentPaymentAmountColumn;

    public TableColumn<RecentPaymentActivity, String> recentPaymentDateColumn;

    public TableColumn<RecentPaymentActivity, String> recentPaymentStatusColumn;
    private final ObservableList<RecentTenantActivity> recentTenantActivities = FXCollections.observableArrayList();
    private final ObservableList<RecentPaymentActivity> recentPaymentActivities = FXCollections.observableArrayList();

    private Timeline dashboardRefreshTimeline;
    @FXML
    private Label currentDateLabel;

    @FXML
    private Label currentTimeLabel;

    public void initialize() {

        if (!validateSession()) {
            return;
        }

        initializeRecentActivityTables();
        loadLoggedInAdministrator();

        refreshDashboardData();
        startDashboardAutoRefresh();
    }

    private void loadRecentPaymentActivity() {

        recentPaymentActivities.clear();

        String sql =
                """
                SELECT
                    t.full_name,
                    p.amount,
                    p.payment_date,
                    p.status
                FROM payments p
                INNER JOIN tenants t
                    ON p.tenant_id = t.tenant_id
                ORDER BY
                    p.payment_date DESC,
                    p.payment_id DESC
                LIMIT 5
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

                LocalDate paymentDate = null;

                if (resultSet.getDate("payment_date") != null) {
                    paymentDate =
                            resultSet.getDate(
                                    "payment_date"
                            ).toLocalDate();
                }

                recentPaymentActivities.add(
                        new RecentPaymentActivity(
                                resultSet.getString(
                                        "full_name"
                                ),
                                resultSet.getDouble(
                                        "amount"
                                ),
                                paymentDate,
                                resultSet.getString(
                                        "status"
                                )
                        )
                );
            }

            recentPaymentTable.setItems(
                    recentPaymentActivities
            );

            recentPaymentTable.refresh();

            System.out.println(
                    "Recent payments loaded: "
                            + recentPaymentActivities.size()
            );

        } catch (SQLException exception) {

            System.err.println(
                    "Unable to load recent payment activity."
            );

            exception.printStackTrace();
        }
    }
    private void loadCurrentDateTime() {

        LocalDateTime now = LocalDateTime.now();

        currentDateLabel.setText(
                now.format(
                        DateTimeFormatter.ofPattern(
                                "MMMM dd, yyyy"
                        )
                )
        );

        currentTimeLabel.setText(
                now.format(
                        DateTimeFormatter.ofPattern(
                                "hh:mm a"
                        )
                )
        );
    }

    private void loadRecentTenantActivity() {

        recentTenantActivities.clear();

        String sql =
                """
                SELECT
                    t.full_name,
                    COALESCE(r.room_number, 'Not Assigned')
                        AS room_number,
                    t.status
                FROM tenants t
                LEFT JOIN rooms r
                    ON t.room_id = r.room_id
                ORDER BY t.tenant_id DESC
                LIMIT 5
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

                recentTenantActivities.add(
                        new RecentTenantActivity(
                                resultSet.getString(
                                        "full_name"
                                ),
                                resultSet.getString(
                                        "room_number"
                                ),
                                resultSet.getString(
                                        "status"
                                )
                        )
                );
            }

            recentTenantTable.setItems(
                    recentTenantActivities
            );

            recentTenantTable.refresh();

            System.out.println(
                    "Recent tenants loaded: "
                            + recentTenantActivities.size()
            );

        } catch (SQLException exception) {

            System.err.println(
                    "Unable to load recent tenant activity."
            );

            exception.printStackTrace();
        }
    }

    private void initializeRecentActivityTables() {

        recentTenantNameColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getTenantName()
                )
        );

        recentTenantRoomColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getRoomNumber()
                )
        );

        recentTenantStatusColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getStatus()
                )
        );

        recentTenantTable.setItems(
                recentTenantActivities
        );

        recentTenantTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );


        recentPaymentTenantColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getTenantName()
                )
        );

        recentPaymentAmountColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.format(
                                "₱%,.2f",
                                data.getValue().getAmount()
                        )
                )
        );

        recentPaymentDateColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getPaymentDate() == null
                                ? "No Date"
                                : data.getValue()
                                .getPaymentDate()
                                .toString()
                )
        );

        recentPaymentStatusColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getStatus()
                )
        );

        recentPaymentTable.setItems(
                recentPaymentActivities
        );

        recentPaymentTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
    }

    private void loadFinancialSummary() {

        String sql =
                """
                SELECT
                    COALESCE(
                        SUM(
                            CASE
                                WHEN status = 'PAID'
                                 AND YEAR(payment_date) = YEAR(CURDATE())
                                 AND MONTH(payment_date) = MONTH(CURDATE())
                                THEN amount
                                ELSE 0
                            END
                        ),
                        0
                    ) AS monthly_revenue,
    
                    COALESCE(
                        SUM(
                            CASE
                                WHEN status = 'PAID'
                                THEN amount
                                ELSE 0
                            END
                        ),
                        0
                    ) AS total_revenue,
    
                    SUM(
                        CASE
                            WHEN status = 'PAID'
                            THEN 1
                            ELSE 0
                        END
                    ) AS paid_transactions,
    
                    SUM(
                        CASE
                            WHEN status = 'VOID'
                            THEN 1
                            ELSE 0
                        END
                    ) AS void_transactions
    
                FROM payments
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

                double monthlyRevenue =
                        resultSet.getDouble(
                                "monthly_revenue"
                        );

                double totalRevenue =
                        resultSet.getDouble(
                                "total_revenue"
                        );

                int paidTransactions =
                        resultSet.getInt(
                                "paid_transactions"
                        );

                int voidTransactions =
                        resultSet.getInt(
                                "void_transactions"
                        );

                monthlyRevenueLabel.setText(
                        String.format(
                                "₱%,.2f",
                                monthlyRevenue
                        )
                );

                totalRevenueLabel.setText(
                        String.format(
                                "₱%,.2f",
                                totalRevenue
                        )
                );

                totalPaymentsLabel.setText(
                        String.valueOf(
                                paidTransactions
                        )
                );

                voidPaymentsLabel.setText(
                        String.valueOf(
                                voidTransactions
                        )
                );
            }

        } catch (SQLException exception) {

            monthlyRevenueLabel.setText("₱0.00");
            totalRevenueLabel.setText("₱0.00");
            totalPaymentsLabel.setText("0");
            voidPaymentsLabel.setText("0");

            System.err.println(
                    "Unable to load financial summary."
            );

            exception.printStackTrace();
        }
    }

    private void loadOccupancySummary() {

        String sql =
                """
                SELECT
                    (
                        SELECT COUNT(DISTINCT t.room_id)
                        FROM tenants t
                        WHERE t.room_id IS NOT NULL
                          AND t.status = 'ACTIVE'
                    ) AS occupied_rooms,
    
                    (
                        SELECT COUNT(*)
                        FROM rooms
                    ) AS total_rooms,
    
                    (
                        SELECT COUNT(*)
                        FROM tenants
                        WHERE room_id IS NOT NULL
                          AND status = 'ACTIVE'
                    ) AS occupied_beds,
    
                    (
                        SELECT COALESCE(SUM(capacity), 0)
                        FROM rooms
                        WHERE status IN ('AVAILABLE', 'FULL')
                    ) AS total_capacity
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

                int occupiedRooms =
                        resultSet.getInt("occupied_rooms");

                int totalRooms =
                        resultSet.getInt("total_rooms");

                int occupiedBeds =
                        resultSet.getInt("occupied_beds");

                int totalCapacity =
                        resultSet.getInt("total_capacity");

                int availableSlots =
                        Math.max(
                                totalCapacity - occupiedBeds,
                                0
                        );

                double occupancyRate =
                        totalCapacity == 0
                                ? 0.0
                                : (
                                occupiedBeds * 100.0
                                / totalCapacity
                        );

                occupiedRoomsLabel.setText(
                        String.valueOf(occupiedRooms)
                );

                totalRoomsLabel.setText(
                        String.valueOf(totalRooms)
                );

                occupiedBedsLabel.setText(
                        String.valueOf(occupiedBeds)
                );

                availableSlotsLabel.setText(
                        String.valueOf(availableSlots)
                );

                occupancyRateLabel.setText(
                        String.format(
                                "%.1f%%",
                                occupancyRate
                        )
                );
            }

        } catch (SQLException exception) {

            occupiedRoomsLabel.setText("0");
            totalRoomsLabel.setText("0");
            occupiedBedsLabel.setText("0");
            availableSlotsLabel.setText("0");
            occupancyRateLabel.setText("0%");

            System.err.println(
                    "Unable to load occupancy summary."
            );

            exception.printStackTrace();
        }
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

        stopDashboardAutoRefresh();

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

    public void openRoomView(ActionEvent actionEvent) {

        stopDashboardAutoRefresh();

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

        refreshDashboardData();
    }

    public void openTenantView(
            ActionEvent actionEvent
    ) {

        stopDashboardAutoRefresh();

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

        stopDashboardAutoRefresh();

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

    public void handleRefreshDashboard(
            ActionEvent actionEvent
    ) {

        refreshDashboardData();

        System.out.println(
                "Administrator dashboard refreshed."
        );
    }
    private void startDashboardAutoRefresh() {

        dashboardRefreshTimeline =
                new Timeline(
                        new KeyFrame(
                                Duration.seconds(30),
                                event ->
                                        refreshDashboardData()
                        )
                );

        dashboardRefreshTimeline.setCycleCount(
                Timeline.INDEFINITE
        );

        dashboardRefreshTimeline.play();
    }

    private void refreshDashboardData() {

        loadDashboardStatistics();
        loadOccupancySummary();
        loadFinancialSummary();

        loadRecentTenantActivity();
        loadRecentPaymentActivity();
        loadCurrentDateTime();
    }
    private void stopDashboardAutoRefresh() {

        if (dashboardRefreshTimeline != null) {
            dashboardRefreshTimeline.stop();
        }
    }

    public static class RecentPaymentActivity {

        private final String tenantName;
        private final double amount;
        private final LocalDate paymentDate;
        private final String status;

        public RecentPaymentActivity(
                String tenantName,
                double amount,
                LocalDate paymentDate,
                String status
        ) {
            this.tenantName =
                    tenantName;

            this.amount =
                    amount;

            this.paymentDate =
                    paymentDate;

            this.status =
                    status;
        }

        public String getTenantName() {
            return tenantName;
        }

        public double getAmount() {
            return amount;
        }

        public LocalDate getPaymentDate() {
            return paymentDate;
        }

        public String getStatus() {
            return status;
        }
    }

    public static class RecentTenantActivity {

        private final String tenantName;
        private final String roomNumber;
        private final String status;

        public RecentTenantActivity(
                String tenantName,
                String roomNumber,
                String status
        ) {
            this.tenantName =
                    tenantName;

            this.roomNumber =
                    roomNumber;

            this.status =
                    status;
        }

        public String getTenantName() {
            return tenantName;
        }

        public String getRoomNumber() {
            return roomNumber;
        }

        public String getStatus() {
            return status;
        }
    }
}