package org.example.Views;

import java.util.*;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;

import org.example.Models.Campaign;
import org.example.Models.ClickCostHistogram;
import org.example.Controllers.UIController;
import org.jfree.chart.ChartPanel;
import javafx.embed.swing.SwingNode;

public class MainScreen {

  private final Stage primaryStage;
  private MenuButton campaignMenuButton; // Replaces the title label after a campaign is added
  private SwingNode swingNode = new SwingNode();
  private LineChart<String, Number> lineChart;
  private StackPane chartContainer;
  private ChartPanel histogramPanel;
  private ClickCostHistogram clickCostHistogram = new ClickCostHistogram();
  private boolean isClickByCost = true; // Track histogram type (true = Clicks by Cost, false = Clicks by Time)
  private boolean isDataDownloaded = false; // Track if data is available for histograms
  private UIController controller; // Reference to the controller
  Boolean firstGraphGeneration = true; //solves bug with chart generation

  // Core metric labels
  Label keyMetricsTitle = new Label("Key Metrics:");
  Label impressionsLabel = new Label("Impressions: ");
  Label clicksLabel = new Label("Clicks on Ad: ");
  Label uniquesLabel = new Label("Unique Clicks on Ad: ");
  Label conversionsLabel = new Label("Conversions: ");
  Label bounceRateLabel = new Label("Bounce Rate: ");
  Label ctrLabel = new Label("CTR: ");
  Label cpaLabel = new Label("CPA: ");
  Label cpcLabel = new Label("CPC: ");
  Label cpmLabel = new Label("CPM: ");
  Label totalCostLabel = new Label("Total Cost: ");
  Label keyMetricsValue = new Label("");
  Label impressionsValue = new Label("");
  Label clicksValue = new Label("");
  Label uniquesValue = new Label("");
  Label conversionsValue = new Label("");
  Label bounceRateValue = new Label("");
  Label ctrValue = new Label("");
  Label cpaValue = new Label("");
  Label cpcValue = new Label("");
  Label cpmValue = new Label("");
  Label totalCostValue = new Label("");
  Button toggleChartBtn = new Button("Switch to Histogram");

  ToggleButton pageleftBounceToggle = new ToggleButton("Page Left");
  ToggleButton singlePageBounceToggle = new ToggleButton("Single Page");
  private ToggleButton toggleHistogramTypeBtn = new ToggleButton("Clicks by time");
  ComboBox<String> metricDropdown = new ComboBox<>();
  /**
   * Constructor that accepts the controller
   */
  public MainScreen(Stage stage, UIController controller) {
    this.primaryStage = stage;
    this.controller = controller;
    keyMetricsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bolder; -fx-underline: true;");
  }

  /**
   * Initialize and show the main screen
   */
  public void show(String role) {
    primaryStage.setTitle("Ad Campaign Dashboard");

    // Top bar with title and Add Campaign button
    HBox topBar = new HBox();
    topBar.setStyle("-fx-background-color: #ddd; -fx-padding: 10px;");

    // Initially set a Label (will be replaced by MenuButton)
    Label title = new Label("Add Campaign");
    title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #328ccd;");
    title.setOnMouseClicked(e -> controller.openAddCampaignDialog(title, topBar)); // Use controller

    Button logoutButton = new Button("Logout");
    logoutButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white; -fx-font-weight: bold;");
    logoutButton.setOnAction(e -> controller.logout()); // Delegate to controller

    toggleChartBtn.setStyle("-fx-background-color: #555; -fx-text-fill: white;");
    toggleHistogramTypeBtn.setStyle("-fx-background-color: #555; -fx-text-fill: white;");
    toggleHistogramTypeBtn.setVisible(false);

    ComboBox<String> exportSelectBox = new ComboBox<>();
    exportSelectBox.promptTextProperty().set("Export Format");

    Button exportButton = new Button();
    exportButton.setText("Export Graph");

    Button authoriseUsersButton = new Button("Authorise Users");
    authoriseUsersButton.setOnAction(e -> controller.openAuthoriseUsersPage());

    topBar.getChildren().addAll(title, logoutButton, toggleChartBtn, exportSelectBox, exportButton, toggleHistogramTypeBtn, authoriseUsersButton);
    topBar.setSpacing(20);


    if (role.equals("Admin")){
      authoriseUsersButton.setVisible(true);
    }else{
      authoriseUsersButton.setVisible(false);
    }

    // Left panel with campaign statistics
    VBox leftPanel = new VBox();
    leftPanel.setStyle("-fx-background-color: #e0e0e0;");
    leftPanel.setPrefSize(250.0, 334.0);

    HBox metricsPanel = new HBox();
    metricsPanel.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 10px");
    VBox metricsLabels = new VBox();
    VBox metricsValues = new VBox();
    VBox.setVgrow(metricsLabels, Priority.ALWAYS);
    metricsLabels.setAlignment(Pos.CENTER_LEFT);
    metricsValues.setAlignment(Pos.CENTER_RIGHT);
    metricsLabels.setFillWidth(true);
    metricsPanel.getChildren().addAll(metricsLabels, metricsValues);

    metricsLabels
            .getChildren()
            .addAll(
                    keyMetricsTitle,
                    impressionsLabel,
                    clicksLabel,
                    uniquesLabel,
                    conversionsLabel,
                    bounceRateLabel,
                    ctrLabel,
                    cpaLabel,
                    cpcLabel,
                    cpmLabel,
                    totalCostLabel);

    metricsValues
            .getChildren()
            .addAll(
                    keyMetricsValue,
                    impressionsValue,
                    clicksValue,
                    uniquesValue,
                    conversionsValue,
                    bounceRateValue,
                    ctrValue,
                    cpaValue,
                    cpcValue,
                    cpmValue,
                    totalCostValue);

    // Add tooltips
    setupTooltips();

    // Filters panel
    VBox filtersPanel = createFiltersPanel();
    leftPanel.getChildren().addAll(metricsPanel, filtersPanel);

    // Initialize chart components
    CategoryAxis xAxis = new CategoryAxis();
    xAxis.setLabel("Date");

    NumberAxis yAxis = new NumberAxis();
    yAxis.setLabel("Count");

    lineChart = new LineChart<>(xAxis, yAxis);
    lineChart.setTitle("Campaign Performance Over Time");
    lineChart.setAnimated(false);

    // Histogram panel setup
    chartContainer = new StackPane();
    histogramPanel = new ChartPanel(clickCostHistogram.createBlankHistogram());

    // Ensure SwingNode initializes properly
    Platform.runLater(() -> swingNode.setContent(histogramPanel));

    chartContainer.getChildren().addAll(lineChart, swingNode);
    swingNode.setVisible(false); // Hide Histogram Initially

    // Setup chart toggle functionality
    setupChartToggleButton();

    VBox centerPanel = new VBox(chartContainer);
    centerPanel.setPrefSize(600, 400);

    // Layout
    BorderPane root = new BorderPane();
    root.setTop(topBar);
    root.setLeft(leftPanel);
    root.setCenter(centerPanel);

    setupCloseHandler(primaryStage);

    Scene scene = new Scene(root, 900, 600);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  /**
   * Set up tooltips for the metrics labels
   */
  private void setupTooltips() {
    Tooltip impressionsTooltip = new Tooltip("Total number of times the ad was displayed.");
    Tooltip.install(impressionsLabel, impressionsTooltip);

    Tooltip clicksTooltip = new Tooltip("Total number of times the ad was clicked.");
    Tooltip.install(clicksLabel, clicksTooltip);

    Tooltip uniquesTooltip = new Tooltip("Unique users who viewed the ad.");
    Tooltip.install(uniquesLabel, uniquesTooltip);

    Tooltip conversionsTooltip = new Tooltip("Total successful actions taken after clicking the ad.");
    Tooltip.install(conversionsLabel, conversionsTooltip);

    Tooltip bounceRateTooltip = new Tooltip("Percentage of visitors who left without interaction.");
    Tooltip.install(bounceRateLabel, bounceRateTooltip);

    Tooltip ctrTooltip = new Tooltip("Click-through rate");
    Tooltip.install(ctrLabel, ctrTooltip);

    Tooltip cpaTooltip = new Tooltip("Cost per acquisition");
    Tooltip.install(cpaLabel, cpaTooltip);

    Tooltip cpcTooltip = new Tooltip("Cost per click");
    Tooltip.install(cpcLabel, cpcTooltip);

    Tooltip cpmTooltip = new Tooltip("Cost per thousand impressions");
    Tooltip.install(cpmLabel, cpmTooltip);

    Tooltip totalCostTooltip = new Tooltip("Total advertising spend.");
    Tooltip.install(totalCostLabel, totalCostTooltip);
  }

  /**
   * Create the filters panel
   */
  private VBox createFiltersPanel() {
    VBox filtersPanel = new VBox();
    filtersPanel.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 15px;");
    filtersPanel.setPrefSize(132, 500);

    // Filters title
    Label filterLabel = new Label("Filters");
    filterLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

    // Metric selection dropdown at top clearly
    Label metricLabel = new Label("Metric:");
    metricDropdown = new ComboBox<>();
    metricDropdown.getItems().addAll("Impressions", "Clicks", "Uniques", "Conversions", "Bounces");
    metricDropdown.setValue("Impressions");
    metricDropdown.setPrefWidth(190);

    VBox metricBox = new VBox(metricLabel, metricDropdown);
    metricBox.setAlignment(Pos.CENTER);

    HBox topfilterBox = new HBox();
    // Gender and Age ComboBoxes
    ComboBox<String> genderComboBox = new ComboBox<>();
    genderComboBox.getItems().addAll("Male","Female","All");
    genderComboBox.setPromptText("Gender");
    genderComboBox.setPrefSize(100, 26);

    ComboBox<String> ageComboBox = new ComboBox<>();
    ageComboBox.promptTextProperty().set("Age");
    ageComboBox.setPrefSize(100, 26);

    topfilterBox.getChildren().addAll(genderComboBox, ageComboBox);
    topfilterBox.setPadding(new Insets(5, 5, 0, 0));
    topfilterBox.setAlignment(Pos.CENTER);

    // Income and Context
    HBox bottomfilterBox = new HBox();

    // Income and Context ComboBoxes
    ComboBox<String> incomeComboBox = new ComboBox<>();
    incomeComboBox.setPromptText("Income");
    incomeComboBox.setPrefSize(100, 26);

    ComboBox<String> contextComboBox = new ComboBox<>();
    contextComboBox.promptTextProperty().set("Context");
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
    DatePicker datePicker1 = new DatePicker();
    datePicker1.setPrefSize(200.0, 35.0);
    Label toLabel = new Label("to");
    DatePicker datePicker2 = new DatePicker();
    datePicker2.setPrefSize(200.0, 35.0);
    datePickerBox.getChildren().addAll(datePicker1, toLabel, datePicker2);
    datePickerBox.setPadding(new Insets(5, 0, 15, 0));

    // keeping the apply Filters button at bottom
    Button applyButton = new Button("Apply filters");
    applyButton.setPrefSize(133, 26);
    applyButton.setOnAction(e -> {
      Campaign selectedCampaign = getSelectedCampaign();
      if (selectedCampaign != null) {
        String bounceType = pageleftBounceToggle.isSelected() ? "PageLeft" : "SinglePage";
        String selectedMetric = metricDropdown.getValue();
        String selectedGender = genderComboBox.getValue();
        controller.generateGraph(selectedCampaign.getName(), bounceType, selectedMetric,selectedGender);
        controller.updateBounceRate(selectedCampaign.getName(), bounceType);
      }
    });

    // Adding  content to panel
    filtersPanel.getChildren().addAll( metricBox,
            filterLabel, topfilterBox, bottomfilterBox,
            timeGranularityLabel, timeGranularityToggleBox,
            bounceDefinitionLabel, bounceDefinitionBox, datePickerLabel,
            datePickerBox, applyButton);
    filtersPanel.setAlignment(Pos.CENTER);

    return filtersPanel;
  }

  private Campaign getSelectedCampaign() {
    if (campaignMenuButton != null && controller.getCampaigns().size() > 0) {
      for (Campaign campaign : controller.getCampaigns()) {
        if (campaign.getName().equals(campaignMenuButton.getText())) {
          return campaign;

        }
      }    metricDropdown.setValue("Impressions");

    }
    return null;
  }

  /**
   * Configure the chart toggle button behavior
   */
  private void setupChartToggleButton() {
    toggleChartBtn.setOnAction(e -> {
      Campaign selectedCampaign = getSelectedCampaign();
      if (selectedCampaign == null) return;

      if (lineChart.isVisible()) {
        controller.updateHistogram(selectedCampaign.getName(), isClickByCost);
        lineChart.setVisible(false);
        swingNode.setVisible(true);
        toggleChartBtn.setText("Switch to Performance Chart");
        toggleHistogramTypeBtn.setVisible(true);
        toggleHistogramTypeBtn.setText("Clicks by Time");
        isClickByCost = true;
      } else {
        swingNode.setVisible(false);
        lineChart.setVisible(true);
        toggleChartBtn.setText("Switch to Histogram");
        toggleHistogramTypeBtn.setVisible(false);
        toggleHistogramTypeBtn.setText("Clicks by Time");
        isClickByCost = true;
      }
    });

    toggleHistogramTypeBtn.setOnAction(e -> {
      isClickByCost = !isClickByCost;
      toggleHistogramTypeBtn.setText(isClickByCost ? "Clicks by Time" : "Clicks by Cost");

      Campaign selectedCampaign = getSelectedCampaign();
      if (selectedCampaign != null) {
        controller.updateHistogram(selectedCampaign.getName(), isClickByCost);
      }
    });
  }

  /**
   * Show alert dialog for various error cases
   */
  public static void showAlert(File file, String type) {
    Alert alert = new Alert(Alert.AlertType.ERROR);

    if (type.equals("Files")) {
      alert.setTitle("Error");
      alert.setHeaderText("Wrong files");
      alert.setContentText("One or more files are either not entered or in the correct format");
    } else if (type.equals("campaignname")) {
      alert.setTitle("Error");
      alert.setHeaderText("Campaign name wrong");
      alert.setContentText("Campaign name already exists");
    }else if (type.equals("userpwdempty")){
      alert.setTitle("Error");
      alert.setHeaderText("Username or password invalid");
      alert.setContentText("The username and password field cannot be empty");
    } else if (type.equals("usernotexist")){
      alert.setTitle("Error");
      alert.setHeaderText("Username or password invalid");
      alert.setContentText("The username and password combination does not exist");
    }else if (type.equals("notauthorised")){
      alert.setTitle("Error");
      alert.setHeaderText("Not authorised");
      alert.setContentText("Your account must be authorised by the admin");
    } else if (type.equals("usernamealreadyexists")){
      alert.setTitle("Error");
      alert.setHeaderText("Username already exists");
      alert.setContentText("This username is already registered");
    }else if(type.equals("passwordwrong")){
      alert.setTitle("Error");
      alert.setHeaderText("Password invalid");
      alert.setContentText("Password is incorrect");
    } else {
      alert.setTitle("Error");
      alert.setHeaderText("Wrong format");
      alert.setContentText(file.getName() + " has wrong format for " + type + " log file");
    }

    alert.showAndWait();
  }

  public void changeSelectedCampaign(Campaign campaign){
    campaignMenuButton.setText(campaign.getName());
    metricDropdown.setValue("Impressions");

  }

  /**
   * Update the campaign menu with available campaigns
   */
  public void updateCampaignMenu(Label titleLabel, HBox topBar) {
    if (campaignMenuButton == null) {
      campaignMenuButton = new MenuButton("Select Campaign");
    }

    campaignMenuButton.getItems().clear();

    MenuItem selectcampaignItem = new MenuItem("Select Campaign");
    selectcampaignItem.setStyle("-fx-text-fill: grey;");
    campaignMenuButton.getItems().add(selectcampaignItem);

    // Add the campaign names to the menu
    for (Campaign campaignselected : controller.getCampaigns()) {
      MenuItem item = new MenuItem(campaignselected.getName());
      item.setOnAction(e -> {
        controller.selectCampaign(campaignselected);
        changeSelectedCampaign(campaignselected);
      });
      campaignMenuButton.getItems().add(item);
    }

    // Add the "Add Campaign" item
    MenuItem addcampaignItem = new MenuItem("Add Campaign");
    addcampaignItem.setOnAction(e -> controller.openAddCampaignDialog(titleLabel, topBar));
    campaignMenuButton.getItems().add(addcampaignItem);

    // Replace the titleLabel with the campaignMenuButton
    int index = topBar.getChildren().indexOf(titleLabel);
    if (index != -1) {
      topBar.getChildren().set(index, campaignMenuButton);
    }

  }

  /**
   * Update metrics display with new values
   */
  public void updateMetricsDisplay(Map<String, Double> coreMetrics, double bounceRate,
                                   double ctr, double cpa, double cpc, double cpm, double totalCost) {
    impressionsValue.setText(String.format("%,.0f", coreMetrics.getOrDefault("Impressions", 0.0)));
    clicksValue.setText(String.format("%,.0f", coreMetrics.getOrDefault("Clicks", 0.0)));
    uniquesValue.setText(String.format("%,.0f", coreMetrics.getOrDefault("Uniques", 0.0)));
    conversionsValue.setText(String.format("%,.0f", coreMetrics.getOrDefault("Conversions", 0.0)));
    bounceRateValue.setText(String.format("%,.2f%%", bounceRate * 100));
    ctrValue.setText(String.format("%,.2f%%", ctr));
    cpaValue.setText(String.format("%,.2f", cpa));
    cpcValue.setText(String.format("%,.2f", cpc));
    cpmValue.setText(String.format("%,.2f", cpm));
    totalCostValue.setText(String.format("£" + "%,.2f", totalCost));

    isDataDownloaded = true;
  }

  /**
   * Update only the bounce rate display
   */
  public void updateBounceRateDisplay(double bounceRate) {
    bounceRateValue.setText(String.format("%,.2f%%", bounceRate * 100));
  }

  /**
   * Update the performance graph with new data
   */
  public void updatePerformanceGraph(Map<String, Map<String, Integer>> metricsOverTime, String selectedMetric) {
    // Clear existing chart data

    if (!firstGraphGeneration){
      lineChart.setAnimated(true);
    }
    lineChart.getData().clear();

    if (!metricsOverTime.containsKey(selectedMetric)) {
      System.out.println("Metric not found: " + selectedMetric);
      return;
    }

    addSeriesToChart(metricsOverTime.get(selectedMetric), selectedMetric, 1.0);
    firstGraphGeneration = false;
  }

  /**
   * Helper to add series to the line chart
   */
  private void addSeriesToChart(Map<String, Integer> dataMap, String metricName, double scaleFactor) {
      XYChart.Series<String, Number> series = new XYChart.Series<>();
      series.setName(metricName);



    List<String> sortedDates = new ArrayList<>(dataMap.keySet());
    sortedDates.sort(Comparator.naturalOrder());

    for (String date : sortedDates) {
      double yValue = dataMap.get(date) * scaleFactor;
      XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(date, yValue);

      // Add tooltip showing raw and scaled values
      Tooltip tooltip = new Tooltip(metricName + "\nDate: " + date +
          "\nRaw: " + dataMap.get(date) + "\nScaled: " + yValue);
      Platform.runLater(() -> Tooltip.install(dataPoint.getNode(), tooltip));

      series.getData().add(dataPoint);
    }


    Platform.runLater(() -> lineChart.getData().add(series));
  }


  /**
   * Update the histogram for click costs
   */
  public void updateClickCostHistogram(List<Double> clickCosts) {
    ChartPanel newHistogramPanel = new ChartPanel(new ClickCostHistogram(clickCosts).createHistogram());
    Platform.runLater(() -> swingNode.setContent(newHistogramPanel));
  }

  /**
   * Update the histogram for click times
   */
  public void updateClickTimeHistogram(Map<String, Integer> clicksByDate) {
    ChartPanel newHistogramPanel = new ChartPanel(new ClickCostHistogram(clicksByDate).createHistogram());
    Platform.runLater(() -> swingNode.setContent(newHistogramPanel));
  }

  // Add this to your main application class where you set up the primary stage
  private void setupCloseHandler(Stage primaryStage) {
    primaryStage.setOnCloseRequest(event -> {
      controller.closeAppActions();
    });
  }

}