package com.airline.Controller;

import com.airline.database.DatabaseConnection;
import com.airline.model.Reservation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ReservationController implements Initializable {

    @FXML private TextField flightCodeField;
    @FXML private TextField customerCodeField;
    @FXML private ComboBox<String> seatClassCombo;
    @FXML private TextField seatNumberField;
    @FXML private DatePicker travelDateField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField fareField;
    @FXML private TextField customerNameField;

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, Integer> reservationIdColumn;
    @FXML private TableColumn<Reservation, String> flightCodeColumn;
    @FXML private TableColumn<Reservation, Integer> customerCodeColumn;
    @FXML private TableColumn<Reservation, String> seatClassColumn;
    @FXML private TableColumn<Reservation, Integer> seatNumberColumn;
    @FXML private TableColumn<Reservation, LocalDate> travelDateColumn;
    @FXML private TableColumn<Reservation, String> statusColumn;
    @FXML private TableColumn<Reservation, Double> fareColumn;

    private ObservableList<Reservation> reservationData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        initializeComboBoxes();
        loadReservations();
        createReservationsTableIfNotExists();
        loadAvailableFlights();
    }

    private void setupTableColumns() {
        reservationIdColumn.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        flightCodeColumn.setCellValueFactory(new PropertyValueFactory<>("flightCode"));
        customerCodeColumn.setCellValueFactory(new PropertyValueFactory<>("customerCode"));
        seatClassColumn.setCellValueFactory(new PropertyValueFactory<>("seatClass"));
        seatNumberColumn.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));
        travelDateColumn.setCellValueFactory(new PropertyValueFactory<>("travelDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        fareColumn.setCellValueFactory(new PropertyValueFactory<>("fare"));

        reservationTable.setItems(reservationData);
    }

    private void initializeComboBoxes() {
        seatClassCombo.getItems().addAll("ECO", "EXE", "BUS");
        statusCombo.getItems().addAll("CONFIRMED", "WAITING", "CANCELLED");
        seatClassCombo.setValue("ECO");
        statusCombo.setValue("CONFIRMED");
        travelDateField.setValue(LocalDate.now().plusDays(1));
    }

    private void loadAvailableFlights() {
        System.out.println("DEBUG: Loading available flights...");
        String sql = "SELECT f_code, f_name, f_source, f_destination FROM flight_information LIMIT 10";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println("DEBUG: Flight - " + rs.getString("f_code") + ": " +
                        rs.getString("f_source") + " to " + rs.getString("f_destination"));
            }
        } catch (SQLException e) {
            System.err.println("DEBUG: Error loading flights: " + e.getMessage());
        }
    }

    @FXML
    private void handleMakeReservation() {
        try {
            if (validateReservationForm()) {
                Reservation reservation = new Reservation();
                reservation.setFlightCode(flightCodeField.getText().trim().toUpperCase());
                reservation.setCustomerCode(Integer.parseInt(customerCodeField.getText().trim()));
                reservation.setSeatClass(seatClassCombo.getValue());
                reservation.setSeatNumber(Integer.parseInt(seatNumberField.getText().trim()));
                reservation.setTravelDate(travelDateField.getValue());
                reservation.setStatus(statusCombo.getValue());
                reservation.setFare(Double.parseDouble(fareField.getText().trim()));

                if (makeReservation(reservation)) {
                    showAlert("Success", "Reservation made successfully!", "SUCCESS");
                    clearForm();
                    loadReservations();
                } else {
                    showAlert("Error", "Failed to make reservation.", "ERROR");
                }
            }
        } catch (Exception e) {
            showAlert("Error", "An error occurred: " + e.getMessage(), "ERROR");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelReservation() {
        Reservation selectedReservation = reservationTable.getSelectionModel().getSelectedItem();
        if (selectedReservation != null) {
            if (cancelReservation(selectedReservation.getReservationId())) {
                showAlert("Success", "Reservation cancelled successfully!", "SUCCESS");
                loadReservations();
                clearForm();
            } else {
                showAlert("Error", "Failed to cancel reservation.", "ERROR");
            }
        } else {
            showAlert("Error", "Please select a reservation to cancel.", "WARNING");
        }
    }


    @FXML
    private void handleShowReservations() {
        loadReservations();
    }

    @FXML
    private void handleSearchCustomer() {
        String customerCode = customerCodeField.getText().trim();
        if (!customerCode.isEmpty()) {
            try {
                String customerName = getCustomerName(Integer.parseInt(customerCode));
                if (customerName != null) {
                    customerNameField.setText(customerName);
                } else {
                    customerNameField.setText("Customer not found");
                    showAlert("Error", "Customer with code " + customerCode + " not found.", "WARNING");
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a valid customer code.", "ERROR");
            }
        }
    }

    @FXML
    private void handleSearchFlight() {
        String flightCode = flightCodeField.getText().trim();
        if (!flightCode.isEmpty()) {
            if (flightExists(flightCode)) {
                showAlert("Flight Found", "Flight " + flightCode + " is available.", "INFO");
                autoFillFare(flightCode, seatClassCombo.getValue());
            } else {
                showAlert("Flight Not Found", "Flight " + flightCode + " does not exist. Please check available flights.", "ERROR");
            }
        }
    }

    @FXML
    private void handleTableClick() {
        Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            flightCodeField.setText(selected.getFlightCode());
            customerCodeField.setText(String.valueOf(selected.getCustomerCode()));
            seatClassCombo.setValue(selected.getSeatClass());
            seatNumberField.setText(String.valueOf(selected.getSeatNumber()));
            travelDateField.setValue(selected.getTravelDate());
            statusCombo.setValue(selected.getStatus());
            fareField.setText(String.valueOf(selected.getFare()));
            handleSearchCustomer();
        }
    }

    @FXML
    private void clearForm() {
        flightCodeField.clear();
        customerCodeField.clear();
        customerNameField.clear();
        seatClassCombo.setValue("ECO");
        seatNumberField.clear();
        travelDateField.setValue(LocalDate.now().plusDays(1));
        statusCombo.setValue("CONFIRMED");
        fareField.clear();
        reservationTable.getSelectionModel().clearSelection();
    }

    private void autoFillFare(String flightCode, String seatClass) {
        String sql = "SELECT f_exe_fare, f_eco_fare FROM flight_information WHERE f_code = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, flightCode);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double fare = 0.0;
                if ("EXE".equals(seatClass)) {
                    fare = rs.getDouble("f_exe_fare");
                } else if ("ECO".equals(seatClass)) {
                    fare = rs.getDouble("f_eco_fare");
                } else if ("BUS".equals(seatClass)) {
                    fare = rs.getDouble("f_exe_fare") * 1.5;
                }

                if (fare > 0) {
                    fareField.setText(String.format("%.2f", fare));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error auto-filling fare: " + e.getMessage());
        }
    }

    private String getCustomerName(int customerCode) {
        String sql = "SELECT cust_name FROM customer_details WHERE cust_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerCode);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("cust_name");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching customer: " + e.getMessage());
        }
        return null;
    }

    private boolean flightExists(String flightCode) {
        String sql = "SELECT COUNT(*) as count FROM flight_information WHERE f_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, flightCode);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt("count") > 0;
        } catch (SQLException e) {
            System.err.println("Error checking flight existence: " + e.getMessage());
            return false;
        }
    }

    private boolean customerExists(int customerCode) {
        String sql = "SELECT COUNT(*) as count FROM customer_details WHERE cust_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerCode);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt("count") > 0;
        } catch (SQLException e) {
            System.err.println("Error checking customer existence: " + e.getMessage());
            return false;
        }
    }

    private boolean makeReservation(Reservation reservation) {
        // Check if customer exists
        if (!customerExists(reservation.getCustomerCode())) {
            showAlert("Error", "Customer with code " + reservation.getCustomerCode() + " does not exist.", "ERROR");
            return false;
        }

        // Check if flight exists
        if (!flightExists(reservation.getFlightCode())) {
            showAlert("Error", "Flight " + reservation.getFlightCode() + " does not exist. Please use a valid flight code.", "ERROR");
            return false;
        }

        String sql = "INSERT INTO reservations (flight_code, customer_code, seat_class, " +
                "seat_number, travel_date, status, fare) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, reservation.getFlightCode());
            pstmt.setInt(2, reservation.getCustomerCode());
            pstmt.setString(3, reservation.getSeatClass());
            pstmt.setInt(4, reservation.getSeatNumber());
            pstmt.setDate(5, Date.valueOf(reservation.getTravelDate()));
            pstmt.setString(6, reservation.getStatus());
            pstmt.setDouble(7, reservation.getFare());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Reservation created successfully!");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error making reservation: " + e.getMessage());
            showAlert("Database Error", "Error making reservation: " + e.getMessage(), "ERROR");
        }
        return false;
    }

    private void createReservationsTableIfNotExists() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS reservations (" +
                "reservation_id SERIAL PRIMARY KEY, " +
                "flight_code VARCHAR(10) NOT NULL, " +
                "customer_code INTEGER NOT NULL, " +
                "seat_class VARCHAR(10) NOT NULL, " +
                "seat_number INTEGER NOT NULL, " +
                "travel_date DATE NOT NULL, " +
                "status VARCHAR(20) NOT NULL, " +
                "fare DECIMAL(10,2) NOT NULL, " +
                "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Reservations table created/verified successfully!");
        } catch (SQLException e) {
            System.err.println("Error creating reservations table: " + e.getMessage());
        }
    }

    private boolean cancelReservation(int reservationId) {
        String sql = "DELETE FROM reservations WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reservationId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            showAlert("Database Error", "Error cancelling reservation: " + e.getMessage(), "ERROR");
            return false;
        }
    }

    private void loadReservations() {
        String sql = "SELECT * FROM reservations ORDER BY reservation_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            reservationData.clear();
            while (rs.next()) {
                Reservation reservation = new Reservation(
                        rs.getInt("reservation_id"),
                        rs.getString("flight_code"),
                        rs.getInt("customer_code"),
                        rs.getString("seat_class"),
                        rs.getInt("seat_number"),
                        rs.getDate("travel_date").toLocalDate(),
                        rs.getString("status"),
                        rs.getDouble("fare")
                );
                reservationData.add(reservation);
            }
            reservationTable.refresh();
            System.out.println("Loaded " + reservationData.size() + " reservations");
        } catch (SQLException e) {
            System.err.println("Error loading reservations: " + e.getMessage());
        }
    }

    private boolean validateReservationForm() {
        if (flightCodeField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Flight code is required.", "WARNING");
            flightCodeField.requestFocus();
            return false;
        }
        if (customerCodeField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Customer code is required.", "WARNING");
            customerCodeField.requestFocus();
            return false;
        }
        if (seatNumberField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Seat number is required.", "WARNING");
            seatNumberField.requestFocus();
            return false;
        }
        if (travelDateField.getValue() == null) {
            showAlert("Validation Error", "Travel date is required.", "WARNING");
            return false;
        }
        if (fareField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Fare is required.", "WARNING");
            fareField.requestFocus();
            return false;
        }
        try {
            Integer.parseInt(customerCodeField.getText().trim());
            Integer.parseInt(seatNumberField.getText().trim());
            Double.parseDouble(fareField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter valid numbers for customer code, seat number, and fare.", "WARNING");
            return false;
        }
        if (travelDateField.getValue().isBefore(LocalDate.now())) {
            showAlert("Validation Error", "Travel date cannot be in the past.", "WARNING");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message, String type) {
        Alert.AlertType alertType = Alert.AlertType.INFORMATION;
        switch (type) {
            case "ERROR": alertType = Alert.AlertType.ERROR; break;
            case "WARNING": alertType = Alert.AlertType.WARNING; break;
        }
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}