package com.example.cabiso_capstone.controllers;

import com.example.cabiso_capstone.MainApplication;
import com.example.cabiso_capstone.database.DatabaseConnection;
import com.example.cabiso_capstone.model.Room;
import com.example.cabiso_capstone.model.Tenant;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import javafx.application.Platform;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TenantController {

    public TextField searchField;

    public TableView<Tenant> tenantTable;

    public TableColumn<Tenant, Integer> tenantIdColumn;
    public TableColumn<Tenant, String> fullNameColumn;
    public TableColumn<Tenant, String> usernameColumn;
    public TableColumn<Tenant, String> contactColumn;
    public TableColumn<Tenant, String> roomColumn;
    public TableColumn<Tenant, Double> balanceColumn;
    public TableColumn<Tenant, String> statusColumn;

    public Label recordCountLabel;
    public Label formMessageLabel;

    public TextField tenantIdField;
    public TextField fullNameField;
    public TextField usernameField;
    public PasswordField passwordField;
    public TextField contactField;
    public ComboBox<Room> roomComboBox;
    public TextField balanceField;
    public ComboBox<String> statusComboBox;

    private final ObservableList<Tenant> tenantList =
            FXCollections.observableArrayList();

    public void initialize() {

        tenantIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("tenantId")
        );

        fullNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("fullName")
        );

        usernameColumn.setCellValueFactory(
                new PropertyValueFactory<>("username")
        );

        contactColumn.setCellValueFactory(
                new PropertyValueFactory<>("contactNumber")
        );

        roomColumn.setCellValueFactory(
                new PropertyValueFactory<>("assignedRoomNumber")
        );

        balanceColumn.setCellValueFactory(
                new PropertyValueFactory<>("balance")
        );

        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        statusComboBox.getItems().addAll(
                "PENDING",
                "ACTIVE",
                "INACTIVE"
        );

        tenantTable.setItems(tenantList);

        loadTenants();

        Platform.runLater(() -> {
            tenantTable.refresh();
            tenantTable.requestLayout();
        });
    }

    private void loadTenants() {

        tenantList.clear();

        String sql =
                "SELECT "
                        + "t.tenant_id, "
                        + "u.user_id, "
                        + "t.full_name, "
                        + "u.username, "
                        + "u.password, "
                        + "t.contact_number, "
                        + "t.status, "
                        + "t.room_id, "
                        + "r.room_number "
                        + "FROM tenants t "
                        + "JOIN users u ON t.user_id = u.user_id "
                        + "LEFT JOIN rooms r ON t.room_id = r.room_id "
                        + "ORDER BY t.tenant_id DESC";

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

                int roomId = resultSet.getInt("room_id");

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
                        resultSet.getString("password"),
                        resultSet.getString("contact_number"),
                        assignedRoom,
                        0.0,
                        resultSet.getString("status")
                );

                tenantList.add(tenant);
            }

            recordCountLabel.setText(
                    tenantList.size() + " records"
            );

        } catch (SQLException exception) {

            formMessageLabel.setStyle(
                    "-fx-text-fill: red;"
            );

            formMessageLabel.setText(
                    "Unable to load tenant records."
            );

            exception.printStackTrace();
        }
    }

    public void handleRefresh(ActionEvent actionEvent) {
        loadTenants();

        formMessageLabel.setStyle(
                "-fx-text-fill: green;"
        );

        formMessageLabel.setText(
                "Tenant records refreshed."
        );
    }

    public void handleAddTenant(ActionEvent actionEvent) {

    }

    public void handleUpdateTenant(ActionEvent actionEvent) {

    }

    public void handleDeleteTenant(ActionEvent actionEvent) {

    }

    public void handleClear(ActionEvent actionEvent) {

    }

    public void handleSearch(ActionEvent actionEvent) {

    }

    public void handleLogout(ActionEvent actionEvent) {
        try {
            MainApplication.changeScene("login-view.fxml");
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

    public void openRoomView(ActionEvent actionEvent) {
        try {
            MainApplication.changeScene("room-view.fxml");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
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

    public void handleApproveTenant(ActionEvent actionEvent) {

        Tenant selectedTenant =
                tenantTable.getSelectionModel().getSelectedItem();

        if (selectedTenant == null) {
            formMessageLabel.setStyle("-fx-text-fill: red;");
            formMessageLabel.setText(
                    "Please select a tenant to approve."
            );
            return;
        }

        if (selectedTenant.getStatus().equalsIgnoreCase("ACTIVE")) {
            formMessageLabel.setStyle("-fx-text-fill: orange;");
            formMessageLabel.setText(
                    "This tenant is already active."
            );
            return;
        }

        String updateUserSql =
                "UPDATE users "
                        + "SET account_status = 'ACTIVE' "
                        + "WHERE user_id = ?";

        String updateTenantSql =
                "UPDATE tenants "
                        + "SET status = 'ACTIVE' "
                        + "WHERE tenant_id = ?";

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try (
                    PreparedStatement userStatement =
                            connection.prepareStatement(updateUserSql);

                    PreparedStatement tenantStatement =
                            connection.prepareStatement(updateTenantSql)
            ) {

                userStatement.setInt(
                        1,
                        selectedTenant.getUserId()
                );

                tenantStatement.setInt(
                        1,
                        selectedTenant.getTenantId()
                );

                int userRows =
                        userStatement.executeUpdate();

                int tenantRows =
                        tenantStatement.executeUpdate();

                if (userRows == 0 || tenantRows == 0) {
                    connection.rollback();

                    formMessageLabel.setStyle(
                            "-fx-text-fill: red;"
                    );

                    formMessageLabel.setText(
                            "Unable to approve the selected tenant."
                    );

                    return;
                }

                connection.commit();

                formMessageLabel.setStyle(
                        "-fx-text-fill: green;"
                );

                formMessageLabel.setText(
                        selectedTenant.getFullName()
                                + " has been approved."
                );

                loadTenants();

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }

        } catch (SQLException exception) {
            formMessageLabel.setStyle(
                    "-fx-text-fill: red;"
            );

            formMessageLabel.setText(
                    "Database error while approving tenant."
            );

            exception.printStackTrace();
        }
    }
}