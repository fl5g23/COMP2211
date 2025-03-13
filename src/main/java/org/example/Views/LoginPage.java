package org.example.Views;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.Controllers.UIController;

public class LoginPage {
    private final Stage primaryStage;
    private UIController controller;  // Reference to the controller

    public LoginPage(Stage stage) {
        this.primaryStage = stage;
    }

    public void show(Stage primaryStage) {
        // Create UI elements
        AnchorPane root = new AnchorPane();
        root.setPrefSize(385, 294);

        Label titleLabel = new Label("Ad Campaign Dashboard");
        titleLabel.setFont(new Font(21));
        titleLabel.setLayoutX(85);
        titleLabel.setLayoutY(18);

        Label usernameLabel = new Label("Username");
        usernameLabel.setLayoutX(14);
        usernameLabel.setLayoutY(73);

        Label passwordLabel = new Label("Password");
        passwordLabel.setLayoutX(14);
        passwordLabel.setLayoutY(152);

        TextField usernameField = new TextField();
        usernameField.setLayoutX(14);
        usernameField.setLayoutY(100);
        usernameField.setPrefSize(161, 26);

        TextField passwordField = new PasswordField(); // Changed to PasswordField for security
        passwordField.setLayoutX(14);
        passwordField.setLayoutY(178);
        passwordField.setPrefSize(161, 26);

        Button registerButton = new Button("Register");
        registerButton.setLayoutX(287);
        registerButton.setLayoutY(100);
        registerButton.setPrefSize(74, 26);
        registerButton.setOnAction(e -> {
            // In a real app, you would validate and register the user
            // For this demo, we'll just proceed to the main screen
            loginUser(usernameField.getText(), passwordField.getText());
        });

        Button loginButton = new Button("Login");
        loginButton.setLayoutX(287);
        loginButton.setLayoutY(178);
        loginButton.setPrefSize(74, 26);
        loginButton.setOnAction(e -> loginUser(usernameField.getText(), passwordField.getText()));

        // Add elements to the layout
        root.getChildren().addAll(titleLabel, usernameLabel, passwordLabel, usernameField, passwordField, registerButton, loginButton);

        // Set up scene and stage
        primaryStage.setTitle("Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private void loginUser(String username, String password) {
        // Create the controller upon successful login
        this.controller = new UIController(primaryStage);
        controller.showMainScreen();
    }

    // Method to set the controller (needed for testing or dependency injection)
    public void setController(UIController controller) {
        this.controller = controller;
    }
}