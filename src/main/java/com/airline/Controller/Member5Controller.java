package com.airline.Controller;

import com.airline.database.DatabaseConnection;
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
import java.util.Random;
import java.util.ResourceBundle;


public class Member5Controller implements Initializable {

    @FXML private VBox mainContainer;
    @FXML private TableView<FlightFare> fareTable;
    @FXML private TableColumn<FlightFare, String> colFlightCode;
    @FXML private TableColumn<FlightFare, String> colFlightName;
    @FXML private TableColumn<FlightFare, Double> colExecutiveFare;
    @FXML private TableColumn<FlightFare, Double> colEconomyFare;
    @FXML private TableColumn<FlightFare, Double> colBusinessFare;
    @FXML private TableColumn<FlightFare, Double> colProfitMargin;
    @FXML private Label totalFlightsLabel;
    @FXML private Label avgExecutiveFareLabel;
    @FXML private Label avgEconomyFareLabel;
    @FXML private Label revenuePotentialLabel;
    @FXML private Label selectedFlightLabel;
    @FXML private TextField searchField;
    @FXML private TextField executiveFareField;
    @FXML private TextField economyFareField;
    @FXML private TextField businessFareField;
    @FXML private ComboBox<String> pricingStrategyCombo;
    @FXML private ProgressBar demandProgress;
    @FXML private Label demandLevelLabel;

    private ObservableList<FlightFare> fareData = FXCollections.observableArrayList();
    private FlightFare selectedFlight;
    private Random random = new Random();

    public static class FlightFare {
        private final String flightCode;
        private final String flightName;
        private double executiveFare;
        private double economyFare;
        private double businessFare;
        private double profitMargin;

        public FlightFare(String flightCode, String flightName, double executiveFare, double economyFare, double businessFare) {
            this.flightCode = flightCode;
            this.flightName = flightName;
            this.executiveFare = executiveFare;
            this.economyFare = economyFare;
            this.businessFare = businessFare;
            this.profitMargin = calculateProfitMargin();
        }

        private double calculateProfitMargin() {
            double baseCost = economyFare * 0.6; // Assume 40% profit margin on economy
            double totalRevenue = (executiveFare + economyFare + businessFare) / 3;
            return ((totalRevenue - baseCost) / totalRevenue) * 100;
        }

        // Getters
        public String getFlightCode() { return flightCode; }
        public String getFlightName() { return flightName; }
        public double getExecutiveFare() { return executiveFare; }
        public double getEconomyFare() { return economyFare; }
        public double getBusinessFare() { return businessFare; }
        public double getProfitMargin() { return profitMargin; }

        // Setters
        public void setExecutiveFare(double executiveFare) {
            this.executiveFare = executiveFare;
            this.profitMargin = calculateProfitMargin();
        }
        public void setEconomyFare(double economyFare) {
            this.economyFare = economyFare;
            this.profitMargin = calculateProfitMargin();
        }
        public void setBusinessFare(double businessFare) {
            this.businessFare = businessFare;
            this.profitMargin = calculateProfitMargin();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        initializeComboBoxes();
        loadFareData();
        loadFareStatistics();
        updateFlightTableWithFares();
        simulateDemand();
    }

    private void setupTableColumns() {
        colFlightCode.setCellValueFactory(new PropertyValueFactory<>("flightCode"));
        colFlightName.setCellValueFactory(new PropertyValueFactory<>("flightName"));
        colExecutiveFare.setCellValueFactory(new PropertyValueFactory<>("executiveFare"));
        colEconomyFare.setCellValueFactory(new PropertyValueFactory<>("economyFare"));
        colBusinessFare.setCellValueFactory(new PropertyValueFactory<>("businessFare"));
        colProfitMargin.setCellValueFactory(new PropertyValueFactory<>("profitMargin"));

        fareTable.setItems(fareData);
    }

    private void initializeComboBoxes() {
        pricingStrategyCombo.getItems().addAll(
                "Standard Pricing",
                "Peak Season",
                "Off-Peak Discount",
                "Dynamic Pricing",
                "Competitive Match",
                "Revenue Optimization"
        );
        pricingStrategyCombo.setValue("Standard Pricing");
    }

    private void simulateDemand() {
        // Simulate random demand fluctuations
        double demand = 0.3 + (random.nextDouble() * 0.5); // 30% to 80% demand
        demandProgress.setProgress(demand);

        if (demand < 0.4) {
            demandLevelLabel.setText("Low Demand");
            demandLevelLabel.setStyle("-fx-text-fill: #e74c3c;");
        } else if (demand < 0.7) {
            demandLevelLabel.setText("Medium Demand");
            demandLevelLabel.setStyle("-fx-text-fill: #f39c12;");
        } else {
            demandLevelLabel.setText("High Demand");
            demandLevelLabel.setStyle("-fx-text-fill: #27ae60;");
        }
    }

    @FXML
    private void handleSearchFares() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            loadFareData();
        } else {
            ObservableList<FlightFare> filtered = fareData.filtered(
                    f -> (f.getFlightCode() != null && f.getFlightCode().toLowerCase().contains(searchText)) ||
                            (f.getFlightName() != null && f.getFlightName().toLowerCase().contains(searchText))
            );
            fareTable.setItems(filtered);
        }
    }

    @FXML
    private void handleUpdateFares() {
        if (selectedFlight == null) {
            showAlert("Error", "Please select a flight to update fares", Alert.AlertType.ERROR);
            return;
        }

        try {
            double executiveFare = Double.parseDouble(executiveFareField.getText());
            double economyFare = Double.parseDouble(economyFareField.getText());
            double businessFare = Double.parseDouble(businessFareField.getText());

            if (executiveFare <= 0 || economyFare <= 0 || businessFare <= 0) {
                showAlert("Error", "Fares must be positive numbers", Alert.AlertType.ERROR);
                return;
            }

            if (economyFare >= executiveFare) {
                showAlert("Error", "Executive fare must be higher than economy fare", Alert.AlertType.ERROR);
                return;
            }

            if (businessFare <= executiveFare) {
                showAlert("Error", "Business fare must be higher than executive fare", Alert.AlertType.ERROR);
                return;
            }

            String strategy = pricingStrategyCombo.getValue();
            if (updateFlightFares(selectedFlight.getFlightCode(), executiveFare, economyFare, businessFare, strategy)) {
                showAlert("Success", "Fares updated successfully using " + strategy + " strategy!", Alert.AlertType.INFORMATION);
                loadFareData();
                loadFareStatistics();
                clearFareForm();
                simulateDemand();
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid fare amounts", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleApplyDiscount() {
        if (selectedFlight != null) {
            // Apply 10% discount for demonstration
            double newExecutiveFare = selectedFlight.getExecutiveFare() * 0.9;
            double newEconomyFare = selectedFlight.getEconomyFare() * 0.9;
            double newBusinessFare = selectedFlight.getBusinessFare() * 0.9;

            executiveFareField.setText(String.format("%.2f", newExecutiveFare));
            economyFareField.setText(String.format("%.2f", newEconomyFare));
            businessFareField.setText(String.format("%.2f", newBusinessFare));

            showAlert("Discount Applied", "10% discount applied to all fare classes", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Error", "Please select a flight first", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleApplyDiscount10() {
        applyBulkDiscount(0.9, "10% discount");
    }

    @FXML
    private void handleIncreasePremium15() {
        applyBulkPremium(1.15, "15% premium increase");
    }

    @FXML
    private void handleSeasonalPricing() {
        // Apply seasonal pricing adjustments
        for (FlightFare fare : fareData) {
            double seasonalMultiplier = 1.0 + (random.nextDouble() * 0.3); // 0% to 30% increase
            fare.setExecutiveFare(fare.getExecutiveFare() * seasonalMultiplier);
            fare.setEconomyFare(fare.getEconomyFare() * seasonalMultiplier);
            fare.setBusinessFare(fare.getBusinessFare() * seasonalMultiplier);
        }

        fareTable.refresh();
        loadFareStatistics();
        showAlert("Seasonal Pricing", "Applied seasonal pricing adjustments to all flights", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleWeekendPricing() {
        // Apply weekend pricing premium
        for (FlightFare fare : fareData) {
            fare.setExecutiveFare(fare.getExecutiveFare() * 1.2);
            fare.setEconomyFare(fare.getEconomyFare() * 1.15);
            fare.setBusinessFare(fare.getBusinessFare() * 1.25);
        }

        fareTable.refresh();
        loadFareStatistics();
        showAlert("Weekend Pricing", "Applied weekend pricing premium to all flights", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleResetToBase() {
        // Reset to base fares
        loadFareData();
        showAlert("Reset Complete", "All fares reset to base pricing", Alert.AlertType.INFORMATION);
    }

    private void applyBulkDiscount(double discountMultiplier, String message) {
        int updatedCount = 0;
        for (FlightFare fare : fareData) {
            fare.setExecutiveFare(fare.getExecutiveFare() * discountMultiplier);
            fare.setEconomyFare(fare.getEconomyFare() * discountMultiplier);
            fare.setBusinessFare(fare.getBusinessFare() * discountMultiplier);
            updatedCount++;
        }

        fareTable.refresh();
        loadFareStatistics();
        showAlert("Bulk Update", message + " applied to " + updatedCount + " flights", Alert.AlertType.INFORMATION);
    }

    private void applyBulkPremium(double premiumMultiplier, String message) {
        int updatedCount = 0;
        for (FlightFare fare : fareData) {
            fare.setExecutiveFare(fare.getExecutiveFare() * premiumMultiplier);
            fare.setEconomyFare(fare.getEconomyFare() * premiumMultiplier);
            fare.setBusinessFare(fare.getBusinessFare() * premiumMultiplier);
            updatedCount++;
        }

        fareTable.refresh();
        loadFareStatistics();
        showAlert("Bulk Update", message + " applied to " + updatedCount + " flights", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleGenerateFareReport() {
        try {
            int totalFlights = getTotalFlightsWithFares();
            double avgExecutive = getAverageExecutiveFare();
            double avgEconomy = getAverageEconomyFare();
            double totalRevenuePotential = calculateTotalRevenuePotential();

            String report = "FARE MANAGEMENT REPORT\n\n" +
                    "Total Flights with Fares: " + totalFlights + "\n" +
                    "Average Executive Fare: " + String.format("%.2f", avgExecutive) + "\n" +
                    "Average Economy Fare: " + String.format("%.2f", avgEconomy) + "\n" +
                    "Total Revenue Potential: " + String.format("%.2f", totalRevenuePotential) + "\n\n" +
                    "FARE ANALYSIS:\n" +
                    "Executive class markup: 150\n" +
                    "Business class markup: 250\n" +
                    "Most profitable route: DEL-BOM\n" +
                    "Seasonal variations: 15\n" +
                    "Average profit margin: " + String.format("%.1f", calculateAverageProfitMargin()) + "\n\n" +
                    "RECOMMENDATIONS:\n" +
                    "1. Increase premium fares on high-demand routes\n" +
                    "2. Introduce dynamic pricing for last-minute bookings\n" +
                    "3. Offer package deals for family travel\n" +
                    "4. Implement seasonal pricing strategies";

            TextArea textArea = new TextArea(report);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefSize(500, 400);

            ScrollPane scrollPane = new ScrollPane(textArea);
            scrollPane.setFitToWidth(true);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Fare Analysis Report");
            alert.setHeaderText("Comprehensive Fare Analysis");
            alert.getDialogPane().setContent(scrollPane);
            alert.showAndWait();

        } catch (SQLException e) {
            showAlert("Error", "Failed to generate fare report: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRefresh() {
        loadFareData();
        loadFareStatistics();
        simulateDemand();
        showAlert("Refreshed", "Fare data updated successfully!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleTableClick() {
        selectedFlight = fareTable.getSelectionModel().getSelectedItem();
        if (selectedFlight != null) {
            selectedFlightLabel.setText(selectedFlight.getFlightCode() + " - " + selectedFlight.getFlightName());
            executiveFareField.setText(String.format("%.2f", selectedFlight.getExecutiveFare()));
            economyFareField.setText(String.format("%.2f", selectedFlight.getEconomyFare()));
            businessFareField.setText(String.format("%.2f", selectedFlight.getBusinessFare()));
        }
    }

    private void clearFareForm() {
        executiveFareField.clear();
        economyFareField.clear();
        businessFareField.clear();
        selectedFlightLabel.setText("None");
        selectedFlight = null;
    }

    // Database methods
    private void loadFareData() {
        String sql = "SELECT f_code, f_name, " +
                "COALESCE(f_exe_fare, 5000.00) as executive_fare, " +
                "COALESCE(f_eco_fare, 2000.00) as economy_fare, " +
                "COALESCE(f_exe_fare * 1.5, 7500.00) as business_fare " +
                "FROM flight_information ORDER BY f_code";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            fareData.clear();
            while (rs.next()) {
                FlightFare fare = new FlightFare(
                        rs.getString("f_code"),
                        rs.getString("f_name"),
                        rs.getDouble("executive_fare"),
                        rs.getDouble("economy_fare"),
                        rs.getDouble("business_fare")
                );
                fareData.add(fare);
            }
            fareTable.refresh();

        } catch (SQLException e) {
            showAlert("Error", "Failed to load fare data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadFareStatistics() {
        try {
            int totalFlights = getTotalFlightsWithFares();
            totalFlightsLabel.setText(String.valueOf(totalFlights));

            double avgExecutive = getAverageExecutiveFare();
            avgExecutiveFareLabel.setText(String.format("%.2f", avgExecutive));

            double avgEconomy = getAverageEconomyFare();
            avgEconomyFareLabel.setText(String.format("%.2f", avgEconomy));

            double revenuePotential = calculateTotalRevenuePotential();
            revenuePotentialLabel.setText(String.format("%.2f", revenuePotential));

        } catch (SQLException e) {
            showAlert("Error", "Failed to load fare statistics: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean updateFlightFares(String flightCode, double executiveFare, double economyFare, double businessFare, String strategy) {
        ensureFareColumnsExist();

        String sql = "UPDATE flight_information SET f_exe_fare = ?, f_eco_fare = ?, pricing_strategy = ? WHERE f_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, executiveFare);
            pstmt.setDouble(2, economyFare);
            pstmt.setString(3, strategy);
            pstmt.setString(4, flightCode);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            showAlert("Error", "Failed to update fares: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }

    private void ensureFareColumnsExist() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Add fare columns if they don't exist
            stmt.execute("ALTER TABLE flight_information ADD COLUMN IF NOT EXISTS f_exe_fare DECIMAL(10,2) DEFAULT 5000.00");
            stmt.execute("ALTER TABLE flight_information ADD COLUMN IF NOT EXISTS f_eco_fare DECIMAL(10,2) DEFAULT 2000.00");
            stmt.execute("ALTER TABLE flight_information ADD COLUMN IF NOT EXISTS pricing_strategy VARCHAR(50) DEFAULT 'Standard'");

        } catch (SQLException e) {
            System.err.println("Error ensuring fare columns exist: " + e.getMessage());
        }
    }

    private void updateFlightTableWithFares() {
        ensureFareColumnsExist();
    }

    private double calculateTotalRevenuePotential() throws SQLException {
        String sql = "SELECT COALESCE(SUM(f_exe_fare + f_eco_fare + (f_exe_fare * 1.5)), 0) as total_potential FROM flight_information";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getDouble("total_potential") : 0;
        }
    }

    private double calculateAverageProfitMargin() {
        if (fareData.isEmpty()) return 0;
        double totalMargin = 0;
        for (FlightFare fare : fareData) {
            totalMargin += fare.getProfitMargin();
        }
        return totalMargin / fareData.size();
    }

    private int getTotalFlightsWithFares() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM flight_information";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    private double getAverageExecutiveFare() throws SQLException {
        String sql = "SELECT COALESCE(AVG(f_exe_fare), 5000.00) as avg_fare FROM flight_information";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getDouble("avg_fare") : 0;
        }
    }

    private double getAverageEconomyFare() throws SQLException {
        String sql = "SELECT COALESCE(AVG(f_eco_fare), 2000.00) as avg_fare FROM flight_information";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getDouble("avg_fare") : 0;
        }
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
