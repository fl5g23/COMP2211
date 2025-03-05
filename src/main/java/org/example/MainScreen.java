
package org.example;

import java.util.Comparator;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartPanel;
import javafx.embed.swing.SwingNode;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;

/**
 * Mohammad: lines 110-117 add a new blank histogram to the screen, see function updateStats for me trying to add the one with data
 */
public class MainScreen {

  private final Stage primaryStage;
  private StatsCalculator statsCalculator = new StatsCalculator();
  private ClickCostHistogram clickCostHistogram = new ClickCostHistogram();
  private StackPane chartContainer;
  private ChartPanel histogramPanel;

  // Instance variables to store selected files
  private File impressionLogFile;
  private File clicksLogFile;
  private File serverLogFile;
  private MenuButton campaignMenuButton; // Replaces the title label after a campaign is added
  private List<Campaign> campaigns = new ArrayList<>(); // Stores campaign names
  private SwingNode swingNode = new SwingNode();
  private LineChart<Number, Number> lineChart;

  // Core metric labels
  Label impressionsLabel = new Label("Number of Impressions: ");
  Label clicksLabel = new Label("Clicks on Advertisement: ");
  Label uniquesLabel = new Label("Unique Clicks on Advertisement: ");
  Label conversionsLabel = new Label("Conversions: ");
  Label bounceRateLabel = new Label("Bounce Rate: ");
  Label ctrLabel = new Label("CTR - Click Through Rate: ");
  Label cpaLabel = new Label("CPA - Cost Per Acquisition: ");
  Label cpcLabel = new Label("CPC - Cost Per Click: ");
  Label cpmLabel = new Label("CPM - Cost Per Thousand Impressions: ");
  Label totalCostLabel = new Label("Total Cost: ");
  Button toggleChartBtn = new Button("Switch to Histogram");

  public MainScreen(Stage stage) {
    this.primaryStage = stage;
  }

  public void show() {
    primaryStage.setTitle("Ad Campaign Dashboard");

    // Top bar with title and Add Campaign button
    HBox topBar = new HBox();
    topBar.setStyle("-fx-background-color: #ddd; -fx-padding: 10px;");

    // Initially set a Label (will be replaced by MenuButton)
    Label title = new Label("Add Campaign");
    title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #328ccd;");
    title.setOnMouseClicked(e -> openAddCampaignDialog(title, topBar)); // Pass Label and TopBar

    toggleChartBtn.setStyle("-fx-background-color: #555; -fx-text-fill: white;");

    topBar.getChildren().addAll(title, toggleChartBtn);
    topBar.setSpacing(20);

    // Left panel with campaign statistics
    VBox leftPanel = new VBox();
    leftPanel.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 15px;");
    leftPanel.setSpacing(10);

    leftPanel
        .getChildren()
        .addAll(
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

    // Use CategoryAxis for X-Axis (since X values are String-based dates)
    NumberAxis xAxis = new NumberAxis();
    xAxis.setLabel("Time (Days)"); // Updated X-axis label
    // Use NumberAxis for Y-Axis (since Y values are numeric counts)
    NumberAxis yAxis = new NumberAxis();
    yAxis.setLabel("Count (Impressions, Clicks, Uniques, Conversions)");

    // Explicitly tell JavaFX that this is a LineChart<String, Number>
    lineChart = new LineChart<>(xAxis, yAxis);
    lineChart.setTitle("Campaign Performance Over Time");

    // Histogram panel (JFreeChart)
    histogramPanel = new ChartPanel(clickCostHistogram.createBlankHistogram());

    // StackPane (To Toggle Between Line Chart and Histogram)
    chartContainer = new StackPane();

    // Ensure SwingNode initializes properly
    Platform.runLater(() -> swingNode.setContent(histogramPanel));

    chartContainer.getChildren().addAll(lineChart, swingNode);
    swingNode.setVisible(false); // Hide Histogram Initially

    VBox centerPanel = new VBox(chartContainer);
    centerPanel.setPrefSize(600, 400);

    toggleChartBtn.setOnAction(
        e -> {
          if (lineChart.isVisible()) {
            lineChart.setVisible(false);
            Platform.runLater(
                () -> swingNode.setContent(histogramPanel)); // Ensure correct embedding
            swingNode.setVisible(true);
            toggleChartBtn.setText("Switch to Performance Chart");
          } else {
            swingNode.setVisible(false);
            lineChart.setVisible(true);
            toggleChartBtn.setText("Switch to Histogram");
          }
        });

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
   * Logic for opening new dialog when "Add Campaign" Label is pressed
   */
  public void openAddCampaignDialog(Label titleLabel, HBox rootContainer) {
    Stage dialogStage = new Stage();
    dialogStage.setTitle("Add Campaign");
    dialogStage.initOwner(primaryStage);
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
    impressionLogButton.setOnAction(
        e -> {
          File selectedFile = openFileFinder();
          if (selectedFile != null) {
            impressionLogFile = selectedFile;
            impressionLogButton.setText(selectedFile.getName());
          }
        });

    Button clicksLogButton = new Button("Click Log");
    clicksLogButton.setLayoutX(14);
    clicksLogButton.setLayoutY(100);
    clicksLogButton.setPrefSize(124, 26);
    clicksLogButton.setOnAction(
        e -> {
          File selectedFile = openFileFinder();
          if (selectedFile != null) {
            clicksLogFile = selectedFile;
            clicksLogButton.setText(selectedFile.getName());
          }
        });

    Button serverLogButton = new Button("Server Log");
    serverLogButton.setLayoutX(14);
    serverLogButton.setLayoutY(133);
    serverLogButton.setPrefSize(124, 26);
    serverLogButton.setOnAction(
        e -> {
          File selectedFile = openFileFinder();
          if (selectedFile != null) {
            serverLogFile = selectedFile;
            serverLogButton.setText(selectedFile.getName());
          }
        });

    Button saveButton = new Button("Save");
    saveButton.setLayoutX(211);
    saveButton.setLayoutY(187);
    saveButton.setPrefSize(55, 26);
    saveButton.setOnAction(
        e -> {
          String campaignName = campaignNameField.getText().trim();
          if (!campaignName.isEmpty()) {
            Campaign newCampaign =
                new Campaign(
                    campaignName,
                    impressionLogFile.getAbsoluteFile(),
                    clicksLogFile.getAbsoluteFile(),
                    serverLogFile.getAbsoluteFile());
            campaigns.add(newCampaign); // Store campaign name
            updateCampaignMenu(titleLabel, rootContainer); // Replace label with MenuButton
            itemSelected(newCampaign);
            dialogStage.close();
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
   * File finder interface
   */
  public File openFileFinder() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
    return fileChooser.showOpenDialog(primaryStage);
  }

  /**
   * Updates the menubox with new campaign
   */
  private void updateCampaignMenu(Label titleLabel, HBox topBar) {
    if (campaignMenuButton == null) {
      campaignMenuButton = new MenuButton("Select Campaign");
    }

    campaignMenuButton.getItems().clear();
    MenuItem selectcampaignItem = new MenuItem("Select Campaign");
    selectcampaignItem.setStyle("-fx-text-fill: grey;");
    campaignMenuButton.getItems().add(selectcampaignItem);

    // Add the campaign names to the menu
    for (Campaign campaignselected : campaigns) {
      MenuItem item = new MenuItem(campaignselected.getName());
      item.setOnAction(e -> itemSelected(campaignselected));
      campaignMenuButton.getItems().add(item);
    }

    // Add the "Add Campaign" item
    MenuItem addcampaignItem = new MenuItem("Add Campaign");
    addcampaignItem.setOnAction(
        e -> openAddCampaignDialog(titleLabel, topBar)); // Opens the dialog again
    campaignMenuButton.getItems().add(addcampaignItem);

    // Replace the titleLabel with the campaignMenuButton
    int index = topBar.getChildren().indexOf(titleLabel);
    if (index != -1) {
      topBar.getChildren().set(index, campaignMenuButton);
    }
  }

  /**
   * Invoked when campaign in selected in the menubox
   */
  private void itemSelected(Campaign campaign) {
    campaignMenuButton.setText(campaign.getName());
    if (!(statsCalculator.isCampaignExists(campaign))) {
      System.out.println("Campaign does not exist");
      statsCalculator.setup(
          campaign,
          campaign.getImpressionLogFile(),
          campaign.getClicksLogFile(),
          campaign.getServerLogFile());
    }
    updateStats(campaign.getName());
  }

  /**
   * Updates the labels when new campaign is selected
   * Mohammad: lines 297-300 try and make a new Chart and replace the blank one, but doesn't show
   * I edited the clickcosthistogram class to generate a new class
   */
  private void updateStats(String campaignName) {
    // Update statistics

    List<Double> costslist = statsCalculator.getCostsList(campaignName);
    double bounceRate =
        statsCalculator.calculateBounceRate(campaignName).getOrDefault("Single Rate", 0.0);
    double ctr = statsCalculator.calculateCTR(campaignName);
    double cpa = statsCalculator.calculateCPA(campaignName);
    double cpc = statsCalculator.calculateCPC(campaignName);
    double cpm = statsCalculator.calculateCPM(campaignName);
    double totalCost = statsCalculator.calculateTotalCost(campaignName);

    ChartPanel newHistogramPanel = new ChartPanel(clickCostHistogram.createHistogram(costslist));
    int indexofhistogram = chartContainer.getChildren().indexOf(swingNode);
    Platform.runLater(() -> swingNode.setContent(newHistogramPanel));
    chartContainer.getChildren().set(indexofhistogram, swingNode);
    Map<String, Double> coreMetrics = statsCalculator.getCoreMetrics(campaignName);
    toggleChartBtn.setOnAction(
        e -> {
          if (lineChart.isVisible()) {
            lineChart.setVisible(false);
            Platform.runLater(
                () -> swingNode.setContent(newHistogramPanel)); // Ensure correct embedding
            swingNode.setVisible(true);
            toggleChartBtn.setText("Switch to Performance Chart");
          } else {
            swingNode.setVisible(false);
            lineChart.setVisible(true);
            toggleChartBtn.setText("Switch to Histogram");
          }
        });

    impressionsLabel.setText(
        "Number of Impressions: " + coreMetrics.getOrDefault("Impressions", 0.0));
    clicksLabel.setText("Clicks on Advertisement: " + coreMetrics.getOrDefault("Clicks", 0.0));
    uniquesLabel.setText(
        "Unique Clicks on Advertisement: " + coreMetrics.getOrDefault("Uniques", 0.0));
    conversionsLabel.setText("Conversions: " + coreMetrics.getOrDefault("Conversions", 0.0));
    bounceRateLabel.setText("Bounce Rate: " + String.format("%.2f%%", bounceRate * 100));
    ctrLabel.setText("CTR - Click Through Rate: " + String.format("%.2f%%", ctr));
    cpaLabel.setText("CPA - Cost Per Acquisition: £" + String.format("%.2f", cpa));
    cpcLabel.setText("CPC - Cost Per Click: £" + String.format("%.2f", cpc));
    cpmLabel.setText("CPM - Cost Per Thousand Impressions: £" + String.format("%.2f", cpm));
    totalCostLabel.setText("Total Cost: £" + String.format("%.2f", totalCost));

    //real graph implementation
     Map<String, Map<String, Integer>> metricsOverTime = statsCalculator.getMetricsOverTime(campaignName);

      // Clear existing chart data
      lineChart.getData().clear();

      // Find max values for each metric
      int maxImpressions = metricsOverTime.get("Impressions").values().stream().max(Integer::compare).orElse(1);
      int maxClicks = metricsOverTime.get("Clicks").values().stream().max(Integer::compare).orElse(1);
      int maxUniques = metricsOverTime.get("Uniques").values().stream().max(Integer::compare).orElse(1);
      int maxConversions = metricsOverTime.get("Conversions").values().stream().max(Integer::compare).orElse(1);

      // Store all metrics in a list and sort them
      Map<String, Integer> metricValues = new HashMap<>();
      metricValues.put("Impressions", maxImpressions);
      metricValues.put("Clicks", maxClicks);
      metricValues.put("Uniques", maxUniques);
      metricValues.put("Conversions", maxConversions);

      List<Map.Entry<String, Integer>> sortedMetrics = new ArrayList<>(metricValues.entrySet());
      sortedMetrics.sort(Map.Entry.comparingByValue()); // Sort by value (ascending)

      // Assign scaling factors based on ranking
      String smallestMetric = sortedMetrics.get(0).getKey();
      String secondSmallest = sortedMetrics.get(1).getKey();
      String secondLargest = sortedMetrics.get(2).getKey();
      String largestMetric = sortedMetrics.get(3).getKey(); // The largest one is the baseline

      Map<String, Double> scaleFactors = new HashMap<>();
      scaleFactors.put(largestMetric, 1.0); // Largest stays the same
      scaleFactors.put(secondLargest, 50.0 );
      scaleFactors.put(secondSmallest, 100.0 );
      scaleFactors.put(smallestMetric, 150.0);

      // Debug print to verify scaling
      System.out.println("Scaling Factors: " + scaleFactors);

      // Apply scaling dynamically
      addSeriesToChart(metricsOverTime.get("Impressions"), "Impressions (x" + scaleFactors.get("Impressions") + ")", scaleFactors.get("Impressions"));
      addSeriesToChart(metricsOverTime.get("Clicks"), "Clicks (x" + scaleFactors.get("Clicks") + ")", scaleFactors.get("Clicks"));
      addSeriesToChart(metricsOverTime.get("Uniques"), "Uniques (x" + scaleFactors.get("Uniques") + ")", scaleFactors.get("Uniques"));
      addSeriesToChart(metricsOverTime.get("Conversions"), "Conversions (x" + scaleFactors.get("Conversions") + ")", scaleFactors.get("Conversions"));
    }



  // helper function to plot data
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
      Tooltip tooltip = new Tooltip(name + "\nDate: " + entry.getKey() + "\nRaw: " + entry.getValue() + "\nScaled: " + yValue);
      Tooltip.install(dataPoint.getNode(), tooltip);

      series.getData().add(dataPoint);
    }

    Platform.runLater(() -> lineChart.getData().add(series));
  }


}