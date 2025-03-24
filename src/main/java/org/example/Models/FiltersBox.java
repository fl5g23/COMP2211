package org.example.Models;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class FiltersBox extends VBox {

    private String gender;
    private String age;
    private String context;
    private String income;
    private String bounceValue;
    private String granularity;
    private String campaignName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String metric;
    private ToggleButton pageleftBounceToggle;
    private ToggleButton dayToggle;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;


    // Constructor
    public FiltersBox(String title, String campaignName, LocalDate startDate, LocalDate endDate, Integer prefHorizontalSize, Integer prefVerticalSize) {
        this.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 15px;");
        this.setPrefSize(prefHorizontalSize, prefVerticalSize);
        this.setAlignment(Pos.CENTER);
        this.campaignName = campaignName;

        // Top filters
        HBox topfilterBox = new HBox();
        ComboBox<String> genderComboBox = new ComboBox<>();
        genderComboBox.getItems().addAll("Male", "Female", "All");
        genderComboBox.setPromptText("Gender");
        genderComboBox.setPrefSize(100, 26);

        ComboBox<String> ageComboBox = new ComboBox<>();
        ageComboBox.getItems().addAll("<25", "25-34", "35-44", "45-54", ">54", "All");
        ageComboBox.setPromptText("Age");
        ageComboBox.setPrefSize(100, 26);

        topfilterBox.getChildren().addAll(genderComboBox, ageComboBox);
        topfilterBox.setPadding(new Insets(5, 5, 0, 0));
        topfilterBox.setAlignment(Pos.CENTER);

        // Bottom filters
        HBox bottomfilterBox = new HBox();

        ComboBox<String> incomeComboBox = new ComboBox<>();
        incomeComboBox.getItems().addAll("Low", "Medium", "High", "All");
        incomeComboBox.setPromptText("Income");
        incomeComboBox.setPrefSize(100, 26);

        ComboBox<String> contextComboBox = new ComboBox<>();
        contextComboBox.getItems().addAll("News", "Shopping", "Social Media", "Blog", "Hobbies", "Travel", "All");
        contextComboBox.setPromptText("Context");
        contextComboBox.setPrefSize(100, 26);

        bottomfilterBox.getChildren().addAll(incomeComboBox, contextComboBox);
        bottomfilterBox.setPadding(new Insets(5, 5, 0, 0));
        bottomfilterBox.setAlignment(Pos.CENTER);

        // Time Granularity
        Label timeGranularityLabel = new Label("Time Granularity");
        timeGranularityLabel.setPadding(new Insets(5, 5, 0, 0));
        HBox timeGranularityToggleBox = new HBox();

        ToggleGroup timeGranularityGroup = new ToggleGroup();
        ToggleButton hourToggle = new ToggleButton("Hour");
        dayToggle = new ToggleButton("Day");
        ToggleButton weekToggle = new ToggleButton("Week");

        hourToggle.setToggleGroup(timeGranularityGroup);
        dayToggle.setToggleGroup(timeGranularityGroup);
        weekToggle.setToggleGroup(timeGranularityGroup);

        timeGranularityToggleBox.getChildren().addAll(hourToggle, dayToggle, weekToggle);
        timeGranularityToggleBox.setPadding(new Insets(5, 5, 0, 0));
        timeGranularityToggleBox.setAlignment(Pos.CENTER);

        // Bounce Definition
        Label bounceDefinitionLabel = new Label("Bounce Definition");
        ToggleGroup bounceDefinitionGroup = new ToggleGroup();
        pageleftBounceToggle = new ToggleButton("Page Left");
        ToggleButton singlePageBounceToggle = new ToggleButton("Single Page");

        pageleftBounceToggle.setToggleGroup(bounceDefinitionGroup);
        singlePageBounceToggle.setToggleGroup(bounceDefinitionGroup);

        bounceDefinitionLabel.setPadding(new Insets(5, 5, 0, 0));
        HBox bounceDefinitionBox = new HBox();
        bounceDefinitionBox.getChildren().addAll(pageleftBounceToggle, singlePageBounceToggle);
        bounceDefinitionBox.setPadding(new Insets(5, 5, 0, 0));
        bounceDefinitionBox.setAlignment(Pos.CENTER);

        // Date pickers
        Label datePickerLabel = new Label("Date Range");
        HBox datePickerBox = new HBox();

        startDatePicker = new DatePicker();
        startDatePicker.setPrefSize(200.0, 35.0);
        if (this.startDate!= null){
            startDatePicker.setValue(startDate);
        }
        Label toLabel = new Label("to");
        endDatePicker = new DatePicker();
        endDatePicker.setPrefSize(200.0, 35.0);
        if (this.endDate != null){
            endDatePicker.setValue(endDate);
        }
        datePickerBox.getChildren().addAll(startDatePicker, toLabel, endDatePicker);
        datePickerBox.setPadding(new Insets(5, 0, 15, 0));

    if (title != null) {
      Label filterTitle = new Label(title);
      this.getChildren().add(filterTitle);
    }

    // Add all components to this VBox
    this.getChildren()
        .addAll(
            topfilterBox,
            bottomfilterBox,
            timeGranularityLabel,
            timeGranularityToggleBox,
            bounceDefinitionLabel,
            bounceDefinitionBox,
            datePickerLabel,
            datePickerBox);

        // Listeners to update properties
        genderComboBox.valueProperty().addListener((obs, oldVal, newVal) -> gender = newVal);
        ageComboBox.valueProperty().addListener((obs, oldVal, newVal) -> age = newVal);
        incomeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> income = newVal);
        contextComboBox.valueProperty().addListener((obs, oldVal, newVal) -> context = newVal);
        bounceDefinitionGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == pageleftBounceToggle) {
                bounceValue = "PageLeft";
            } else if (newVal == singlePageBounceToggle) {
                bounceValue = "SinglePage";
            }
        });

        timeGranularityGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == hourToggle) {
                granularity = "Hourly";
            } else if (newVal == dayToggle) {
                granularity = "Daily";
            } else if (newVal == weekToggle) {
                granularity = "Weekly";
            } else {
                granularity = "Daily";
            }
        });

        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                this.startDate = newVal.atTime(0, 0, 0);
            }
        });

        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                this.endDate = newVal.atTime(23, 59, 59);
            }
        });

    }


    public void selectFirstGenerationFilters(LocalDate start, LocalDate end, String campaignName){
        bounceValue = "PageLeft";
        metric = "Impressions";
        granularity = "Daily";
        gender = "All";
        this.campaignName = campaignName;
        pageleftBounceToggle.setSelected(true);
        dayToggle.setSelected(true);
        startDatePicker.setValue(start);
        endDatePicker.setValue(end);
    }

    public void selectFirstGenerationFilters(){
        bounceValue = "PageLeft";
        metric = "Impressions";
        granularity = "Daily";
        gender = "All";
    }

  public String getCampaignName() {
        return campaignName;
    }

    // Getters
    public String getGender() {
        return gender;
    }

    public String getAge() {
        return age;
    }

    public String getIncome() {
        return income;
    }

    public String getBounceValue() {
        return bounceValue;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public String getContext() {
        return context;
    }

    public String getGranularity() {
        return granularity;
    }

    public void setMetric(String metric){
        this.metric = metric;
    }

    public String getMetric() {
        return metric;
    }
}