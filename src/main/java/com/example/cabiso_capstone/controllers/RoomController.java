package com.example.cabiso_capstone.controllers;

import com.example.cabiso_capstone.MainApplication;
import com.example.cabiso_capstone.database.DatabaseConnection;
import com.example.cabiso_capstone.facade.ApplicationFacade;
import com.example.cabiso_capstone.model.Room;
import com.example.cabiso_capstone.session.SessionManager;
import com.example.cabiso_capstone.session.UserSession;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;
import java.util.function.UnaryOperator;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class RoomController {

    public TextField searchField;
    public ComboBox<String> filterStatusComboBox;

    public TableView<Room> roomTable;

    public TableColumn<Room, Integer> roomIdColumn;
    public TableColumn<Room, String> roomNumberColumn;
    public TableColumn<Room, Integer> capacityColumn;
    public TableColumn<Room, Integer> occupantsColumn;
    public TableColumn<Room, Integer> availableSlotsColumn;
    public TableColumn<Room, Double> monthlyRateColumn;
    public TableColumn<Room, String> statusColumn;

    public Label recordCountLabel;
    public Label formMessageLabel;

    public TextField roomIdField;
    public TextField roomNumberField;
    public TextField capacityField;
    public TextField occupantsField;
    public TextField monthlyRateField;
    public ComboBox<String> statusComboBox;

    private final ObservableList<Room> roomList =
            FXCollections.observableArrayList();

    public void initialize() {
        if (!validateAdminSession()) {
            return;
        }

        roomIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("roomId")
        );

        roomNumberColumn.setCellValueFactory(
                new PropertyValueFactory<>("roomNumber")
        );

        capacityColumn.setCellValueFactory(
                new PropertyValueFactory<>("capacity")
        );

        occupantsColumn.setCellValueFactory(
                new PropertyValueFactory<>("occupants")
        );

        availableSlotsColumn.setCellValueFactory(
                new PropertyValueFactory<>("availableSlots")
        );

        monthlyRateColumn.setCellValueFactory(
                new PropertyValueFactory<>("monthlyRate")
        );

        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        roomTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        filterStatusComboBox.getItems().addAll(
                "ALL",
                "AVAILABLE",
                "MAINTENANCE",
                "INACTIVE"
        );

        filterStatusComboBox.setValue("ALL");

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

        statusComboBox.getItems().addAll(
                "AVAILABLE",
                "MAINTENANCE",
                "INACTIVE"
        );

        roomTable.setItems(roomList);

        roomTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldRoom, selectedRoom) -> {

                    if (selectedRoom != null) {
                        populateRoomForm(selectedRoom);
                    }
                });

        loadRooms();

        Platform.runLater(() -> {
            roomTable.refresh();
            roomTable.requestLayout();
        });
        setupInputRestrictions();
    }

    private void setupInputRestrictions() {
        UnaryOperator<TextFormatter.Change> roomFilter = change -> {

            if (change.getControlNewText()
                    .matches("[A-Za-z0-9\\- ]*")) {

                return change;
            }

            return null;
        };

        roomNumberField.setTextFormatter(
                new TextFormatter<>(roomFilter)
        );

        UnaryOperator<TextFormatter.Change> capacityFilter = change -> {

            if (change.getControlNewText()
                    .matches("\\d*")) {

                return change;
            }

            return null;
        };

        capacityField.setTextFormatter(
                new TextFormatter<>(capacityFilter)
        );

        UnaryOperator<TextFormatter.Change> rateFilter = change -> {

            if (change.getControlNewText()
                    .matches("\\d*(\\.\\d{0,2})?")) {

                return change;
            }

            return null;
        };

        monthlyRateField.setTextFormatter(
                new TextFormatter<>(rateFilter)
        );
    }

    private void populateRoomForm(Room room) {

        roomIdField.setText(
                String.valueOf(room.getRoomId())
        );

        roomNumberField.setText(
                room.getRoomNumber()
        );

        capacityField.setText(
                String.valueOf(room.getCapacity())
        );

        occupantsField.setText(
                String.valueOf(room.getOccupants())
        );

        monthlyRateField.setText(
                String.valueOf(room.getMonthlyRate())
        );

        if ("FULL".equalsIgnoreCase(room.getStatus())) {
            statusComboBox.setValue("AVAILABLE");
        } else {
            statusComboBox.setValue(room.getStatus());
        }

        formMessageLabel.setText("");
    }

    private void loadRooms() {

        roomList.clear();

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

                int capacity =
                        resultSet.getInt("capacity");

                int occupants =
                        resultSet.getInt("occupants");

                String storedStatus =
                        resultSet.getString("status");

                String displayStatus =
                        calculateDisplayStatus(
                                storedStatus,
                                capacity,
                                occupants
                        );

                Room room = new Room(
                        resultSet.getInt("room_id"),
                        resultSet.getString("room_number"),
                        capacity,
                        occupants,
                        resultSet.getDouble("monthly_rate"),
                        displayStatus
                );

                roomList.add(room);
            }

            updateRecordCount();

        } catch (SQLException exception) {

            showFormError(
                    "Unable to load room records."
            );

            exception.printStackTrace();
        }
    }

    private String calculateDisplayStatus(
            String storedStatus,
            int capacity,
            int occupants
    ) {

        if ("MAINTENANCE".equalsIgnoreCase(storedStatus)) {
            return "MAINTENANCE";
        }

        if ("INACTIVE".equalsIgnoreCase(storedStatus)) {
            return "INACTIVE";
        }

        if (occupants >= capacity) {
            return "FULL";
        }

        return "AVAILABLE";
    }

    private void updateRecordCount() {

        int totalRecords = roomList.size();

        recordCountLabel.setText(
                totalRecords
                        + (totalRecords == 1
                        ? " record"
                        : " records")
        );
    }

    public void handleRefresh(ActionEvent actionEvent) {

        searchField.clear();
        filterStatusComboBox.setValue("ALL");

        loadRooms();
        roomTable.setItems(roomList);

        roomTable
                .getSelectionModel()
                .clearSelection();

        clearFormFields();

        showFormSuccess(
                "Room records refreshed."
        );
    }

    public void handleSearch(ActionEvent actionEvent) {

        applyFilters();

    }

    public void handleAddRoom(ActionEvent actionEvent) {

        String roomNumber =
                roomNumberField.getText().trim();

        String capacityText =
                capacityField.getText().trim();

        String monthlyRateText =
                monthlyRateField.getText().trim();

        String selectedStatus =
                statusComboBox.getValue();

        if (roomNumber.isEmpty()
                || capacityText.isEmpty()
                || monthlyRateText.isEmpty()
                || selectedStatus == null) {

            showFormError(
                    "Please complete all required room information."
            );
            return;
        }

        int capacity;
        double monthlyRate;

        try {

            capacity =
                    Integer.parseInt(capacityText);

            monthlyRate =
                    Double.parseDouble(monthlyRateText);

        } catch (NumberFormatException exception) {

            showFormError(
                    "Capacity and monthly rate must be valid numbers."
            );
            return;
        }

        if (capacity <= 0) {

            showFormError(
                    "Capacity must be greater than zero."
            );
            return;
        }

        if (monthlyRate <= 0) {

            showFormError(
                    "Monthly rate must be greater than zero."
            );
            return;
        }

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {

            if (roomNumberExists(
                    connection,
                    roomNumber
            )) {

                showFormError(
                        "That room number already exists."
                );
                return;
            }

            String sql =
                    """
                    INSERT INTO rooms
                    (
                        room_number,
                        capacity,
                        monthly_rate,
                        status
                    )
                    VALUES
                    (?, ?, ?, ?)
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(sql)
            ) {

                statement.setString(
                        1,
                        roomNumber
                );

                statement.setInt(
                        2,
                        capacity
                );

                statement.setDouble(
                        3,
                        monthlyRate
                );

                statement.setString(
                        4,
                        selectedStatus
                );

                int rows =
                        statement.executeUpdate();

                if (rows > 0) {

                    loadRooms();

                    clearFormFields();

                    roomTable
                            .getSelectionModel()
                            .clearSelection();

                    showFormSuccess(
                            "Room "
                                    + roomNumber
                                    + " added successfully."
                    );
                }

            }

        } catch (SQLException exception) {

            showFormError(
                    "Unable to add the room."
            );

            exception.printStackTrace();
        }
    }

    private void applyFilters() {

        String keyword =
                searchField
                        .getText()
                        .trim()
                        .toLowerCase();

        String selectedStatus =
                filterStatusComboBox.getValue();

        ObservableList<Room> filteredRooms =
                FXCollections.observableArrayList();

        for (Room room : roomList) {

            boolean matchesRoom =

                    room.getRoomNumber()
                            .toLowerCase()
                            .contains(keyword);

            boolean matchesStatus =

                    selectedStatus == null
                            || selectedStatus.equals("ALL")
                            || room.getStatus()
                            .equalsIgnoreCase(selectedStatus);

            if (matchesRoom && matchesStatus) {

                filteredRooms.add(room);

            }
        }

        roomTable.setItems(filteredRooms);

        recordCountLabel.setText(

                filteredRooms.size()

                        + (filteredRooms.size() == 1
                        ? " record"
                        : " records")

        );
    }

    private boolean roomNumberExists(
            Connection connection,
            String roomNumber
    ) throws SQLException {

        String sql =
                """
                SELECT room_id
                FROM rooms
                WHERE room_number = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    roomNumber
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                return resultSet.next();
            }
        }
    }

    public void handleUpdateRoom(ActionEvent actionEvent) {

        Room selectedRoom =
                roomTable.getSelectionModel().getSelectedItem();

        if (selectedRoom == null) {
            showFormError(
                    "Please select a room to update."
            );
            return;
        }

        String roomNumber =
                roomNumberField.getText().trim();

        String capacityText =
                capacityField.getText().trim();

        String monthlyRateText =
                monthlyRateField.getText().trim();

        String selectedStatus =
                statusComboBox.getValue();

        if (roomNumber.isEmpty()
                || capacityText.isEmpty()
                || monthlyRateText.isEmpty()
                || selectedStatus == null) {

            showFormError(
                    "Please complete all required room information."
            );
            return;
        }

        int capacity;
        double monthlyRate;

        try {
            capacity = Integer.parseInt(capacityText);
            monthlyRate = Double.parseDouble(monthlyRateText);

        } catch (NumberFormatException exception) {
            showFormError(
                    "Capacity and monthly rate must be valid numbers."
            );
            return;
        }

        if (capacity <= 0) {
            showFormError(
                    "Capacity must be greater than zero."
            );
            return;
        }

        if (monthlyRate <= 0) {
            showFormError(
                    "Monthly rate must be greater than zero."
            );
            return;
        }

        if (capacity < selectedRoom.getOccupants()) {
            showFormError(
                    "Capacity cannot be lower than the current number of occupants."
            );
            return;
        }

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {

            if (roomNumberBelongsToAnotherRoom(
                    connection,
                    roomNumber,
                    selectedRoom.getRoomId()
            )) {

                showFormError(
                        "That room number already exists."
                );
                return;
            }

            String sql =
                    """
                    UPDATE rooms
                    SET
                        room_number = ?,
                        capacity = ?,
                        monthly_rate = ?,
                        status = ?
                    WHERE room_id = ?
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(sql)
            ) {

                statement.setString(
                        1,
                        roomNumber
                );

                statement.setInt(
                        2,
                        capacity
                );

                statement.setDouble(
                        3,
                        monthlyRate
                );

                statement.setString(
                        4,
                        selectedStatus
                );

                statement.setInt(
                        5,
                        selectedRoom.getRoomId()
                );

                int rows =
                        statement.executeUpdate();

                if (rows == 0) {
                    showFormError(
                            "The selected room could not be updated."
                    );
                    return;
                }

                loadRooms();

                roomTable
                        .getSelectionModel()
                        .clearSelection();

                clearFormFields();

                showFormSuccess(
                        "Room "
                                + roomNumber
                                + " updated successfully."
                );
            }

        } catch (SQLException exception) {

            showFormError(
                    "Database error while updating the room."
            );

            exception.printStackTrace();
        }
    }

    private boolean roomNumberBelongsToAnotherRoom(
            Connection connection,
            String roomNumber,
            int currentRoomId
    ) throws SQLException {

        String sql =
                """
                SELECT room_id
                FROM rooms
                WHERE room_number = ?
                  AND room_id <> ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    roomNumber
            );

            statement.setInt(
                    2,
                    currentRoomId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {
                return resultSet.next();
            }
        }
    }

    private boolean roomHasActiveTenants(
            Connection connection,
            int roomId
    ) throws SQLException {

        String sql =
                """
                SELECT COUNT(*)
                FROM tenants
                WHERE room_id = ?
                  AND status = 'ACTIVE'
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

                resultSet.next();

                return resultSet.getInt(1) > 0;
            }
        }
    }

    public void handleDeleteRoom(ActionEvent actionEvent) {

        Room selectedRoom =
                roomTable.getSelectionModel().getSelectedItem();

        if (selectedRoom == null) {

            showFormError(
                    "Please select a room to deactivate."
            );

            return;
        }

        Alert confirmation =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Deactivate Room");

        confirmation.setHeaderText(
                "Deactivate "
                        + selectedRoom.getRoomNumber()
                        + "?"
        );

        confirmation.setContentText(
                "The room will no longer be available for assignment.\n\n"
                        + "Existing room history will be preserved."
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

            if (roomHasActiveTenants(
                    connection,
                    selectedRoom.getRoomId()
            )) {

                showFormError(
                        "This room still has active tenants assigned."
                );

                return;
            }

            String sql =
                    """
                    UPDATE rooms
                    SET status = 'INACTIVE'
                    WHERE room_id = ?
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(sql)
            ) {

                statement.setInt(
                        1,
                        selectedRoom.getRoomId()
                );

                int rows =
                        statement.executeUpdate();

                if (rows == 0) {

                    showFormError(
                            "The room could not be deactivated."
                    );

                    return;
                }

                loadRooms();

                roomTable
                        .getSelectionModel()
                        .clearSelection();

                clearFormFields();

                showFormSuccess(
                        "Room "
                                + selectedRoom.getRoomNumber()
                                + " has been deactivated."
                );
            }

        } catch (SQLException exception) {

            showFormError(
                    "Database error while deactivating the room."
            );

            exception.printStackTrace();
        }
    }

    public void handleClear(ActionEvent actionEvent) {

        roomTable
                .getSelectionModel()
                .clearSelection();

        clearFormFields();

        formMessageLabel.setText("");
    }

    private void clearFormFields() {

        roomIdField.clear();
        roomNumberField.clear();
        capacityField.clear();
        occupantsField.clear();
        monthlyRateField.clear();
        statusComboBox.setValue(null);
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

    public void handleLogout(ActionEvent actionEvent) {

        try {

            ApplicationFacade.logout();

        } catch (IOException exception) {

            exception.printStackTrace();
        }
    }

    public void openDashboard(ActionEvent actionEvent) {

        try {

            ApplicationFacade.openAdminDashboard();

        } catch (IOException exception) {

            exception.printStackTrace();
        }
    }

    public void openTenantView(ActionEvent actionEvent) {

        try {

            ApplicationFacade.openTenantManagement();

        } catch (IOException exception) {

            exception.printStackTrace();
        }
    }

    public void openPaymentView(ActionEvent actionEvent) {

        try {

            ApplicationFacade.openPaymentManagement();

        } catch (IOException exception) {

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