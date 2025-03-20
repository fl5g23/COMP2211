package org.example.Views;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.Controllers.UIController;
import org.example.Models.Campaign;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class CompareGraphsView{
    private Stage primaryStage;
    private UIController uiController;
    private ComboBox<String> metricDropdown;
    private ComboBox<String> genderComboBox1, ageComboBox1, incomeComboBox1, contextComboBox1;
    private ComboBox<String> genderComboBox2, ageComboBox2, incomeComboBox2, contextComboBox2;
    private DatePicker startDatePickerLeft, endDatePickerLeft, startDatePickerRight, endDatePickerRight;
    private ToggleButton hourToggleLeft, hourToggleRight, dayToggleLeft, dayToggleRight, weekToggleLeft, weekToggleRight, pageleftBounceToggleLeft, pageleftBounceToggleRight, singlePageBounceToggleLeft, singlePageBounceToggleRight;
    private ToggleGroup bounceDefinitionGroupLeft, bounceDefinitionGroupRight, timeGranularityGroupLeft, timeGranularityGroupRight;
    private LocalDate startDate, endDate;
    private Campaign campaign;


    public CompareGraphsView(Stage primaryStage, UIController uiController, DatePicker startDate, DatePicker endDate, Campaign campaign){
        this.primaryStage = primaryStage;
        this.uiController = uiController;
        this.startDate = startDate.getValue();
        this.endDate = endDate.getValue();
        this.campaign = campaign;
    }

    public void show(){
    Stage stage = new Stage();
    stage.setTitle("Compare Graphs");

    HBox topBar = new HBox();
    topBar.setAlignment(Pos.TOP_LEFT);

    VBox metricBox = new VBox();
    metricBox.setAlignment(Pos.CENTER);
    Label metricLabel = new Label("Choose a Metric:");
    metricDropdown = new ComboBox<>();
    metricDropdown.getItems().addAll("Impressions", "Clicks", "Uniques", "Bounces" );
    metricDropdown.setValue("Impressions");
    metricBox.getChildren().addAll(metricLabel, metricDropdown);
    metricBox.setSpacing(5);

    VBox graph1Filters = createFiltersPanel("Choose Filters for Graph 1:", startDate,endDate,true);
    VBox graph2Filters = createFiltersPanel("Choose Filters for Graph 2:", startDate, endDate,false);

    Button compareButton = new Button("Compare");
    compareButton.setOnAction(e -> handleCompareButton(stage));

    HBox filtersContainer = new HBox(40, graph1Filters, graph2Filters);
    filtersContainer.setAlignment(Pos.CENTER);
    filtersContainer.setPadding(new Insets(20, 0, 20, 0));

    VBox root = new VBox(15, topBar, metricBox, filtersContainer, compareButton);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(20));

    Scene showScene = new Scene(root, 800, 500);
    stage.setScene(showScene);
    stage.show();
    }


  private VBox createFiltersPanel(String title, LocalDate startDate, LocalDate endDate, Boolean isLeftGraph) {
    VBox filtersPanel = new VBox();
    filtersPanel.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 15px;");
    filtersPanel.setPrefSize(300, 500);

    HBox topfilterBox = new HBox();
    // Gender and Age ComboBoxes
    ComboBox<String> genderComboBox = new ComboBox<>();
    genderComboBox.getItems().addAll("Male", "Female", "All");
    genderComboBox.setPromptText("Gender");
    genderComboBox.setPrefSize(100, 26);

    ComboBox<String> ageComboBox = new ComboBox<>();
    ageComboBox.promptTextProperty().set("Age");
    ageComboBox.getItems().addAll("<25", "25-34", "35-44", "45-54", ">54", "All");
    ageComboBox.setPrefSize(100, 26);

    topfilterBox.getChildren().addAll(genderComboBox, ageComboBox);
    topfilterBox.setPadding(new Insets(5, 5, 0, 0));
    topfilterBox.setAlignment(Pos.CENTER);

    // Income and Context
    HBox bottomfilterBox = new HBox();

    // Income and Context ComboBoxes
    ComboBox<String> incomeComboBox = new ComboBox<>();
    incomeComboBox.setPromptText("Income");
    incomeComboBox.getItems().addAll("Low", "Medium", "High", "All");
    incomeComboBox.setPrefSize(100, 26);

    ComboBox<String> contextComboBox = new ComboBox<>();
    contextComboBox.promptTextProperty().set("Context");
    contextComboBox
        .getItems()
        .addAll("News", "Shopping", "Social Media", "Blog", "Hobbies", "Travel", "All");
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
    ToggleButton dayToggle = new ToggleButton("Day");
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


    ToggleButton pageleftBounceToggle = new ToggleButton("Page Left");
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

    DatePicker startDatePicker = new DatePicker();
    startDatePicker.setPrefSize(200.0, 35.0);
    startDatePicker.setValue(startDate);
    Label toLabel = new Label("to");
    DatePicker endDatePicker = new DatePicker();
    endDatePicker.setPrefSize(200.0, 35.0);
    endDatePicker.setValue(endDate);
    datePickerBox.getChildren().addAll(startDatePicker, toLabel, endDatePicker);
    datePickerBox.setPadding(new Insets(5, 0, 15, 0));

    Label filterTitle = new Label(title);

      // Adding  content to panel
      filtersPanel
              .getChildren()
              .addAll(
                      filterTitle,
                      topfilterBox,
                      bottomfilterBox,
                      timeGranularityLabel,
                      timeGranularityToggleBox,
                      bounceDefinitionLabel,
                      bounceDefinitionBox,
                      datePickerLabel,
                      datePickerBox
                      );
      filtersPanel.setAlignment(Pos.CENTER);

      if (isLeftGraph) {
          genderComboBox1 = genderComboBox;
          ageComboBox1 = ageComboBox;
          incomeComboBox1 = incomeComboBox;
          contextComboBox1 = contextComboBox;
          bounceDefinitionGroupLeft = bounceDefinitionGroup;
          singlePageBounceToggleLeft = singlePageBounceToggle;
          pageleftBounceToggleLeft = pageleftBounceToggle;
          timeGranularityGroupLeft = timeGranularityGroup;
          hourToggleLeft = hourToggle;
          dayToggleLeft = dayToggle;
          weekToggleLeft = weekToggle;
          startDatePickerLeft = startDatePicker;
          endDatePickerLeft = endDatePicker;
      } else {
          genderComboBox2 = genderComboBox;
          ageComboBox2 = ageComboBox;
          incomeComboBox2 = incomeComboBox;
          contextComboBox2 = contextComboBox;
          bounceDefinitionGroupRight = bounceDefinitionGroup;
          singlePageBounceToggleRight = singlePageBounceToggle;
          pageleftBounceToggleRight = pageleftBounceToggle;
          timeGranularityGroupRight = timeGranularityGroup;
          hourToggleRight = hourToggle;
          dayToggleRight = dayToggle;
          weekToggleRight = weekToggle;
          startDatePickerRight = startDatePicker;
          endDatePickerRight = endDatePicker;
      }


      return filtersPanel;


  }

  private void handleCompareButton(Stage compareStage) {


      Toggle selectedToggleLeft = timeGranularityGroupLeft.getSelectedToggle();
      Toggle selectedToggleRight = timeGranularityGroupLeft.getSelectedToggle();
      String granularityLeft;
      String granularityRight;
      String bounceTypeLeft = pageleftBounceToggleLeft.isSelected() ? "PageLeft" : "SinglePage";
      String bounceTypeRight = pageleftBounceToggleRight.isSelected() ? "PageLeft" : "SinglePage";

      if (selectedToggleLeft == hourToggleLeft) {
          granularityLeft = "Hourly";
      } else if (selectedToggleLeft == dayToggleLeft) {
          granularityLeft = "Daily";
      } else if (selectedToggleLeft == weekToggleLeft) {
          granularityLeft = "Weekly";
      } else {
          granularityLeft = "Daily"; // Default option clearly set here
      }

      if (selectedToggleRight == hourToggleRight) {
          granularityRight = "Hourly";
      } else if (selectedToggleRight == dayToggleRight) {
          granularityRight = "Daily";
      } else if (selectedToggleRight == weekToggleRight) {
          granularityRight = "Weekly";
      } else {
          granularityRight = "Daily"; // Default option clearly set here
      }


      String metric= metricDropdown.getValue();
      String campaignName = campaign.getName();
        String gender1 = genderComboBox1.getValue();
        String age1 = ageComboBox1.getValue();
        String income1 = incomeComboBox1.getValue();
        String context1 = contextComboBox1.getValue();
        String granularity1 = granularityLeft;
        LocalDateTime startselectedDateLeft = startDatePickerLeft.getValue().atTime(00, 00, 00);
        LocalDateTime endselectedDateLeft = endDatePickerLeft.getValue().atTime(23, 59, 59);
        Map<String,String> filtersMap1 = new HashMap<>();
        filtersMap1.put("campaignName", campaignName);
        filtersMap1.put("selectedMetric", metric);
        filtersMap1.put("Gender", gender1);
        filtersMap1.put("Age", age1);
        filtersMap1.put("Income", income1);
        filtersMap1.put("Context", context1);
        filtersMap1.put("Granularity", granularity1);
        filtersMap1.put("bounceDefinition", bounceTypeLeft);

      String gender2 = genderComboBox2.getValue();
      String age2 = ageComboBox2.getValue();
      String income2 = incomeComboBox2.getValue();
      String context2 = contextComboBox2.getValue();
      String granularity2 = granularityRight;
      LocalDateTime startselectedDateRight = startDatePickerRight.getValue().atTime(00, 00, 00);
      LocalDateTime endselectedDateRight = endDatePickerRight.getValue().atTime(23, 59, 59);
      Map<String,String> filtersMap2 = new HashMap<>();
      filtersMap2.put("campaignName", campaignName);
      filtersMap2.put("selectedMetric", metric);
      filtersMap2.put("Gender", gender2);
      filtersMap2.put("Age", age2);
      filtersMap2.put("Income", income2);
      filtersMap2.put("Context", context2);
      filtersMap2.put("Granularity", granularity2);
      filtersMap2.put("bounceDefinition", bounceTypeRight);

        CompareGraphsResultView resultView = new CompareGraphsResultView( primaryStage, uiController,filtersMap1, startselectedDateLeft, endselectedDateLeft,
                filtersMap2, startselectedDateRight, endselectedDateRight);
        resultView.show();
        compareStage.close();
    }

}