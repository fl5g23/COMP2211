
package org.example;


import javafx.scene.Scene;

import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.scene.text.Font;
import javafx.stage.Stage;


public class LoginPage {
    private final Stage primaryStage;

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

        TextField passwordField = new TextField();
        passwordField.setLayoutX(14);
        passwordField.setLayoutY(178);
        passwordField.setPrefSize(161, 26);

        Button registerButton = new Button("Register");
        registerButton.setLayoutX(287);
        registerButton.setLayoutY(100);
        registerButton.setPrefSize(74, 26);
        registerButton.setOnAction(e -> loadMainScreen(primaryStage));

        Button loginButton = new Button("Login");
        loginButton.setLayoutX(287);
        loginButton.setLayoutY(178);
        loginButton.setPrefSize(74, 26);
        loginButton.setOnAction(e -> loadMainScreen(primaryStage));

        // Add elements to the layout
        root.getChildren().addAll(titleLabel, usernameLabel, passwordLabel, usernameField, passwordField, registerButton, loginButton);

        // Set up scene and stage
        primaryStage.setTitle("Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private void loadMainScreen(Stage stage) {
        MainScreen mainScreen = new MainScreen(stage);
        mainScreen.show();
    }


}