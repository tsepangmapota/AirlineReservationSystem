package com.airline.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;


public class LoginController {

    @FXML private ComboBox<String> userTypeComboBox;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private Button loginButton;

    @FXML
    public void initialize() {
        // Set up user types
        userTypeComboBox.getItems().addAll(
                "Customer",
                "Administrator",
                "Member 1 - Customer Management",
                "Member 2 - Flight Operations",
                "Member 3 - Reservations",
                "Member 4 - Cancellation & Refunds",
                "Member 5 - Fare Management",
                "Member 6 - Analytics"
        );
        userTypeComboBox.getSelectionModel().selectFirst();

        // Enable login button only when fields are filled
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> validateFields());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> validateFields());

        validateFields();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String userType = userTypeComboBox.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password.");
            return;
        }

        if (authenticateUser(username, password, userType)) {
            try {
                redirectToDashboard(userType);
            } catch (IOException e) {
                showAlert("Navigation Error", "Unable to load dashboard: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Login Failed", "Invalid username or password.");
        }
    }

    @FXML
    private void handleDemoLogin() {
        String userType = userTypeComboBox.getValue();
        if (userType != null) {
            switch (userType) {
                case "Customer":
                    usernameField.setText("customer_demo");
                    passwordField.setText("demo123");
                    break;
                case "Administrator":
                    usernameField.setText("admin_demo");
                    passwordField.setText("admin123");
                    break;
                case "Member 1 - Customer Management":
                    usernameField.setText("member1_demo");
                    passwordField.setText("member123");
                    break;
                case "Member 2 - Flight Operations":
                    usernameField.setText("member2_demo");
                    passwordField.setText("member123");
                    break;
                case "Member 3 - Reservations":
                    usernameField.setText("member3_demo");
                    passwordField.setText("member123");
                    break;
                case "Member 4 - Cancellation & Refunds":
                    usernameField.setText("member4_demo");
                    passwordField.setText("member123");
                    break;
                case "Member 5 - Fare Management":
                    usernameField.setText("member5_demo");
                    passwordField.setText("member123");
                    break;
                case "Member 6 - Analytics":
                    usernameField.setText("member6_demo");
                    passwordField.setText("member123");
                    break;
                default:
                    usernameField.setText("demo_user");
                    passwordField.setText("demo123");
            }
            validateFields();
        }
    }

    @FXML
    private void handleGuestAccess() {
        usernameField.setText("guest");
        passwordField.setText("guest123");
        validateFields();
    }

    @FXML
    private void handleSignUp() {
        showAlert("Sign Up", "Registration feature coming soon!");
    }

    @FXML
    private void handleForgotPassword() {
        showAlert("Password Recovery", "Please contact system administrator.");
    }

    private boolean authenticateUser(String username, String password, String userType) {
        // Simple authentication for demo - accept any non-empty credentials
        return !username.isEmpty() && !password.isEmpty();
    }

    private void redirectToDashboard(String userType) throws IOException {
        String fxmlFile = "";
        String title = "Skyline Airlines - ";

        // Map user types to their respective FXML files
        switch (userType) {
            case "Customer":
                fxmlFile = "/com/airline/view/main-dashboard.fxml";
                title += "Main Dashboard";
                break;
            case "Administrator":
                fxmlFile = "/com/airline/view/main-dashboard.fxml";
                title += "Administrator Dashboard";
                break;
            case "Member 1 - Customer Management":
                fxmlFile = "/com/airline/view/member1-dashboard.fxml";
                title += "Customer Management";
                break;
            case "Member 2 - Flight Operations":
                fxmlFile = "/com/airline/view/member2-dashboard.fxml";
                title += "Flight Operations";
                break;
            case "Member 3 - Reservations":
                fxmlFile = "/com/airline/view/member3-dashboard.fxml";
                title += "Reservations";
                break;
            case "Member 4 - Cancellation & Refunds":
                fxmlFile = "/com/airline/view/member4-dashboard.fxml";
                title += "Cancellation & Refunds";
                break;
            case "Member 5 - Fare Management":
                fxmlFile = "/com/airline/view/member5-dashboard.fxml";
                title += "Fare Management";
                break;
            case "Member 6 - Analytics":
                fxmlFile = "/com/airline/view/member6-dashboard.fxml";
                title += "Analytics";
                break;
            default:
                fxmlFile = "/com/airline/view/main-dashboard.fxml";
                title += "Main Dashboard";
        }

        System.out.println("Attempting to load: " + fxmlFile);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();

            System.out.println("Successfully loaded: " + fxmlFile);

        } catch (IOException e) {
            System.err.println("Failed to load FXML: " + fxmlFile);
            System.err.println("Error: " + e.getMessage());

            // Fallback to main dashboard if specific dashboard doesn't exist
            if (!fxmlFile.equals("/com/airline/view/main-dashboard.fxml")) {
                showAlert("Dashboard Not Available",
                        "The " + userType + " dashboard is not available yet. " +
                                "Redirecting to main dashboard.");
                redirectToDashboard("Customer");
            } else {
                throw e; // Re-throw if main dashboard also fails
            }
        }
    }

    private void validateFields() {
        boolean isValid = !usernameField.getText().trim().isEmpty() &&
                !passwordField.getText().isEmpty();
        loginButton.setDisable(!isValid);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
