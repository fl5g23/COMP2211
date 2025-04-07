package org.example.Views;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.example.Controllers.UIController;

import java.util.ArrayList;

public class LoginPage {
    private final Stage primaryStage;
    private UIController controller;  // Reference to the controller

    public LoginPage(Stage stage) {
        this.primaryStage = stage;
        this.controller = new UIController(primaryStage);
        controller.setup();
    }

    public void show(Stage primaryStage) {
        AnchorPane root = new AnchorPane();
        root.setPrefSize(414, 322); // Match FXML dimensions

        double leftMargin = 33;
        double textFieldWidth = 414 - (2 * leftMargin);
        double labelYOffset = 20;
        double fieldHeight = 26;

        // Title Label
        Label titleLabel = new Label("Ad Campaign Dashboard");
        titleLabel.setFont(new Font(21));
        titleLabel.setLayoutX(95);
        titleLabel.setLayoutY(30);

        // Username Label
        Label usernameLabel = new Label("Username");
        usernameLabel.setLayoutX(leftMargin);
        usernameLabel.setLayoutY(80);

        // Username Field
        TextField usernameField = new TextField();
        usernameField.setLayoutX(leftMargin);
        usernameField.setLayoutY(usernameLabel.getLayoutY() + labelYOffset);
        usernameField.setPrefSize(textFieldWidth, fieldHeight);

        // Password Label
        Label passwordLabel = new Label("Password");
        passwordLabel.setLayoutX(leftMargin);
        passwordLabel.setLayoutY(usernameField.getLayoutY() + 50);

        // Password Field
        PasswordField passwordField = new PasswordField();
        passwordField.setLayoutX(leftMargin);
        passwordField.setLayoutY(passwordLabel.getLayoutY() + labelYOffset);
        passwordField.setPrefSize(textFieldWidth, fieldHeight);

        // Register Button
        Button registerButton = new Button("Register");
        registerButton.setPrefSize(100, 30);
        registerButton.setLayoutX(95);
        registerButton.setLayoutY(passwordField.getLayoutY() + 60);
        registerButton.setOnAction(
                e -> {
                    if (usernameField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()) {
                        controller.showAlert(null, "userpwdempty");
                    } else {
                        addUser(usernameField.getText(), passwordField.getText());
                    }
                });

        // Login Button
        Button loginButton = new Button("Login");
        loginButton.setPrefSize(100, 30);
        loginButton.setLayoutX(220);
        loginButton.setLayoutY(passwordField.getLayoutY() + 60);
        loginButton.setOnAction(e -> {
            if (usernameField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()) {
                controller.showAlert(null, "userpwdempty");
            } else {
                loginUser(usernameField.getText(), passwordField.getText());
                controller.setup();
            }
        });

        // Add all nodes
        root.getChildren().addAll(titleLabel, usernameLabel, usernameField, passwordLabel, passwordField, registerButton, loginButton);

        // Set up scene and stage
        primaryStage.setTitle("Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private void addUser(String username, String password){
        ArrayList<Object> authorisedResult = controller.userExists(username, password);
        Boolean usernameValid = (Boolean) authorisedResult.get(0);
        if (usernameValid){
            controller.showAlert(null, "usernamealreadyexists");
        }else{
            controller.addUser(username, password);
        }
    }

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


    // Method to set the controller (needed for testing or dependency injection)
    public void setController(UIController controller) {
        this.controller = controller;
    }
}