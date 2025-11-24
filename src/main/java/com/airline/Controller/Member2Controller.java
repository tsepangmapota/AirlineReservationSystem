package com.airline.Controller;

import com.airline.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.*;


public class Member2Controller {

    @FXML private Label totalFlightsLabel;
    @FXML private Label activeReservationsLabel;
    @FXML private Label availableSeatsLabel;
    @FXML private TableView<Flight> flightTable;
    @FXML private TableColumn<Flight, String> colFlightId;
    @FXML private TableColumn<Flight, String> colFlightCode;
    @FXML private TableColumn<Flight, String> colSource;
    @FXML private TableColumn<Flight, String> colDestination;
    @FXML private TableColumn<Flight, String> colDeparture;
    @FXML private TableColumn<Flight, String> colArrival;
    @FXML private TableColumn<Flight, Integer> colCapacity;
    @FXML private TableColumn<Flight, Integer> colAvailableSeats;
    @FXML private TextField searchField;

    // Flight inner class
    public static class Flight {
        private final StringProperty flightId;
        private final StringProperty flightCode;
        private final StringProperty flightName;
        private final IntegerProperty executiveSeats;
        private final IntegerProperty economySeats;
        private final IntegerProperty capacity;
        private final IntegerProperty availableSeats;

        public Flight(String flightId, String flightCode, String flightName,
                      int executiveSeats, int economySeats) {
            this.flightId = new SimpleStringProperty(flightId);
            this.flightCode = new SimpleStringProperty(flightCode);
            this.flightName = new SimpleStringProperty(flightName);
            this.executiveSeats = new SimpleIntegerProperty(executiveSeats);
            this.economySeats = new SimpleIntegerProperty(economySeats);
            this.capacity = new SimpleIntegerProperty(executiveSeats + economySeats);
            this.availableSeats = new SimpleIntegerProperty(executiveSeats + economySeats);
        }

        // Getter methods
        public String getFlightId() { return flightId.get(); }
        public String getFlightCode() { return flightCode.get(); }
        public String getFlightName() { return flightName.get(); }
        public int getExecutiveSeats() { return executiveSeats.get(); }
        public int getEconomySeats() { return economySeats.get(); }
        public int getCapacity() { return capacity.get(); }
        public int getAvailableSeats() { return availableSeats.get(); }

        // Property methods
        public StringProperty flightIdProperty() { return flightId; }
        public StringProperty flightCodeProperty() { return flightCode; }
        public StringProperty flightNameProperty() { return flightName; }
        public IntegerProperty executiveSeatsProperty() { return executiveSeats; }
        public IntegerProperty economySeatsProperty() { return economySeats; }
        public IntegerProperty capacityProperty() { return capacity; }
        public IntegerProperty availableSeatsProperty() { return availableSeats; }
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        loadFlightData();
        updateStatistics();
    }

    private void setupTableColumns() {
        colFlightId.setCellValueFactory(cellData -> cellData.getValue().flightIdProperty());
        colFlightCode.setCellValueFactory(cellData -> cellData.getValue().flightCodeProperty());

        // Hide columns that don't exist in your database
        colSource.setVisible(false);
        colDestination.setVisible(false);
        colDeparture.setVisible(false);
        colArrival.setVisible(false);

        colCapacity.setCellValueFactory(cellData -> cellData.getValue().capacityProperty().asObject());
        colAvailableSeats.setCellValueFactory(cellData -> cellData.getValue().availableSeatsProperty().asObject());
    }

    private void loadFlightData() {
        try {
            String query = "SELECT f_code, f_name, COALESCE(t_exe_seatno, 0) as executive_seats, " +
                    "COALESCE(t_eco_seatno, 0) as economy_seats " +
                    "FROM flight_information ORDER BY f_code";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement statement = conn.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                ObservableList<Flight> flights = FXCollections.observableArrayList();
                int flightCount = 1;

                while (resultSet.next()) {
                    Flight flight = new Flight(
                            String.valueOf(flightCount++),
                            resultSet.getString("f_code"),
                            resultSet.getString("f_name"),
                            resultSet.getInt("executive_seats"),
                            resultSet.getInt("economy_seats")
                    );
                    flights.add(flight);
                }
                flightTable.setItems(flights);

                System.out.println("Loaded " + flights.size() + " flights successfully!");

            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to load flights: " + e.getMessage());
            e.printStackTrace();
            loadSampleData(); // Load sample data if database fails
        }
    }

    private void loadSampleData() {
        ObservableList<Flight> flights = FXCollections.observableArrayList();

        // Sample data for demonstration
        flights.add(new Flight("1", "AI101", "Air India Express", 20, 100));
        flights.add(new Flight("2", "SG202", "SpiceJet", 15, 80));
        flights.add(new Flight("3", "IG303", "IndiGo", 25, 120));
        flights.add(new Flight("4", "UK404", "Vistara", 30, 150));

        flightTable.setItems(flights);
        showInfo("Demo Mode", "Loaded sample flight data for demonstration.");
    }

    @FXML
    private void handleAddFlight() {
        Dialog<Flight> dialog = new Dialog<>();
        dialog.setTitle("Add New Flight");
        dialog.setHeaderText("Enter Flight Details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField flightCodeField = new TextField();
        flightCodeField.setPromptText("AI101");
        TextField flightNameField = new TextField();
        flightNameField.setPromptText("Air India Express");
        TextField execSeatsField = new TextField();
        execSeatsField.setPromptText("20");
        TextField ecoSeatsField = new TextField();
        ecoSeatsField.setPromptText("100");

        grid.add(new Label("Flight Code:"), 0, 0);
        grid.add(flightCodeField, 1, 0);
        grid.add(new Label("Flight Name:"), 0, 1);
        grid.add(flightNameField, 1, 1);
        grid.add(new Label("Executive Seats:"), 0, 2);
        grid.add(execSeatsField, 1, 2);
        grid.add(new Label("Economy Seats:"), 0, 3);
        grid.add(ecoSeatsField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String flightCode = flightCodeField.getText().toUpperCase().trim();
                    String flightName = flightNameField.getText().trim();
                    int execSeats = Integer.parseInt(execSeatsField.getText());
                    int ecoSeats = Integer.parseInt(ecoSeatsField.getText());

                    if (flightCode.isEmpty() || flightName.isEmpty()) {
                        showError("Validation Error", "Flight code and name are required.");
                        return null;
                    }

                    // Insert into database
                    String insertQuery = "INSERT INTO flight_information (f_code, f_name, t_exe_seatno, t_eco_seatno) VALUES (?, ?, ?, ?)";
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

                        stmt.setString(1, flightCode);
                        stmt.setString(2, flightName);
                        stmt.setInt(3, execSeats);
                        stmt.setInt(4, ecoSeats);

                        int rowsAffected = stmt.executeUpdate();
                        if (rowsAffected > 0) {
                            showInfo("Success", "Flight added successfully!");
                            loadFlightData();
                            updateStatistics();
                        }
                    }
                } catch (NumberFormatException e) {
                    showError("Input Error", "Please enter valid numbers for seats.");
                } catch (SQLException e) {
                    if (e.getMessage().contains("duplicate key")) {
                        showError("Error", "Flight code already exists. Please use a different code.");
                    } else {
                        showError("Database Error", "Failed to add flight: " + e.getMessage());
                    }
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleUpdateFlight() {
        Flight selectedFlight = flightTable.getSelectionModel().getSelectedItem();
        if (selectedFlight == null) {
            showError("Selection Error", "Please select a flight to update.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Update Flight");
        dialog.setHeaderText("Update Flight: " + selectedFlight.getFlightCode());

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField flightNameField = new TextField(selectedFlight.getFlightName());
        TextField execSeatsField = new TextField(String.valueOf(selectedFlight.getExecutiveSeats()));
        TextField ecoSeatsField = new TextField(String.valueOf(selectedFlight.getEconomySeats()));

        grid.add(new Label("Flight Name:"), 0, 0);
        grid.add(flightNameField, 1, 0);
        grid.add(new Label("Executive Seats:"), 0, 1);
        grid.add(execSeatsField, 1, 1);
        grid.add(new Label("Economy Seats:"), 0, 2);
        grid.add(ecoSeatsField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                try {
                    String updateQuery = "UPDATE flight_information SET f_name = ?, t_exe_seatno = ?, t_eco_seatno = ? WHERE f_code = ?";
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

                        stmt.setString(1, flightNameField.getText());
                        stmt.setInt(2, Integer.parseInt(execSeatsField.getText()));
                        stmt.setInt(3, Integer.parseInt(ecoSeatsField.getText()));
                        stmt.setString(4, selectedFlight.getFlightCode());

                        int rowsAffected = stmt.executeUpdate();
                        if (rowsAffected > 0) {
                            showInfo("Success", "Flight updated successfully!");
                            loadFlightData();
                            updateStatistics();
                        } else {
                            showError("Update Error", "Flight not found in database.");
                        }
                    }
                } catch (NumberFormatException e) {
                    showError("Input Error", "Please enter valid numbers for seats.");
                } catch (SQLException e) {
                    showError("Database Error", "Failed to update flight: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleDeleteFlight() {
        Flight selectedFlight = flightTable.getSelectionModel().getSelectedItem();
        if (selectedFlight == null) {
            showError("Selection Error", "Please select a flight to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Flight");
        confirmation.setContentText("Are you sure you want to delete flight " + selectedFlight.getFlightCode() + "?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String deleteQuery = "DELETE FROM flight_information WHERE f_code = ?";
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {

                        stmt.setString(1, selectedFlight.getFlightCode());
                        int rowsAffected = stmt.executeUpdate();

                        if (rowsAffected > 0) {
                            showInfo("Success", "Flight deleted successfully!");
                            loadFlightData();
                            updateStatistics();
                        } else {
                            showError("Delete Error", "Flight not found in database.");
                        }
                    }
                } catch (SQLException e) {
                    showError("Database Error", "Failed to delete flight: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleSearchFlights() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadFlightData();
            return;
        }

        try {
            String searchQuery = "SELECT f_code, f_name, COALESCE(t_exe_seatno, 0) as executive_seats, " +
                    "COALESCE(t_eco_seatno, 0) as economy_seats " +
                    "FROM flight_information " +
                    "WHERE f_code ILIKE ? OR f_name ILIKE ? " +
                    "ORDER BY f_code";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(searchQuery)) {

                String likeTerm = "%" + searchTerm + "%";
                stmt.setString(1, likeTerm);
                stmt.setString(2, likeTerm);

                ResultSet resultSet = stmt.executeQuery();
                ObservableList<Flight> flights = FXCollections.observableArrayList();
                int flightCount = 1;

                while (resultSet.next()) {
                    Flight flight = new Flight(
                            String.valueOf(flightCount++),
                            resultSet.getString("f_code"),
                            resultSet.getString("f_name"),
                            resultSet.getInt("executive_seats"),
                            resultSet.getInt("economy_seats")
                    );
                    flights.add(flight);
                }
                flightTable.setItems(flights);

                if (flights.isEmpty()) {
                    showInfo("Search Results", "No flights found matching: " + searchTerm);
                }
            }
        } catch (SQLException e) {
            showError("Search Error", "Failed to search flights: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadFlightData();
        updateStatistics();
        searchField.clear();
        showInfo("Refresh", "Data refreshed successfully!");
    }

    private void updateStatistics() {
        try {
            // Total flights count
            String totalFlightsQuery = "SELECT COUNT(*) as total FROM flight_information";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(totalFlightsQuery);
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    totalFlightsLabel.setText(String.valueOf(rs.getInt("total")));
                }
            }

            // Active reservations count
            String activeReservationsQuery = "SELECT COUNT(*) as active FROM reservations WHERE status = 'CONFIRMED'";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(activeReservationsQuery);
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    activeReservationsLabel.setText(String.valueOf(rs.getInt("active")));
                }
            }

            // Total available seats
            String availableSeatsQuery = "SELECT SUM(COALESCE(t_exe_seatno, 0) + COALESCE(t_eco_seatno, 0)) as total_seats FROM flight_information";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(availableSeatsQuery);
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    availableSeatsLabel.setText(String.valueOf(rs.getInt("total_seats")));
                }
            }

        } catch (SQLException e) {
            // Set default values if database is not available
            totalFlightsLabel.setText(String.valueOf(flightTable.getItems().size()));
            activeReservationsLabel.setText("0");

            int totalSeats = flightTable.getItems().stream()
                    .mapToInt(Flight::getCapacity)
                    .sum();
            availableSeatsLabel.setText(String.valueOf(totalSeats));
        }
    }

    @FXML
    private void goBackToMain() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/airline/view/main-dashboard.fxml"));
            Stage stage = (Stage) totalFlightsLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showError("Navigation Error", "Failed to return to main dashboard: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
