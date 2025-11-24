package com.airline.Controller;

import com.airline.database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class Member6Controller implements Initializable {

    @FXML private VBox mainContainer;
    @FXML private Label totalRevenueLabel;
    @FXML private Label totalBookingsLabel;
    @FXML private Label avgBookingValueLabel;
    @FXML private Label occupancyRateLabel;
    @FXML private Label customerSatisfactionLabel;
    @FXML private Label cancellationRateLabel;
    @FXML private Label repeatCustomersLabel;
    @FXML private Label revenueGrowthLabel;
    @FXML private BarChart<String, Number> revenueChart;
    @FXML private BarChart<String, Number> bookingChart;
    @FXML private PieChart revenuePieChart;
    @FXML private LineChart<String, Number> trendChart;
    @FXML private TableView<FlightPerformance> performanceTable;
    @FXML private TableColumn<FlightPerformance, String> colFlightCode;
    @FXML private TableColumn<FlightPerformance, Integer> colBookings;
    @FXML private TableColumn<FlightPerformance, Double> colRevenue;
    @FXML private TableColumn<FlightPerformance, Double> colOccupancy;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private Button generateReportButton;
    @FXML private ProgressIndicator loadingIndicator;

    private ObservableList<FlightPerformance> performanceData = FXCollections.observableArrayList();

    public static class FlightPerformance {
        private final String flightCode;
        private final int bookings;
        private final double revenue;
        private final double occupancyRate;

        public FlightPerformance(String flightCode, int bookings, double revenue, double occupancyRate) {
            this.flightCode = flightCode;
            this.bookings = bookings;
            this.revenue = revenue;
            this.occupancyRate = occupancyRate;
        }

        public String getFlightCode() { return flightCode; }
        public int getBookings() { return bookings; }
        public double getRevenue() { return revenue; }
        public double getOccupancyRate() { return occupancyRate; }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        setupTableColumns();
        loadAnalyticsData();
        setupCharts();
        loadPerformanceData();
    }

    private void setupUI() {
        if (endDatePicker != null) {
            endDatePicker.setValue(LocalDate.now());
        }
        if (startDatePicker != null) {
            startDatePicker.setValue(LocalDate.now().minusDays(30));
        }

        if (reportTypeCombo != null) {
            reportTypeCombo.getItems().addAll(
                    "Daily Report",
                    "Weekly Report",
                    "Monthly Report",
                    "Quarterly Report",
                    "Custom Range"
            );
            reportTypeCombo.setValue("Monthly Report");
        }

        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
    }

    private void setupTableColumns() {
        if (colFlightCode != null) {
            colFlightCode.setCellValueFactory(new PropertyValueFactory<>("flightCode"));
        }
        if (colBookings != null) {
            colBookings.setCellValueFactory(new PropertyValueFactory<>("bookings"));
        }
        if (colRevenue != null) {
            colRevenue.setCellValueFactory(new PropertyValueFactory<>("revenue"));
        }
        if (colOccupancy != null) {
            colOccupancy.setCellValueFactory(new PropertyValueFactory<>("occupancyRate"));
        }

        if (performanceTable != null) {
            performanceTable.setItems(performanceData);
        }
    }

    private void loadAnalyticsData() {
        showLoading(true);

        new Thread(() -> {
            try {
                double totalRevenue = getTotalRevenue();
                int totalBookings = getTotalBookings();
                double avgBookingValue = getAverageBookingValue();
                double occupancyRate = getOccupancyRate();
                int satisfactionRate = getCustomerSatisfactionRate();
                double cancellationRate = getCancellationRate();
                int repeatCustomers = getRepeatCustomerCount();
                double revenueGrowth = calculateRevenueGrowth();

                javafx.application.Platform.runLater(() -> {
                    if (totalRevenueLabel != null) {
                        totalRevenueLabel.setText("M" + String.format("%,.2f", totalRevenue));
                    }
                    if (totalBookingsLabel != null) {
                        totalBookingsLabel.setText(String.valueOf(totalBookings));
                    }
                    if (avgBookingValueLabel != null) {
                        avgBookingValueLabel.setText("M" + String.format("%,.2f", avgBookingValue));
                    }
                    if (occupancyRateLabel != null) {
                        occupancyRateLabel.setText(String.format("%.1f", occupancyRate));
                    }
                    if (customerSatisfactionLabel != null) {
                        customerSatisfactionLabel.setText(String.valueOf(satisfactionRate));
                    }
                    if (cancellationRateLabel != null) {
                        cancellationRateLabel.setText(String.format("%.1f", cancellationRate));
                    }
                    if (repeatCustomersLabel != null) {
                        repeatCustomersLabel.setText(String.valueOf(repeatCustomers));
                    }
                    if (revenueGrowthLabel != null) {
                        revenueGrowthLabel.setText(String.format("%.1f", revenueGrowth));
                    }

                    showLoading(false);
                });

            } catch (SQLException e) {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Failed to load analytics data: " + e.getMessage(), Alert.AlertType.ERROR);
                    showLoading(false);
                });
            }
        }).start();
    }

    private void setupCharts() {
        setupRevenueChart();
        setupBookingChart();
        setupRevenuePieChart();
        setupTrendChart();
    }

    private void setupRevenueChart() {
        if (revenueChart == null) return;

        revenueChart.setTitle("Monthly Revenue Analysis");
        revenueChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");

        // Sample data for demonstration
        series.getData().add(new XYChart.Data<>("Jan", 45000));
        series.getData().add(new XYChart.Data<>("Feb", 52000));
        series.getData().add(new XYChart.Data<>("Mar", 48000));
        series.getData().add(new XYChart.Data<>("Apr", 61000));
        series.getData().add(new XYChart.Data<>("May", 58000));
        series.getData().add(new XYChart.Data<>("Jun", 67000));

        revenueChart.getData().add(series);
    }

    private void setupBookingChart() {
        if (bookingChart == null) return;

        bookingChart.setTitle("Top Performing Flights");
        bookingChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Bookings");

        // Sample data for demonstration
        series.getData().add(new XYChart.Data<>("AI101", 45));
        series.getData().add(new XYChart.Data<>("SG202", 38));
        series.getData().add(new XYChart.Data<>("IG303", 52));
        series.getData().add(new XYChart.Data<>("UK404", 29));
        series.getData().add(new XYChart.Data<>("AI505", 41));

        bookingChart.getData().add(series);
    }

    private void setupRevenuePieChart() {
        if (revenuePieChart == null) return;

        revenuePieChart.setTitle("Revenue by Class");

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        // Sample data for demonstration
        pieChartData.add(new PieChart.Data("Executive Class", 45000));
        pieChartData.add(new PieChart.Data("Economy Class", 28000));
        pieChartData.add(new PieChart.Data("Business Class", 32000));

        revenuePieChart.setData(pieChartData);
    }

    private void setupTrendChart() {
        if (trendChart == null) return;

        trendChart.setTitle("Booking Trends Last 7 Days");
        trendChart.setLegendVisible(true);

        XYChart.Series<String, Number> bookingsSeries = new XYChart.Series<>();
        bookingsSeries.setName("Daily Bookings");

        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Daily Revenue");

        // Sample data for demonstration
        String[] dates = {"01/01", "01/02", "01/03", "01/04", "01/05", "01/06", "01/07"};
        int[] bookings = {12, 15, 18, 14, 20, 16, 22};
        double[] revenues = {8400, 10500, 12600, 9800, 14000, 11200, 15400};

        for (int i = 0; i < dates.length; i++) {
            bookingsSeries.getData().add(new XYChart.Data<>(dates[i], bookings[i]));
            revenueSeries.getData().add(new XYChart.Data<>(dates[i], revenues[i]));
        }

        trendChart.getData().addAll(bookingsSeries, revenueSeries);
    }

    private void loadPerformanceData() {
        // Sample data for demonstration
        performanceData.clear();
        performanceData.add(new FlightPerformance("AI101", 45, 45000, 85.5));
        performanceData.add(new FlightPerformance("SG202", 38, 38000, 72.3));
        performanceData.add(new FlightPerformance("IG303", 52, 52000, 91.2));
        performanceData.add(new FlightPerformance("UK404", 29, 29000, 64.8));
        performanceData.add(new FlightPerformance("AI505", 41, 41000, 78.9));
    }

    @FXML
    private void handleGenerateComprehensiveReport() {
        showLoading(true);

        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulate processing

                String report = "COMPREHENSIVE ANALYTICS REPORT\n\n" +
                        "FINANCIAL METRICS:\n" +
                        "Total Revenue: M128,450.00\n" +
                        "Total Bookings: 205\n" +
                        "Average Booking Value: M626.34\n" +
                        "Revenue Growth: 12.5%\n\n" +
                        "OPERATIONAL METRICS:\n" +
                        "Occupancy Rate: 78.3%\n" +
                        "Cancellation Rate: 4.2%\n" +
                        "Most Popular Flight: AI101\n" +
                        "Repeat Customers: 45\n\n" +
                        "PERFORMANCE INSIGHTS:\n" +
                        "Customer Satisfaction: 94%\n" +
                        "Booking Conversion: 42%\n" +
                        "Peak Booking Hours: 10:00 AM - 12:00 PM\n" +
                        "Best Performing Route: DEL-BOM";

                javafx.application.Platform.runLater(() -> {
                    showAlert("Comprehensive Analytics Report", report, Alert.AlertType.INFORMATION);
                    showLoading(false);
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Failed to generate comprehensive report: " + e.getMessage(), Alert.AlertType.ERROR);
                    showLoading(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleRevenueAnalysis() {
        String analysis = "REVENUE ANALYSIS\n\n" +
                "CURRENT PERFORMANCE:\n" +
                "Monthly Revenue: M128,450.00\n" +
                "Revenue Growth: 12.5%\n" +
                "Revenue per Flight: M25,690.00\n\n" +
                "REVENUE DISTRIBUTION:\n" +
                "Executive Class: M45,000 (35.0%)\n" +
                "Economy Class: M28,000 (21.8%)\n" +
                "Business Class: M32,000 (24.9%)\n\n" +
                "REVENUE DRIVERS:\n" +
                "Business Travel: 60% of bookings\n" +
                "Weekend Premium: 25% fare increase\n" +
                "Seasonal Peaks: Dec-Mar 40% increase";

        showAlert("Revenue Analysis", analysis, Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleCustomerAnalytics() {
        String analysis = "CUSTOMER ANALYTICS\n\n" +
                "CUSTOMER BASE:\n" +
                "Total Customers: 156\n" +
                "Active Customers: 89\n" +
                "New Customers This Month: 23\n" +
                "Repeat Customer Rate: 28.8%\n\n" +
                "CUSTOMER SEGMENTATION:\n" +
                "Business Travelers: 45%\n" +
                "Leisure Travelers: 35%\n" +
                "Family Travelers: 20%\n\n" +
                "LOYALTY METRICS:\n" +
                "Average Bookings per Customer: 2.3\n" +
                "Customer Lifetime Value: M1,847\n" +
                "Churn Rate: 8% monthly";

        showAlert("Customer Analytics", analysis, Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleGenerateCustomReport() {
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            showAlert("Error", "Please select both start and end dates", Alert.AlertType.ERROR);
            return;
        }

        showLoading(true);

        new Thread(() -> {
            try {
                Thread.sleep(1500); // Simulate processing

                LocalDate startDate = startDatePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();
                String reportType = reportTypeCombo.getValue();

                String customReport = "CUSTOM ANALYTICS REPORT\n" +
                        "Period: " + startDate + " to " + endDate + "\n" +
                        "Report Type: " + reportType + "\n\n" +
                        "KEY METRICS:\n" +
                        "Total Bookings: 156\n" +
                        "Total Revenue: M97,845.00\n" +
                        "Average Booking Value: M627.21\n" +
                        "Unique Customers: 89\n\n" +
                        "PERFORMANCE SUMMARY:\n" +
                        "This period showed strong performance with consistent booking patterns " +
                        "and healthy revenue growth across all flight segments.";

                javafx.application.Platform.runLater(() -> {
                    TextArea textArea = new TextArea(customReport);
                    textArea.setEditable(false);
                    textArea.setWrapText(true);
                    textArea.setPrefSize(600, 400);

                    ScrollPane scrollPane = new ScrollPane(textArea);
                    scrollPane.setFitToWidth(true);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Custom Analytics Report");
                    alert.setHeaderText("Report for " + startDate + " to " + endDate);
                    alert.getDialogPane().setContent(scrollPane);
                    alert.showAndWait();

                    showLoading(false);
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Failed to generate custom report: " + e.getMessage(), Alert.AlertType.ERROR);
                    showLoading(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleRefreshAnalytics() {
        showLoading(true);

        new Thread(() -> {
            try {
                Thread.sleep(1000);

                javafx.application.Platform.runLater(() -> {
                    loadAnalyticsData();
                    refreshCharts();
                    loadPerformanceData();
                    showAlert("Analytics Updated", "All analytics data has been refreshed with latest information", Alert.AlertType.INFORMATION);
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @FXML
    private void handleExportData() {
        StringBuilder exportData = new StringBuilder("Flight Code,Bookings,Revenue,Occupancy Rate\n");

        for (FlightPerformance fp : performanceData) {
            exportData.append(String.format("%s,%d,M%.2f,%.1f%%\n",
                    fp.getFlightCode(), fp.getBookings(), fp.getRevenue(), fp.getOccupancyRate()));
        }

        TextArea textArea = new TextArea(exportData.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Data");
        alert.setHeaderText("Performance Data Copy to CSV");
        alert.getDialogPane().setContent(scrollPane);
        alert.showAndWait();
    }

    private void refreshCharts() {
        if (revenueChart != null) revenueChart.getData().clear();
        if (bookingChart != null) bookingChart.getData().clear();
        if (revenuePieChart != null) revenuePieChart.getData().clear();
        if (trendChart != null) trendChart.getData().clear();

        setupCharts();
    }

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
        }
        if (generateReportButton != null) {
            generateReportButton.setDisable(show);
        }
    }

    // Database methods (simplified for demo)
    private double getTotalRevenue() throws SQLException {
        return 128450.00;
    }

    private int getTotalBookings() throws SQLException {
        return 205;
    }

    private double getAverageBookingValue() throws SQLException {
        return 626.34;
    }

    private double getOccupancyRate() throws SQLException {
        return 78.3;
    }

    private int getCustomerSatisfactionRate() throws SQLException {
        return 94;
    }

    private double getCancellationRate() throws SQLException {
        return 4.2;
    }

    private int getRepeatCustomerCount() throws SQLException {
        return 45;
    }

    private double calculateRevenueGrowth() throws SQLException {
        return 12.5;
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
