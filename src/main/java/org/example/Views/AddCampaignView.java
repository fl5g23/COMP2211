package org.example.Views;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.Controllers.UIController;

import java.io.File;

/**
 * Represents the view for adding a new advertising campaign.
 * Provides interface for entering campaign details and uploading log files.
 */
public class AddCampaignView {

    private File impressionLogFile;
    private File clicksLogFile;
    private File serverLogFile;
    private Stage stage;
    private UIController controller;  // Reference to the controller
    private Boolean impression_log_flag = false;
    private Boolean click_log_flag = false;
    private Boolean server_log_flag = false;

    /**
     * Constructs a new AddCampaignView instance.
     * 
     * @param primaryStage The main application window
     * @param controller The UIController instance for handling business logic
     */
    public AddCampaignView(Stage primaryStage, UIController controller) {
        this.stage = primaryStage;
        this.controller = controller;
    }

    /**
     * Opens a modal dialog for adding a new campaign.
     * Provides fields for campaign name and buttons for uploading impression, click, and server logs.
     * Validates file formats and campaign name before saving.
     *
     * @param titleLabel The label to be updated after campaign creation
     * @param rootContainer The container that holds the campaign interface elements
     */
    public void openAddCampaignDialog(Label titleLabel, HBox rootContainer) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Add Campaign");
        dialogStage.initOwner(stage);
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        AnchorPane root = new AnchorPane();
        root.setPrefSize(280, 227);

        TextField campaignNameField = new TextField("Enter Campaign Name");
        campaignNameField.setLayoutX(14);
        campaignNameField.setLayoutY(26);
        campaignNameField.setPrefSize(201, 26);

        Button impressionLogButton = new Button("Impression Log");
        impressionLogButton.setLayoutX(14);
        impressionLogButton.setLayoutY(67);
        impressionLogButton.setPrefSize(124, 26);
        impressionLogButton.setOnAction(e -> {
            File selectedFile = openFileFinder();
            if (selectedFile != null) {
                impressionLogFile = selectedFile;
                if (controller.checkFileFormat(selectedFile, "Impression")) {
                    impression_log_flag = true;
                    impressionLogButton.setText(selectedFile.getName());
                } else {
                    controller.showAlert(selectedFile, "Impression");
                }
            }
        });

        Button clicksLogButton = new Button("Click Log");
        clicksLogButton.setLayoutX(14);
        clicksLogButton.setLayoutY(100);
        clicksLogButton.setPrefSize(124, 26);
        clicksLogButton.setOnAction(e -> {
            File selectedFile = openFileFinder();
            if (selectedFile != null) {
                clicksLogFile = selectedFile;
                if (controller.checkFileFormat(selectedFile, "Click")) {
                    click_log_flag = true;
                    clicksLogButton.setText(selectedFile.getName());
                } else {
                    controller.showAlert(selectedFile, "Click");
                }
            }
        });

        Button serverLogButton = new Button("Server Log");
        serverLogButton.setLayoutX(14);
        serverLogButton.setLayoutY(133);
        serverLogButton.setPrefSize(124, 26);
        serverLogButton.setOnAction(e -> {
            File selectedFile = openFileFinder();
            if (selectedFile != null) {
                serverLogFile = selectedFile;
                if (controller.checkFileFormat(selectedFile, "Server")) {
                    server_log_flag = true;
                    serverLogButton.setText(selectedFile.getName());
                } else {
                    controller.showAlert(selectedFile, "Server");
                }
            }
        });

        Button saveButton = new Button("Save");
        saveButton.setLayoutX(211);
        saveButton.setLayoutY(187);
        saveButton.setPrefSize(55, 26);
        saveButton.setOnAction(
        e -> {
          String campaignName = campaignNameField.getText().trim();
          if (controller.isValidCampaignName(campaignName)) {
            // Delegate the entire save process to the controller
            boolean saveSuccessful =
                controller.saveCampaign(
                    campaignName,
                    impressionLogFile,
                    clicksLogFile,
                    serverLogFile,
                    impression_log_flag,
                    click_log_flag,
                    server_log_flag,
                    titleLabel,
                    rootContainer);

            if (saveSuccessful) {
              dialogStage.close();
            }
          }else{
              controller.showAlert(null, "campaignname");
          }
        });

    Button cancelButton = new Button("Cancel");
        cancelButton.setLayoutX(140);
        cancelButton.setLayoutY(188);
        cancelButton.setOnAction(e -> dialogStage.close());

        root.getChildren()
                .addAll(
                        campaignNameField,
                        impressionLogButton,
                        clicksLogButton,
                        serverLogButton,
                        saveButton,
                        cancelButton);

        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    /**
     * Opens a file chooser dialog for selecting CSV files.
     * Configured to only show and accept CSV format files.
     *
     * @return The selected File object, or null if no file was selected
     */
    public File openFileFinder() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        return fileChooser.showOpenDialog(stage);
    }

    /**
     * Resets all form fields and validation flags to their initial state.
     * Clears all selected files and their corresponding flags.
     */
    public void resetForm() {
        impressionLogFile = null;
        clicksLogFile = null;
        serverLogFile = null;
        impression_log_flag = false;
        click_log_flag = false;
        server_log_flag = false;
    }
}