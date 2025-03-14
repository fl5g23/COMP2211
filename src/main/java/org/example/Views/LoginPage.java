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
        registerButton.setOnAction(
                e -> {
                    if (usernameField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()) {
                        controller.showAlert(null, "userpwdempty");
                    } else {
                        addUser(usernameField.getText(), passwordField.getText());
                    }
                });

        Button loginButton = new Button("Login");
        loginButton.setLayoutX(287);
        loginButton.setLayoutY(178);
        loginButton.setPrefSize(74, 26);
        loginButton.setOnAction(e -> {
            if (usernameField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()) {
                controller.showAlert(null, "userpwdempty");
            } else {
                loginUser(usernameField.getText(), passwordField.getText());
            }
        });

        // Add elements to the layout
        root.getChildren().addAll(titleLabel, usernameLabel, passwordLabel, usernameField, passwordField, registerButton, loginButton);

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