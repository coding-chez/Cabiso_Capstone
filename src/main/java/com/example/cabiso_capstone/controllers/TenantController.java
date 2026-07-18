package com.example.cabiso_capstone.controllers;

import com.example.cabiso_capstone.MainApplication;
import com.example.cabiso_capstone.database.DatabaseConnection;
import com.example.cabiso_capstone.model.Room;
import com.example.cabiso_capstone.model.Tenant;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import javafx.application.Platform;

import java.sql.*;

import javafx.collections.transformation.FilteredList;
import java.util.Locale;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class TenantController {

    public TextField searchField;
    public ComboBox<String> statusFilterComboBox;
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

    private final ObservableList<Tenant> tenantList = FXCollections.observableArrayList();
    private FilteredList<Tenant> filteredTenantList;

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

        statusComboBox.getItems().addAll("PENDING", "ACTIVE", "INACTIVE");

        statusFilterComboBox.getItems().addAll("ALL", "PENDING", "ACTIVE", "INACTIVE");

        statusFilterComboBox.setValue("ALL");

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters()
        );

        statusFilterComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters()
        );

        tenantTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        filteredTenantList = new FilteredList<>(
                tenantList,
                tenant -> true
        );

        tenantTable.setItems(filteredTenantList);

        tenantTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldTenant, selectedTenant) -> {

                    if (selectedTenant != null) {
                        populateTenantForm(selectedTenant);
                    }
                });

        loadTenants();

        Platform.runLater(() -> {
            tenantTable.refresh();
            tenantTable.requestLayout();
        });
    }

    private void applyFilters() {

        String searchText = searchField.getText();

        if (searchText == null) {
            searchText = "";
        }

        String normalizedSearch =
                searchText.trim().toLowerCase(Locale.ROOT);

        String selectedStatus =
                statusFilterComboBox.getValue();

        if (selectedStatus == null) {
            selectedStatus = "ALL";
        }

        String finalSelectedStatus = selectedStatus;

        filteredTenantList.setPredicate(tenant -> {

            boolean matchesSearch;

            if (normalizedSearch.isEmpty()) {
                matchesSearch = true;

            } else {
                String fullName =
                        safeLower(tenant.getFullName());

                String username =
                        safeLower(tenant.getUsername());

                String contactNumber =
                        safeLower(tenant.getContactNumber());

                String roomNumber =
                        safeLower(tenant.getAssignedRoomNumber());

                matchesSearch =
                        fullName.contains(normalizedSearch)
                                || username.contains(normalizedSearch)
                                || contactNumber.contains(normalizedSearch)
                                || roomNumber.contains(normalizedSearch);
            }

            boolean matchesStatus =
                    finalSelectedStatus.equalsIgnoreCase("ALL")
                            || safeLower(tenant.getStatus())
                            .equals(
                                    finalSelectedStatus.toLowerCase(Locale.ROOT)
                            );

            return matchesSearch && matchesStatus;
        });

        updateRecordCount();
    }

    private String safeLower(String value) {

        if (value == null) {
            return "";
        }

        return value.toLowerCase(Locale.ROOT);
    }

    private void updateRecordCount() {

        int visibleRecords = filteredTenantList.size();
        int totalRecords = tenantList.size();

        if (visibleRecords == totalRecords) {

            recordCountLabel.setText(
                    totalRecords
                            + (totalRecords == 1
                            ? " record"
                            : " records")
            );

        } else {
            recordCountLabel.setText(
                    visibleRecords
                            + " of "
                            + totalRecords
                            + " records"
            );
        }
    }

    private void populateTenantForm(Tenant tenant) {

        tenantIdField.setText(
                String.valueOf(tenant.getTenantId())
        );

        fullNameField.setText(
                tenant.getFullName()
        );

        usernameField.setText(
                tenant.getUsername()
        );

        passwordField.clear();
        passwordField.setPromptText(
                "Leave blank to keep current password"
        );

        contactField.setText(
                tenant.getContactNumber()
        );

        balanceField.setText(
                String.valueOf(tenant.getBalance())
        );

        statusComboBox.setValue(
                tenant.getStatus()
        );

        if (tenant.getAssignedRoom() != null) {
            roomComboBox.setValue(
                    tenant.getAssignedRoom()
            );
        } else {
            roomComboBox.setValue(null);
        }

        formMessageLabel.setText("");
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

//            recordCountLabel.setText(
//                    tenantList.size() + " records"
//            );
            applyFilters();

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

        searchField.clear();
        statusFilterComboBox.setValue("ALL");

        loadTenants();

        tenantTable.getSelectionModel().clearSelection();
        clearFormFields();

        formMessageLabel.setStyle(
                "-fx-text-fill: #1c655c;"
        );

        formMessageLabel.setText(
                "Tenant records refreshed."
        );
    }

    private void clearFormFields() {

        tenantIdField.clear();
        fullNameField.clear();
        usernameField.clear();
        passwordField.clear();
        contactField.clear();
        roomComboBox.setValue(null);
        balanceField.clear();
        statusComboBox.setValue(null);
    }

    public void handleAddTenant(ActionEvent actionEvent) {

    }

    public void handleUpdateTenant(ActionEvent actionEvent) {

        Tenant selectedTenant =
                tenantTable.getSelectionModel().getSelectedItem();

        if (selectedTenant == null) {
            showFormError(
                    "Please select a tenant to update."
            );
            return;
        }

        String fullName =
                fullNameField.getText().trim();

        String username =
                usernameField.getText().trim();

        String newPassword =
                passwordField.getText();

        String contactNumber =
                contactField.getText().trim();

        String selectedStatus =
                statusComboBox.getValue();

        Room selectedRoom =
                roomComboBox.getValue();

        if (fullName.isEmpty()
                || username.isEmpty()
                || contactNumber.isEmpty()
                || selectedStatus == null) {

            showFormError(
                    "Please complete the required tenant information."
            );
            return;
        }

        boolean changingPassword =
                newPassword != null
                        && !newPassword.isBlank();

        String updateUserSql;

        if (changingPassword) {
            updateUserSql =
                    "UPDATE users "
                            + "SET username = ?, "
                            + "password = ?, "
                            + "account_status = ? "
                            + "WHERE user_id = ?";
        } else {
            updateUserSql =
                    "UPDATE users "
                            + "SET username = ?, "
                            + "account_status = ? "
                            + "WHERE user_id = ?";
        }

        String updateTenantSql =
                "UPDATE tenants "
                        + "SET full_name = ?, "
                        + "contact_number = ?, "
                        + "room_id = ?, "
                        + "status = ? "
                        + "WHERE tenant_id = ?";

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {

            if (usernameBelongsToAnotherUser(
                    connection,
                    username,
                    selectedTenant.getUserId()
            )) {
                showFormError(
                        "That username is already used by another account."
                );
                return;
            }

            connection.setAutoCommit(false);

            try (
                    PreparedStatement userStatement =
                            connection.prepareStatement(
                                    updateUserSql
                            );

                    PreparedStatement tenantStatement =
                            connection.prepareStatement(
                                    updateTenantSql
                            )
            ) {

                userStatement.setString(
                        1,
                        username
                );

                if (changingPassword) {

                    userStatement.setString(
                            2,
                            newPassword
                    );

                    userStatement.setString(
                            3,
                            selectedStatus
                    );

                    userStatement.setInt(
                            4,
                            selectedTenant.getUserId()
                    );

                } else {

                    userStatement.setString(
                            2,
                            selectedStatus
                    );

                    userStatement.setInt(
                            3,
                            selectedTenant.getUserId()
                    );
                }

                tenantStatement.setString(
                        1,
                        fullName
                );

                tenantStatement.setString(
                        2,
                        contactNumber
                );

                if (selectedRoom == null) {
                    tenantStatement.setNull(
                            3,
                            Types.INTEGER
                    );
                } else {
                    tenantStatement.setInt(
                            3,
                            selectedRoom.getRoomId()
                    );
                }

                tenantStatement.setString(
                        4,
                        selectedStatus
                );

                tenantStatement.setInt(
                        5,
                        selectedTenant.getTenantId()
                );

                int userRows =
                        userStatement.executeUpdate();

                int tenantRows =
                        tenantStatement.executeUpdate();

                if (userRows == 0 || tenantRows == 0) {
                    connection.rollback();

                    showFormError(
                            "The selected tenant could not be updated."
                    );
                    return;
                }

                connection.commit();

                loadTenants();

                tenantTable
                        .getSelectionModel()
                        .clearSelection();

                clearFormFields();

                showFormSuccess(
                        fullName
                                + " was updated successfully."
                );

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }

        } catch (SQLException exception) {
            showFormError(
                    "Database error while updating the tenant."
            );

            exception.printStackTrace();
        }
    }

    private boolean usernameBelongsToAnotherUser(Connection connection, String username, int currentUserId) throws SQLException {

        String sql =
                "SELECT user_id "
                        + "FROM users "
                        + "WHERE username = ? "
                        + "AND user_id <> ?";

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    username
            );

            statement.setInt(
                    2,
                    currentUserId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {
                return resultSet.next();
            }
        }
    }

    private void showFormError(String message) {
        formMessageLabel.setStyle(
                "-fx-text-fill: #bd4338;"
        );

        formMessageLabel.setText(message);
    }

    private void showFormSuccess(String message) {
        formMessageLabel.setStyle(
                "-fx-text-fill: #1c655c;"
        );

        formMessageLabel.setText(message);
    }

    public void handleDeleteTenant(ActionEvent actionEvent) {

    }

    public void handleClear(ActionEvent actionEvent) {

        tenantTable
                .getSelectionModel()
                .clearSelection();

        clearFormFields();

        formMessageLabel.setText("");
    }

    public void handleSearch(ActionEvent actionEvent) {

        applyFilters();

        formMessageLabel.setStyle(
                "-fx-text-fill: #1c655c;"
        );

        formMessageLabel.setText(
                filteredTenantList.size()
                        + " matching tenant record(s) found."
        );
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

    public void handleDeactivateTenant(
            ActionEvent actionEvent
    ) {

        Tenant selectedTenant =
                tenantTable.getSelectionModel().getSelectedItem();

        if (selectedTenant == null) {
            showFormError(
                    "Please select a tenant to deactivate."
            );
            return;
        }

        if (selectedTenant
                .getStatus()
                .equalsIgnoreCase("INACTIVE")) {

            showFormError(
                    "This tenant is already inactive."
            );
            return;
        }

        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmation.setTitle(
                "Deactivate Tenant"
        );

        confirmation.setHeaderText(
                "Deactivate "
                        + selectedTenant.getFullName()
                        + "?"
        );

        confirmation.setContentText(
                "This tenant will no longer be able to log in. "
                        + "Their records will remain in the system."
        );

        confirmation.showAndWait().ifPresent(
                response -> {

                    if (response == ButtonType.OK) {
                        deactivateTenant(
                                selectedTenant
                        );
                    }
                }
        );
    }

    private void deactivateTenant(Tenant tenant) {

        String updateUserSql =
                "UPDATE users "
                        + "SET account_status = 'INACTIVE' "
                        + "WHERE user_id = ?";

        String updateTenantSql =
                "UPDATE tenants "
                        + "SET status = 'INACTIVE' "
                        + "WHERE tenant_id = ?";

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {

            connection.setAutoCommit(false);

            try (
                    PreparedStatement userStatement =
                            connection.prepareStatement(
                                    updateUserSql
                            );

                    PreparedStatement tenantStatement =
                            connection.prepareStatement(
                                    updateTenantSql
                            )
            ) {

                userStatement.setInt(
                        1,
                        tenant.getUserId()
                );

                tenantStatement.setInt(
                        1,
                        tenant.getTenantId()
                );

                int userRows =
                        userStatement.executeUpdate();

                int tenantRows =
                        tenantStatement.executeUpdate();

                if (userRows == 0 || tenantRows == 0) {
                    connection.rollback();

                    showFormError(
                            "The selected tenant could not be deactivated."
                    );
                    return;
                }

                connection.commit();

                loadTenants();

                tenantTable
                        .getSelectionModel()
                        .clearSelection();

                clearFormFields();

                showFormSuccess(
                        tenant.getFullName()
                                + " has been deactivated."
                );

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }

        } catch (SQLException exception) {
            showFormError(
                    "Database error while deactivating the tenant."
            );

            exception.printStackTrace();
        }
    }
}