package org.example.Views;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.example.Controllers.UIController;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents the login page of the application.
 * Handles user authentication and registration interface.
 */
public class LoginPage {
    private final Stage primaryStage;
    private UIController controller;  // Reference to the controller

    /**
     * Constructs a new LoginPage with the specified primary stage.
     * Initializes the UI controller and performs initial setup.
     *
     * @param stage The primary stage for the login window
     */
    public LoginPage(Stage stage) {
        this.primaryStage = stage;
        this.controller = new UIController(primaryStage);
        controller.setup();
    }

    /**
     * Displays the login page interface.
     * Creates and configures the login form with username and password fields,
     * login and register buttons, and profile icon.
     *
     * @param primaryStage The primary stage where the login page will be displayed
     */
    public void show(Stage primaryStage) {
        // Create a VBox to hold the form elements
        VBox vbox = new VBox(10); // spacing between elements
        vbox.setPrefWidth(300);   // consistent width
        vbox.setStyle("-fx-padding: 30;");

        vbox.setAlignment(Pos.CENTER); // center elements horizontally

        //profile icon
        Image profileIcon = new Image(getClass().getResourceAsStream("/profile-icon.png"));
        ImageView profileImageView = new ImageView(profileIcon);
        profileImageView.setFitWidth(64);
        profileImageView.setFitHeight(64);

        // Title
        Label titleLabel = new Label("Log in");
        titleLabel.setFont(Font.font("System", FontWeight.BLACK, 50));

        // Username
        Label usernameLabel = new Label("Username");
        usernameLabel.setFont(new Font(26));
        TextField usernameField = new TextField();
        usernameField.setMaxWidth(Double.MAX_VALUE);
        usernameField.setFont(new Font(26));

        // Password
        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(new Font(26));
        PasswordField passwordField = new PasswordField();
        passwordField.setMaxWidth(Double.MAX_VALUE);
        passwordField.setFont(new Font(26));

        // Buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        Button registerButton = new Button("Register");
        Button loginButton = new Button("Login");
        registerButton.setPrefSize(160, 60);
        loginButton.setPrefSize(160, 60);
        registerButton.setFont(Font.font("System", 20));
        loginButton.setFont(Font.font("System", 20));


        registerButton.setOnAction(e -> {
            if (usernameField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()) {
                controller.showAlert(null, "userpwdempty");
            } else {
                addUser(usernameField.getText(), passwordField.getText());
            }
        });

        loginButton.setOnAction(e -> {
            if (usernameField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()) {
                controller.showAlert(null, "userpwdempty");
            } else {
                loginUser(usernameField.getText(), passwordField.getText());
                controller.setup();
            }
        });

        buttonBox.getChildren().addAll(registerButton, loginButton);

        // Add elements to VBox
        vbox.getChildren().addAll(
            profileImageView,
            usernameLabel, usernameField,
            passwordLabel, passwordField,
            buttonBox
        );

        // Center the VBox using StackPane
        StackPane root = new StackPane(vbox);
        root.setPrefSize(414, 322);

        // Set up scene and stage
        Scene scene = new Scene(root);
        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Handles user registration process.
     * Validates if username already exists and adds new user if validation passes.
     *
     * @param username The username to register
     * @param password The password for the new account
     */
    private void addUser(String username, String password){
        ArrayList<Object> authorisedResult = controller.userExists(username, password);
        Boolean usernameValid = (Boolean) authorisedResult.get(0);
        if (usernameValid){
            controller.showAlert(null, "usernamealreadyexists");
        }else{
            controller.addUser(username, password);
        }
    }

    /**
     * Handles user login authentication.
     * Validates username, password, and authorization status.
     * Shows appropriate error messages for invalid credentials or unauthorized access.
     * Redirects to main screen upon successful authentication.
     *
     * @param username The username to authenticate
     * @param password The password to verify
     */
    private void loginUser(String username, String password) {
        ArrayList<Object> authorisedResult = controller.userExists(username, password);
        Boolean usernameValid = (Boolean) authorisedResult.get(0);
        Boolean passwordValid = (Boolean) authorisedResult.get(1);
        Boolean authorisedStatus = (Boolean) authorisedResult.get(2);
        String userRole = (String) authorisedResult.get(3);

        if(usernameValid && passwordValid && !(authorisedStatus)){
            controller.showAlert(null,"notauthorised");
        }
        else if((!usernameValid)){
            controller.showAlert(null, "usernotexist");
        }
        else if(!(passwordValid)){
            controller.showAlert(null, "passwordwrong");
        }
        else if (usernameValid && passwordValid && authorisedStatus){
            controller.showMainScreen(userRole);
        }
    }

    /**
     * Sets the UI controller for the login page.
     * Used for testing purposes or dependency injection.
     *
     * @param controller The UIController instance to be used
     */
    // Method to set the controller (needed for testing or dependency injection)
    public void setController(UIController controller) {
        this.controller = controller;
    }
}