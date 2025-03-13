package org.example.Views;

import java.util.*;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

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
  private LineChart<Number, Number> lineChart;
  private StackPane chartContainer;
  private ChartPanel histogramPanel;
  private ClickCostHistogram clickCostHistogram = new ClickCostHistogram();
  private boolean isClickByCost = true; // Track histogram type (true = Clicks by Cost, false = Clicks by Time)
  private boolean isDataDownloaded = false; // Track if data is available for histograms
  private UIController controller; // Reference to the controller

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
  public void show() {
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

    topBar.getChildren().addAll(title, logoutButton, toggleChartBtn, exportSelectBox, exportButton, toggleHistogramTypeBtn);
    topBar.setSpacing(20);

    // Left panel with campaign statistics
    VBox leftPanel = new VBox();
    leftPanel.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 15px;");
    leftPanel.setPrefSize(250.0, 334.0);

    HBox metricsPanel = new HBox();
    metricsPanel.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 15px;");
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
    NumberAxis xAxis = new NumberAxis();
    xAxis.setLabel("Time (Days)");
    NumberAxis yAxis = new NumberAxis();
    yAxis.setLabel("Count (Impressions, Clicks, Uniques, Conversions)");

    lineChart = new LineChart<>(xAxis, yAxis);
    lineChart.setTitle("Campaign Performance Over Time");

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

    // Label
    Label filterLabel = new Label("Filters");

    // Gender and Age
    HBox topfilterBox = new HBox();
    ComboBox<String> genderComboBox = new ComboBox<>();
    genderComboBox.promptTextProperty().set("Gender");
    genderComboBox.setPrefSize(100, 26);
    ComboBox<String> ageComboBox = new ComboBox<>();
    ageComboBox.promptTextProperty().set("Age");
    ageComboBox.setPrefSize(100, 26);
    topfilterBox.getChildren().addAll(genderComboBox, ageComboBox);
    topfilterBox.setPadding(new Insets(5, 5, 0, 0));
    topfilterBox.setAlignment(Pos.CENTER);

    // Income and Context
    HBox bottomfilterBox = new HBox();
    ComboBox<String> incomeComboBox = new ComboBox<>();
    incomeComboBox.promptTextProperty().set("Income");
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
    ToggleButton hourToggle = new ToggleButton("Hour");
    ToggleButton dayToggle = new ToggleButton("Day");
    ToggleButton weekToggle = new ToggleButton("Week");
    timeGranularityToggleBox.getChildren().addAll(hourToggle, dayToggle, weekToggle);
    timeGranularityToggleBox.setPadding(new Insets(5, 5, 0, 0));
    timeGranularityToggleBox.setAlignment(Pos.CENTER);

    ToggleGroup timeGranularityGroup = new ToggleGroup();
    hourToggle.setToggleGroup(timeGranularityGroup);
    dayToggle.setToggleGroup(timeGranularityGroup);
    weekToggle.setToggleGroup(timeGranularityGroup);

    // Bounce Definition
    Label bounceDefinitionLabel = new Label("Bounce Definition");
    bounceDefinitionLabel.setPadding(new Insets(5, 5, 0, 0));

    HBox bounceDefinitionBox = new HBox();
    bounceDefinitionBox.getChildren().addAll(pageleftBounceToggle, singlePageBounceToggle);
    bounceDefinitionBox.setPadding(new Insets(5, 5, 0, 0));
    bounceDefinitionBox.setAlignment(Pos.CENTER);

    ToggleGroup bounceDefinitionGroup = new ToggleGroup();
    pageleftBounceToggle.setToggleGroup(bounceDefinitionGroup);
    singlePageBounceToggle.setToggleGroup(bounceDefinitionGroup);

    // Date pickers
    HBox datePickerBox = new HBox();
    DatePicker datePicker1 = new DatePicker();
    datePicker1.setPrefSize(200.0, 35.0);
    Label toLabel = new Label("to");
    DatePicker datePicker2 = new DatePicker();
    datePicker2.setPrefSize(200.0, 35.0);
    datePickerBox.getChildren().addAll(datePicker1, toLabel, datePicker2);
    datePickerBox.setPadding(new Insets(5, 0, 50, 0));
    datePickerBox.setAlignment(Pos.CENTER);

    // Apply filters button
    Button applyButton = new Button("Apply filters");
    applyButton.setPrefSize(133, 26);
    applyButton.setOnAction(e -> {
      Campaign selectedCampaign = getSelectedCampaign();
      if (selectedCampaign != null) {
        String bounceType = pageleftBounceToggle.isSelected() ? "PageLeft" : "SinglePage";
        controller.generateGraph(selectedCampaign.getName(), bounceType);
        controller.updateBounceRate(selectedCampaign.getName(), bounceType);
      }
    });

    filtersPanel.getChildren().addAll(
            filterLabel, topfilterBox, bottomfilterBox,
            timeGranularityLabel, timeGranularityToggleBox,
            bounceDefinitionLabel, bounceDefinitionBox,
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
      }
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
    } else {
      alert.setTitle("Error");
      alert.setHeaderText("Wrong format");
      alert.setContentText(file.getName() + " has wrong format for " + type + " log file");
    }

    alert.showAndWait();
  }

  public void changeSelectedCampaign(Campaign campaign){
    campaignMenuButton.setText(campaign.getName());
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
  public void updatePerformanceGraph(Map<String, Map<String, Integer>> metricsOverTime) {
    // Clear existing chart data
    lineChart.getData().clear();

    // Find max values for each metric
    int maxImpressions = metricsOverTime.get("Impressions").values().stream().max(Integer::compare).orElse(1);
    int maxClicks = metricsOverTime.get("Clicks").values().stream().max(Integer::compare).orElse(1);
    int maxUniques = metricsOverTime.get("Uniques").values().stream().max(Integer::compare).orElse(1);
    int maxConversions = metricsOverTime.get("Conversions").values().stream().max(Integer::compare).orElse(1);
    int maxBounces = metricsOverTime.get("Bounces").values().stream().max(Integer::compare).orElse(1);

    // Store all metrics in a map and sort them
    Map<String, Integer> metricValues = new HashMap<>();
    metricValues.put("Impressions", maxImpressions);
    metricValues.put("Clicks", maxClicks);
    metricValues.put("Uniques", maxUniques);
    metricValues.put("Conversions", maxConversions);
    metricValues.put("Bounces", maxBounces);

    List<Map.Entry<String, Integer>> sortedMetrics = new ArrayList<>(metricValues.entrySet());
    sortedMetrics.sort(Map.Entry.comparingByValue()); // Sort by value (ascending)

    // Assign scaling factors based on ranking
    String smallestMetric = sortedMetrics.get(0).getKey();
    String secondSmallest = sortedMetrics.get(1).getKey();
    String middleMetric = sortedMetrics.get(2).getKey();
    String secondLargest = sortedMetrics.get(3).getKey();
    String largestMetric = sortedMetrics.get(4).getKey(); // The largest one is the baseline

    Map<String, Double> scaleFactors = new HashMap<>();
    scaleFactors.put(largestMetric, 1.0); // Largest stays the same
    scaleFactors.put(secondLargest, 50.0);
    scaleFactors.put(middleMetric, 75.0);
    scaleFactors.put(secondSmallest, 100.0);
    scaleFactors.put(smallestMetric, 150.0);

    // Add series to the chart
    addSeriesToChart(metricsOverTime.get("Impressions"), "Impressions (x" + scaleFactors.get("Impressions") + ")", scaleFactors.get("Impressions"));
    addSeriesToChart(metricsOverTime.get("Clicks"), "Clicks (x" + scaleFactors.get("Clicks") + ")", scaleFactors.get("Clicks"));
    addSeriesToChart(metricsOverTime.get("Uniques"), "Uniques (x" + scaleFactors.get("Uniques") + ")", scaleFactors.get("Uniques"));
    addSeriesToChart(metricsOverTime.get("Conversions"), "Conversions (x" + scaleFactors.get("Conversions") + ")", scaleFactors.get("Conversions"));
    addSeriesToChart(metricsOverTime.get("Bounces"), "Bounces (x" + scaleFactors.get("Bounces") + ")", scaleFactors.get("Bounces"));
  }

  /**
   * Helper to add series to the line chart
   */
  private void addSeriesToChart(Map<String, Integer> dataMap, String name, double scaleFactor) {
    XYChart.Series<Number, Number> series = new XYChart.Series<>();
    series.setName(name);

    List<String> sortedDates = new ArrayList<>(dataMap.keySet());
    sortedDates.sort(Comparator.naturalOrder());

    Map<String, Integer> dateIndexMap = new HashMap<>();
    for (int i = 0; i < sortedDates.size(); i++) {
      dateIndexMap.put(sortedDates.get(i), i);
    }

    for (Map.Entry<String, Integer> entry : dataMap.entrySet()) {
      int xValue = dateIndexMap.get(entry.getKey());
      double yValue = entry.getValue() * scaleFactor;

      XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(xValue, yValue);

      // Add tooltip showing raw and scaled values
      Tooltip tooltip = new Tooltip(name + "\nDate: " + entry.getKey() +
              "\nRaw: " + entry.getValue() + "\nScaled: " + yValue);

      // Use Platform.runLater to avoid JavaFX threading issues
      final XYChart.Data<Number, Number> finalDataPoint = dataPoint;
      Platform.runLater(() -> Tooltip.install(finalDataPoint.getNode(), tooltip));

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
}