package com.example.cabiso_capstone.controllers;

import com.example.cabiso_capstone.MainApplication;
import com.example.cabiso_capstone.database.DatabaseConnection;
import com.example.cabiso_capstone.model.Room;
import com.example.cabiso_capstone.model.Tenant;
import com.example.cabiso_capstone.session.SessionManager;
import com.example.cabiso_capstone.session.UserSession;
import com.example.cabiso_capstone.exceptions.ValidationException;
import com.example.cabiso_capstone.validation.InputValidator;

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
    private final ObservableList<Room> availableRoomList = FXCollections.observableArrayList();

    public void initialize() {
        if (!validateAdminSession()) {
            return;
        }

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
        roomComboBox.setItems(availableRoomList);

        loadAvailableRooms();
        
        loadTenants();

        Platform.runLater(() -> {
            tenantTable.refresh();
            tenantTable.requestLayout();
        });
    }

    private void loadAvailableRooms() {

        availableRoomList.clear();

        String sql =
                """
                SELECT
                    r.room_id,
                    r.room_number,
                    r.capacity,
                    r.monthly_rate,
                    r.status,
                    COUNT(t.tenant_id) AS occupants
                FROM rooms r
                LEFT JOIN tenants t
                       ON r.room_id = t.room_id
                      AND t.status = 'ACTIVE'
                GROUP BY
                    r.room_id,
                    r.room_number,
                    r.capacity,
                    r.monthly_rate,
                    r.status
                ORDER BY r.room_number
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

                int capacity = resultSet.getInt("capacity");
                int occupants = resultSet.getInt("occupants");

                String status = resultSet.getString("status");

                // Automatically determine FULL or AVAILABLE
                if (!status.equalsIgnoreCase("MAINTENANCE")
                        && !status.equalsIgnoreCase("INACTIVE")) {

                    if (occupants >= capacity) {
                        status = "FULL";
                    } else {
                        status = "AVAILABLE";
                    }
                }

                Room room = new Room(

                        resultSet.getInt("room_id"),

                        resultSet.getString("room_number"),

                        capacity,

                        occupants,

                        resultSet.getDouble("monthly_rate"),

                        status
                );

                availableRoomList.add(room);
            }

        } catch (SQLException exception) {

            showFormError(
                    "Unable to load available rooms."
            );

            exception.printStackTrace();
        }
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
            selectCurrentRoom(tenant);
        } else {
            roomComboBox.setValue(null);
        }

        formMessageLabel.setText("");
    }

    private void selectCurrentRoom(Tenant tenant) {

        if (tenant.getAssignedRoom() == null) {

            roomComboBox.setValue(null);
            return;
        }

        for (Room room : availableRoomList) {

            if (room.getRoomId()
                    == tenant.getAssignedRoom().getRoomId()) {

                roomComboBox.setValue(room);
                return;
            }
        }

        roomComboBox.setValue(
                tenant.getAssignedRoom()
        );
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


    public void handleUpdateTenant(ActionEvent actionEvent) {

        Tenant selectedTenant =
                tenantTable.getSelectionModel().getSelectedItem();

        if (selectedTenant == null) {
            showFormError(
                    "Please select a tenant to update."
            );
            return;
        }

        Integer previousRoomId = null;

        if (selectedTenant.getAssignedRoom() != null) {
            previousRoomId =
                    selectedTenant
                            .getAssignedRoom()
                            .getRoomId();
        }

        String fullName =
                fullNameField.getText().trim();

        String username =
                usernameField.getText().trim();

        String newPassword =
                passwordField.getText();

        String contactNumber;

        try {
            contactNumber =
                    InputValidator.validateContactNumber(
                            contactField.getText()
                    );

        } catch (ValidationException exception) {

            formMessageLabel.setStyle(
                    "-fx-text-fill: #c62828;"
            );

            formMessageLabel.setText(
                    exception.getMessage()
            );

            return;
        }

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

        if ("ACTIVE".equalsIgnoreCase(selectedStatus)
                && selectedRoom == null) {

            showFormError(
                    "An active tenant must have an assigned room."
            );
            return;
        }

        if (selectedTenant.getStatus().equalsIgnoreCase("PENDING")
                && "ACTIVE".equalsIgnoreCase(selectedStatus)) {

            showFormError(
                    "Use Approve Tenant to activate a pending account."
            );
            return;
        }

        if ("INACTIVE".equalsIgnoreCase(selectedStatus)
                && selectedRoom != null) {

            showFormError(
                    "An inactive tenant cannot keep an assigned room. "
                            + "Use Deactivate to release the room properly."
            );
            return;
        }

        if (!validateSelectedRoom(
                selectedRoom,
                previousRoomId
        )) {
            return;
        }

        boolean changingRoom =
                selectedRoom != null
                        && (
                        previousRoomId == null
                                || selectedRoom.getRoomId()
                                != previousRoomId
                );

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

            if (changingRoom
                    && !roomHasAvailableSlot(
                    connection,
                    selectedRoom.getRoomId()
            )) {

                showFormError(
                        "The selected room is already full."
                );

                loadAvailableRooms();
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

                if (previousRoomId != null) {
                    updateRoomStatus(
                            connection,
                            previousRoomId
                    );
                }

                if (changingRoom && selectedRoom != null) {
                    updateRoomStatus(
                            connection,
                            selectedRoom.getRoomId()
                    );
                }

                connection.commit();

                loadAvailableRooms();
                loadTenants();

                tenantTable
                        .getSelectionModel()
                        .clearSelection();

                clearFormFields();

                if (changingRoom) {
                    showFormSuccess(
                            fullName
                                    + " was updated and assigned to Room "
                                    + selectedRoom.getRoomNumber()
                                    + "."
                    );
                } else {
                    showFormSuccess(
                            fullName
                                    + " was updated successfully."
                    );
                }

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
            showFormError("Please select a pending tenant.");
            return;
        }

        if (!selectedTenant.getStatus().equalsIgnoreCase("PENDING")) {
            showFormError("Only pending tenants can be approved.");
            return;
        }

        Room selectedRoom = roomComboBox.getValue();
        if (!validateSelectedRoom(
                selectedRoom,
                null
        )) {
            return;
        }

        if (selectedRoom == null) {
            showFormError("Please assign a room before approving.");
            return;
        }

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            if (!roomHasAvailableSlot(connection, selectedRoom.getRoomId())) {
                connection.rollback();

                showFormError(
                        "The selected room is already full."
                );

                loadAvailableRooms();
                return;
            }

            String updateUser =
                    """
                    UPDATE users
                    SET account_status='ACTIVE'
                    WHERE user_id=?
                    """;

            String updateTenant =
                    """
                    UPDATE tenants
                    SET
                        status='ACTIVE',
                        room_id=?
                    WHERE tenant_id=?
                    """;

            try (
                    PreparedStatement userStatement =
                            connection.prepareStatement(updateUser);

                    PreparedStatement tenantStatement =
                            connection.prepareStatement(updateTenant)
            ) {

                userStatement.setInt(
                        1,
                        selectedTenant.getUserId()
                );

                tenantStatement.setInt(
                        1,
                        selectedRoom.getRoomId()
                );

                tenantStatement.setInt(
                        2,
                        selectedTenant.getTenantId()
                );

                userStatement.executeUpdate();
                tenantStatement.executeUpdate();

                updateRoomStatus(
                        connection,
                        selectedRoom.getRoomId()
                );

                connection.commit();

            } catch (SQLException exception) {

                connection.rollback();
                throw exception;
            }

            loadAvailableRooms();
            loadTenants();

            tenantTable.getSelectionModel().clearSelection();
            clearFormFields();

            showFormSuccess(
                    selectedTenant.getFullName()
                            + " approved successfully."
            );

        } catch (SQLException exception) {

            showFormError(
                    "Unable to approve tenant."
            );

            exception.printStackTrace();
        }
    }

    private boolean roomHasAvailableSlot(
            Connection connection,
            int roomId
    ) throws SQLException {

        String sql =
                """
                SELECT
                    r.capacity,
                    COUNT(t.tenant_id) occupants
                FROM rooms r
                LEFT JOIN tenants t
                       ON r.room_id=t.room_id
                      AND t.status='ACTIVE'
                WHERE r.room_id=?
                GROUP BY
                    r.room_id,
                    r.capacity
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, roomId);

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (!resultSet.next())
                    return false;

                return resultSet.getInt("occupants")
                        < resultSet.getInt("capacity");
            }
        }
    }

    private void updateRoomStatus(
            Connection connection,
            int roomId
    ) throws SQLException {

        String sql =
                """
                UPDATE rooms
    
                SET status =
    
                CASE
    
                    WHEN status='MAINTENANCE'
    
                        THEN 'MAINTENANCE'
    
                    WHEN status='INACTIVE'
    
                        THEN 'INACTIVE'
    
                    WHEN (
    
                        SELECT COUNT(*)
    
                        FROM tenants
    
                        WHERE room_id=?
    
                        AND status='ACTIVE'
    
                    ) >= capacity
    
                        THEN 'FULL'
    
                    ELSE 'AVAILABLE'
    
                END
    
                WHERE room_id=?
                """;

        try (

                PreparedStatement statement =
                        connection.prepareStatement(sql)

        ) {

            statement.setInt(1, roomId);
            statement.setInt(2, roomId);

            statement.executeUpdate();
        }
    }

    public void handleDeactivateTenant(ActionEvent actionEvent) {

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
                        + "Their room assignment will be removed, "
                        + "but their records will remain in the system."
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

    private boolean validateSelectedRoom(
            Room selectedRoom,
            Integer currentRoomId
    ) {

        if (selectedRoom == null) {
            return true;
        }

        boolean keepingCurrentRoom =
                currentRoomId != null
                        && selectedRoom.getRoomId()
                        == currentRoomId;

        if (keepingCurrentRoom) {
            return true;
        }

        if (selectedRoom.getStatus().equalsIgnoreCase("FULL")) {
            showFormError(
                    "The selected room is already full."
            );
            return false;
        }

        if (selectedRoom.getStatus().equalsIgnoreCase("MAINTENANCE")) {
            showFormError(
                    "The selected room is currently under maintenance."
            );
            return false;
        }

        if (selectedRoom.getStatus().equalsIgnoreCase("INACTIVE")) {
            showFormError(
                    "The selected room is currently unavailable."
            );
            return false;
        }

        return true;
    }

    private void deactivateTenant(Tenant tenant) {

        Integer previousRoomId = null;

        if (tenant.getAssignedRoom() != null) {
            previousRoomId =
                    tenant.getAssignedRoom().getRoomId();
        }

        String updateUserSql =
                "UPDATE users "
                        + "SET account_status = 'INACTIVE' "
                        + "WHERE user_id = ?";

        String updateTenantSql =
                "UPDATE tenants "
                        + "SET status = 'INACTIVE', "
                        + "room_id = NULL "
                        + "WHERE tenant_id = ?";

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {

            connection.setAutoCommit(false);

            try (
                    PreparedStatement userStatement =
                            connection.prepareStatement(updateUserSql);

                    PreparedStatement tenantStatement =
                            connection.prepareStatement(updateTenantSql)
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

                if (previousRoomId != null) {
                    updateRoomStatus(
                            connection,
                            previousRoomId
                    );
                }

                connection.commit();

                loadAvailableRooms();
                loadTenants();

                tenantTable
                        .getSelectionModel()
                        .clearSelection();

                clearFormFields();

                showFormSuccess(
                        tenant.getFullName()
                                + " has been deactivated and removed from the room."
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
    private boolean validateAdminSession() {

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
                    || !session.isActive()
                    || !session.isAdmin()) {

                SessionManager.deleteSession();

                MainApplication.changeScene(
                        "login-view.fxml"
                );

                return false;
            }

            return true;

        } catch (
                IOException
                | ClassNotFoundException exception
        ) {

            try {
                SessionManager.deleteSession();

                MainApplication.changeScene(
                        "login-view.fxml"
                );

            } catch (IOException ignored) {
            }

            exception.printStackTrace();

            return false;
        }
    }
}