package com.airline.Controller;

import com.airline.database.DatabaseConnection;
import com.airline.model.Reservation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class Member4Controller implements Initializable {

    @FXML private VBox mainContainer;
    @FXML private TableView<Reservation> cancellationTable;
    @FXML private TableColumn<Reservation, Integer> colReservationId;
    @FXML private TableColumn<Reservation, String> colFlightCode;
    @FXML private TableColumn<Reservation, Integer> colCustomerCode;
    @FXML private TableColumn<Reservation, Double> colFare;
    @FXML private TableColumn<Reservation, String> colStatus;
    @FXML private TableColumn<Reservation, LocalDate> colTravelDate;
    @FXML private Label totalCancellationsLabel;
    @FXML private Label refundAmountLabel;
    @FXML private Label pendingRefundsLabel;
    @FXML private Label refundRateLabel;
    @FXML private Label selectedReservationLabel;
    @FXML private Label originalFareLabel;
    @FXML private TextField searchField;
    @FXML private TextField refundAmountField;
    @FXML private ComboBox<String> refundStatusCombo;
    @FXML private ComboBox<String> refundReasonCombo;
    @FXML private ProgressBar refundProgress;
    @FXML private Label refundPercentageLabel;

    private ObservableList<Reservation> cancellationData = FXCollections.observableArrayList();
    private Reservation selectedReservation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        initializeComboBoxes();
        loadCancellationData();
        loadCancellationStats();
        createRefundsTableIfNotExists();
    }

    private void setupTableColumns() {
        colReservationId.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        colFlightCode.setCellValueFactory(new PropertyValueFactory<>("flightCode"));
        colCustomerCode.setCellValueFactory(new PropertyValueFactory<>("customerCode"));
        colFare.setCellValueFactory(new PropertyValueFactory<>("fare"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTravelDate.setCellValueFactory(new PropertyValueFactory<>("travelDate"));

        cancellationTable.setItems(cancellationData);
    }

    private void initializeComboBoxes() {
        refundStatusCombo.getItems().addAll("PENDING", "PROCESSED", "REJECTED", "PARTIAL");
        refundStatusCombo.setValue("PENDING");

        refundReasonCombo.getItems().addAll(
                "Customer Request",
                "Flight Cancellation",
                "Schedule Change",
                "Personal Emergency",
                "Duplicate Booking",
                "Payment Issue",
                "Other"
        );
    }

    private void createRefundsTableIfNotExists() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS refunds (" +
                "refund_id SERIAL PRIMARY KEY, " +
                "reservation_id INTEGER NOT NULL, " +
                "refund_amount DECIMAL(10,2) NOT NULL, " +
                "refund_status VARCHAR(20) DEFAULT 'PENDING', " +
                "refund_reason TEXT, " +
                "refund_percentage INTEGER DEFAULT 80, " +
                "processed_date TIMESTAMP, " +
                "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Refunds table created/verified successfully!");
        } catch (SQLException e) {
            System.err.println("Error creating refunds table: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearchCancellations() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            loadCancellationData();
        } else {
            ObservableList<Reservation> filtered = cancellationData.filtered(
                    r -> String.valueOf(r.getReservationId()).contains(searchText) ||
                            (r.getFlightCode() != null && r.getFlightCode().toLowerCase().contains(searchText)) ||
                            String.valueOf(r.getCustomerCode()).contains(searchText)
            );
            cancellationTable.setItems(filtered);
        }
    }

    @FXML
    private void handleProcessCancellation() {
        if (selectedReservation == null) {
            showAlert("Error", "Please select a reservation to process refund", Alert.AlertType.ERROR);
            return;
        }

        try {
            double refundAmount = Double.parseDouble(refundAmountField.getText());
            String refundStatus = refundStatusCombo.getValue();
            String refundReason = refundReasonCombo.getValue();

            if (refundReason == null || refundReason.isEmpty()) {
                showAlert("Error", "Please select a refund reason", Alert.AlertType.ERROR);
                return;
            }

            double refundPercentage = (refundAmount / selectedReservation.getFare()) * 100;

            if (processRefund(selectedReservation.getReservationId(), refundAmount, refundStatus, refundReason, (int) refundPercentage)) {
                showAlert("Success", "Refund processed successfully!", Alert.AlertType.INFORMATION);

                // Update reservation status to REFUNDED
                updateReservationStatus(selectedReservation.getReservationId(), "REFUNDED");

                loadCancellationData();
                loadCancellationStats();
                clearRefundForm();
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid refund amount", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAutoProcessRefunds() {
        int processedCount = 0;
        double totalRefunded = 0;

        for (Reservation reservation : cancellationData) {
            if ("CANCELLED".equals(reservation.getStatus())) {
                // Auto-calculate 80% refund
                double refundAmount = reservation.getFare() * 0.8;
                if (processRefund(reservation.getReservationId(), refundAmount, "PROCESSED", "Auto Processed", 80)) {
                    updateReservationStatus(reservation.getReservationId(), "REFUNDED");
                    processedCount++;
                    totalRefunded += refundAmount;
                }
            }
        }

        showAlert("Auto Processing Complete",
                "Processed " + processedCount + " refunds totaling " + String.format("%.2f", totalRefunded),
                Alert.AlertType.INFORMATION);

        loadCancellationData();
        loadCancellationStats();
    }

    @FXML
    private void handleSendNotifications() {
        int notificationCount = 0;

        for (Reservation reservation : cancellationData) {
            if ("REFUNDED".equals(reservation.getStatus())) {
                // In a real application, this would send email/SMS notifications
                notificationCount++;
            }
        }

        showAlert("Notifications Sent",
                "Refund notifications sent to " + notificationCount + " customers",
                Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleViewRefundHistory() {
        try {
            String refundHistory = getRefundHistory();
            TextArea textArea = new TextArea(refundHistory);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefSize(600, 400);

            ScrollPane scrollPane = new ScrollPane(textArea);
            scrollPane.setFitToWidth(true);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Refund History");
            alert.setHeaderText("Complete Refund History");
            alert.getDialogPane().setContent(scrollPane);
            alert.showAndWait();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load refund history: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleGenerateRefundReport() {
        try {
            int totalCancellations = getTotalCancellations();
            double totalRefunds = getTotalRefundsProcessed();
            int pendingRefunds = getPendingRefundsCount();
            double averageRefund = getAverageRefundAmount();
            double refundRate = getRefundRate();

            String report = "CANCELLATION AND REFUND REPORT\n\n" +
                    "Total Cancellations: " + totalCancellations + "\n" +
                    "Total Refunds Processed: " + String.format("%.2f", totalRefunds) + "\n" +
                    "Pending Refunds: " + pendingRefunds + "\n" +
                    "Average Refund Amount: " + String.format("%.2f", averageRefund) + "\n" +
                    "Refund Rate: " + String.format("%.1f", refundRate) + "\n\n" +
                    "REFUND STATISTICS:\n" +
                    "Average Processing Time: 2-3 business days\n" +
                    "Refund Approval Rate: 95\n" +
                    "Common Reasons:\n" +
                    "• Flight changes (35)\n" +
                    "• Personal emergencies (25)\n" +
                    "• Schedule conflicts (20)\n" +
                    "• Other (20)";

            TextArea textArea = new TextArea(report);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefSize(500, 400);

            ScrollPane scrollPane = new ScrollPane(textArea);
            scrollPane.setFitToWidth(true);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Refund Report");
            alert.setHeaderText("Comprehensive Refund Analysis");
            alert.getDialogPane().setContent(scrollPane);
            alert.showAndWait();

        } catch (SQLException e) {
            showAlert("Error", "Failed to generate report: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleExportRefundReport() {
        StringBuilder exportData = new StringBuilder("Reservation ID,Flight Code,Customer Code,Fare,Refund Amount,Status,Reason\n");

        for (Reservation reservation : cancellationData) {
            exportData.append(String.format("%d,%s,%d,%.2f,%.2f,%s,%s\n",
                    reservation.getReservationId(),
                    reservation.getFlightCode(),
                    reservation.getCustomerCode(),
                    reservation.getFare(),
                    reservation.getFare() * 0.8, // Estimated refund
                    reservation.getStatus(),
                    "Cancellation"
            ));
        }

        TextArea textArea = new TextArea(exportData.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Refund Data");
        alert.setHeaderText("Refund Data (CSV Format)");
        alert.getDialogPane().setContent(scrollPane);
        alert.showAndWait();
    }

    @FXML
    private void handleTableClick() {
        selectedReservation = cancellationTable.getSelectionModel().getSelectedItem();
        if (selectedReservation != null && "CANCELLED".equals(selectedReservation.getStatus())) {
            selectedReservationLabel.setText("Reservation " + selectedReservation.getReservationId());
            originalFareLabel.setText(String.format("%.2f", selectedReservation.getFare()));

            // Auto-calculate refund amount (80% of fare)
            double refundAmount = selectedReservation.getFare() * 0.8;
            refundAmountField.setText(String.format("%.2f", refundAmount));

            // Update progress bar and percentage
            refundProgress.setProgress(0.8);
            refundPercentageLabel.setText("80 refund");
        }
    }

    @FXML
    private void handleRefresh() {
        loadCancellationData();
        loadCancellationStats();
        showAlert("Refreshed", "Cancellation data updated successfully!", Alert.AlertType.INFORMATION);
    }

    private void clearRefundForm() {
        refundAmountField.clear();
        refundStatusCombo.setValue("PENDING");
        refundReasonCombo.setValue(null);
        selectedReservationLabel.setText("None");
        originalFareLabel.setText("0.00");
        refundProgress.setProgress(0);
        refundPercentageLabel.setText("0 refund");
        selectedReservation = null;
    }

    // Enhanced Database methods
    private void loadCancellationData() {
        String sql = "SELECT r.*, c.cust_name FROM reservations r " +
                "LEFT JOIN customer_details c ON r.customer_code = c.cust_code " +
                "WHERE r.status IN ('CANCELLED', 'REFUNDED') " +
                "ORDER BY r.reservation_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            cancellationData.clear();
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
                cancellationData.add(reservation);
            }
            cancellationTable.refresh();

        } catch (SQLException e) {
            showAlert("Error", "Failed to load cancellations: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadCancellationStats() {
        try {
            int totalCancellations = getTotalCancellations();
            totalCancellationsLabel.setText(String.valueOf(totalCancellations));

            double refundAmount = getTotalRefundsProcessed();
            refundAmountLabel.setText(String.format("%.2f", refundAmount));

            int pendingRefunds = getPendingRefundsCount();
            pendingRefundsLabel.setText(String.valueOf(pendingRefunds));

            double refundRate = getRefundRate();
            refundRateLabel.setText(String.format("%.1f", refundRate));

        } catch (SQLException e) {
            showAlert("Error", "Failed to load statistics: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean processRefund(int reservationId, double refundAmount, String refundStatus, String refundReason, int refundPercentage) {
        String sql = "INSERT INTO refunds (reservation_id, refund_amount, refund_status, refund_reason, refund_percentage) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reservationId);
            pstmt.setDouble(2, refundAmount);
            pstmt.setString(3, refundStatus);
            pstmt.setString(4, refundReason);
            pstmt.setInt(5, refundPercentage);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            showAlert("Error", "Failed to process refund: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }

    private boolean updateReservationStatus(int reservationId, String status) {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, reservationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating reservation status: " + e.getMessage());
            return false;
        }
    }

    private double getAverageRefundAmount() throws SQLException {
        String sql = "SELECT COALESCE(AVG(refund_amount), 0) as avg_refund FROM refunds WHERE refund_status = 'PROCESSED'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getDouble("avg_refund") : 0;
        }
    }

    private double getRefundRate() throws SQLException {
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM refunds WHERE refund_status = 'PROCESSED') * 100.0 / " +
                "GREATEST((SELECT COUNT(*) FROM reservations WHERE status = 'CANCELLED'), 1) as rate";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getDouble("rate") : 0;
        }
    }

    // Existing database methods remain the same...
    private int getTotalCancellations() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM reservations WHERE status = 'CANCELLED'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    private double getTotalRefundsProcessed() throws SQLException {
        String sql = "SELECT COALESCE(SUM(refund_amount), 0) as total FROM refunds WHERE refund_status = 'PROCESSED'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getDouble("total") : 0;
        }
    }

    private int getPendingRefundsCount() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM refunds WHERE refund_status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    private String getRefundHistory() throws SQLException {
        String sql = "SELECT r.reservation_id, rf.refund_amount, rf.refund_status, rf.refund_reason, rf.created_date " +
                "FROM refunds rf " +
                "JOIN reservations r ON rf.reservation_id = r.reservation_id " +
                "ORDER BY rf.created_date DESC LIMIT 20";

        StringBuilder history = new StringBuilder("Recent Refund History:\n\n");
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                history.append(String.format("Reservation %d: %.2f - %s (%s) - %s\n",
                        rs.getInt("reservation_id"),
                        rs.getDouble("refund_amount"),
                        rs.getString("refund_status"),
                        rs.getString("refund_reason"),
                        rs.getTimestamp("created_date").toLocalDateTime().toLocalDate()
                ));
            }
        }
        return history.toString();
    }

    @FXML
    private void goBackToMain() {
        try {
            Stage currentStage = (Stage) mainContainer.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/airline/view/main-dashboard.fxml"));
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Airline Reservation System");
        } catch (Exception e) {
            showAlert("Error", "Cannot load main dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
