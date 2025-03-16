package org.example.Views;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.Controllers.UIController;

import java.util.ArrayList;

public class AuthoriseUsersView {

    private Stage primaryStage;
    private UIController controller;
    private ArrayList<String> userList; // Instance variable to hold the list of unauthorised users

    public AuthoriseUsersView(Stage primaryStage, UIController controller) {
        this.primaryStage = primaryStage;
        this.controller = controller;
        this.userList = this.controller.getUnauthorisedUsers(); // Get unauthorised users from the controller
    }


    public void show() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Authorise Users");
        AnchorPane root = new AnchorPane();
        root.setPrefSize(428, 340); // Increased height to accommodate the new admin checkbox
        dialogStage.initOwner(primaryStage);
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        // ListView for users
        ListView<String> listView = new ListView<>();
        listView.setPrefSize(160, 232);
        AnchorPane.setTopAnchor(listView, 40.0);
        AnchorPane.setLeftAnchor(listView, 23.0);

        // Convert the ArrayList to ObservableList and set it to the ListView
        ObservableList<String> users = FXCollections.observableArrayList(userList);
        listView.setItems(users);

        // Buttons
        Button acceptButton = new Button("Accept");
        acceptButton.setLayoutX(227);
        acceptButton.setLayoutY(84);

        Button rejectButton = new Button("Reject");
        rejectButton.setLayoutX(340);
        rejectButton.setLayoutY(84);

        // Admin checkbox
        CheckBox adminCheckBox = new CheckBox("Make Admin");
        adminCheckBox.setLayoutX(227);
        adminCheckBox.setLayoutY(130);

        // Label
        Label titleLabel = new Label("Authorise Users");
        titleLabel.setPrefSize(160, 17);
        titleLabel.setLayoutX(134);
        titleLabel.setLayoutY(14);
        titleLabel.setStyle("-fx-alignment: center;");

        // Info label for admin status
        Label adminInfoLabel = new Label("Only admins can manage users and create other admins");
        adminInfoLabel.setPrefSize(300, 17);
        adminInfoLabel.setLayoutX(64);
        adminInfoLabel.setLayoutY(290);
        adminInfoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555555;");

        // Add elements to root
        root.getChildren().addAll(listView, acceptButton, rejectButton, titleLabel, adminCheckBox, adminInfoLabel);

        // Accept Button Action
        acceptButton.setOnAction(e -> {
            String selectedUser = listView.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                // Check if the admin checkbox is selected
                if (adminCheckBox.isSelected()) {
                    controller.authoriseUser(selectedUser, "Admin");
                    System.out.println("User " + selectedUser + " accepted and authorised as admin.");
                } else {
                    controller.authoriseUser(selectedUser, "User");
                    System.out.println("User " + selectedUser + " accepted and authorised.");
                }
                listView.getItems().remove(selectedUser);
            } else {
                System.out.println("No user selected.");
            }
        });

        // Reject Button Action
        rejectButton.setOnAction(e -> {
            String selectedUser = listView.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                controller.deleteUser(selectedUser);
                System.out.println("User " + selectedUser + " rejected.");
                listView.getItems().remove(selectedUser);
            } else {
                System.out.println("No user selected.");
            }
        });

        // Set up scene and stage
        Scene scene = new Scene(root);
        dialogStage.setTitle("User Authorisation");
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

}