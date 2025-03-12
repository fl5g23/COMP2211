
package org.example;

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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;

import org.jfree.chart.ChartPanel;
import javafx.embed.swing.SwingNode;


public class MainScreen {

  private final Stage primaryStage;
  private StatsCalculator statsCalculator = new StatsCalculator();
  private ClickCostHistogram clickCostHistogram=new ClickCostHistogram();
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
  private Campaign currentCampaign = new Campaign("", new File(""),new File(""),new File(""));

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

  Boolean impression_log_flag = false;
  Boolean click_log_flag = false;
  Boolean server_log_flag = false;

  private boolean isClickByCost = true; // Track histogram type (true = Clicks by Cost, false = Clicks by Time)
  private ToggleButton toggleHistogramTypeBtn = new ToggleButton("Clicks by time");
  private boolean isDataDownloaded = false; // Track histogram type (true = Clicks by Cost, false = Clicks by Time)



  public MainScreen(Stage stage) {
    this.primaryStage = stage;
    keyMetricsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bolder; -fx-underline: true;");
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
    Button logoutButton = new Button("Logout");
    logoutButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white; -fx-font-weight: bold;");
    logoutButton.setOnAction(e -> logout());

    toggleChartBtn.setStyle("-fx-background-color: #555; -fx-text-fill: white;");
    toggleHistogramTypeBtn.setStyle("-fx-background-color: #555; -fx-text-fill: white;");
    toggleHistogramTypeBtn.setVisible(false);

    ComboBox exportSelectBox = new ComboBox();
    exportSelectBox.promptTextProperty().set("Export Format");

    Button exportButton = new Button();
    exportButton.setText("Export Graph");

    topBar.getChildren().addAll(title,logoutButton, toggleChartBtn, exportSelectBox, exportButton,toggleHistogramTypeBtn);
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
    metricsPanel.getChildren().addAll(metricsLabels,metricsValues);

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





    VBox filtersPanel = new VBox();
    filtersPanel.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 15px;");
    filtersPanel.setPrefSize(132, 500);


// Label
    Label filterLabel = new Label("Filters");


// ComboBox Rows
    HBox topfilterBox = new HBox();
    ComboBox<String> genderComboBox = new ComboBox<>();
    genderComboBox.promptTextProperty().set("Gender");
    genderComboBox.setPrefSize(100, 26);
    ComboBox<String> ageComboBox = new ComboBox<>();
    ageComboBox.promptTextProperty().set("Age");
    ageComboBox.setPrefSize(100, 26);
    topfilterBox.getChildren().addAll(genderComboBox, ageComboBox);
    topfilterBox.setPadding(new Insets(5,5,0,0));
    topfilterBox.setAlignment(Pos.CENTER);

    HBox bottomfilterBox = new HBox();
    ComboBox<String> incomeComboBox = new ComboBox<>();
    incomeComboBox.promptTextProperty().set("Income");
    incomeComboBox.setPrefSize(100, 26);
    ComboBox<String> contextComboBox = new ComboBox<>();
    contextComboBox.promptTextProperty().set("Context");
    contextComboBox.setPrefSize(100, 26);
    bottomfilterBox.getChildren().addAll(incomeComboBox, contextComboBox);
    bottomfilterBox.setPadding(new Insets(5,5,0,0));
    bottomfilterBox.setAlignment(Pos.CENTER);

    Label timeGranularityLabel = new Label("Time Granularity");
    timeGranularityLabel.setPadding(new Insets(5,5,0,0));

    HBox timeGranularityToggleBox = new HBox();
    ToggleButton hourToggle = new ToggleButton("Hour");
    ToggleButton dayToggle = new ToggleButton("Day");
    ToggleButton weekToggle = new ToggleButton("Week");
    timeGranularityToggleBox.getChildren().addAll(hourToggle,dayToggle,weekToggle);
    timeGranularityToggleBox.setPadding(new Insets(5,5,0,0));
    timeGranularityToggleBox.setAlignment(Pos.CENTER);

    ToggleGroup timeGranularityGroup = new ToggleGroup();

    hourToggle.setToggleGroup(timeGranularityGroup);
    dayToggle.setToggleGroup(timeGranularityGroup);
    weekToggle.setToggleGroup(timeGranularityGroup);


    Label bounceDefinitionLabel = new Label("Bounce Definition");
    bounceDefinitionLabel.setPadding(new Insets(5,5,0,0));

    HBox bounceDefinitionBox = new HBox();
    pageleftBounceToggle = new ToggleButton("Page Left");
    singlePageBounceToggle = new ToggleButton("Single Page");
    bounceDefinitionBox.getChildren().addAll(pageleftBounceToggle, singlePageBounceToggle);
    bounceDefinitionBox.setPadding(new Insets(5,5,0,0));
    bounceDefinitionBox.setAlignment(Pos.CENTER);

    ToggleGroup bounceDefinitionGroup = new ToggleGroup();

    pageleftBounceToggle.setToggleGroup(bounceDefinitionGroup);
    singlePageBounceToggle.setToggleGroup(bounceDefinitionGroup);



    HBox datePickerBox = new HBox();

    DatePicker datePicker1 = new DatePicker();
    datePicker1.setPrefSize(200.0, 35.0);

    Label toLabel = new Label("to");
    DatePicker datePicker2 = new DatePicker();
    datePicker2.setPrefSize(200.0, 35.0);

    datePickerBox.getChildren().addAll(datePicker1, toLabel, datePicker2);
    datePickerBox.setPadding(new Insets(5, 0, 50, 0));
    datePickerBox.setAlignment(Pos.CENTER);


    Button applyButton = new Button("Apply filters");
    applyButton.setPrefSize(133, 26);
    applyButton.setOnAction(
        e -> {
          generateGraph(currentCampaign.getName());
          updateBounceRate(currentCampaign.getName());
        });

    filtersPanel.getChildren().addAll(filterLabel, topfilterBox, bottomfilterBox, timeGranularityLabel, timeGranularityToggleBox, bounceDefinitionLabel, bounceDefinitionBox, datePickerBox, applyButton);
    filtersPanel.setAlignment(Pos.CENTER);

    leftPanel.getChildren().addAll(metricsPanel, filtersPanel);

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

    // StackPane (To Toggle Between Line Chart and Histogram)
    chartContainer = new StackPane();
    histogramPanel = new ChartPanel(clickCostHistogram.createBlankHistogram());

    // StackPane (To Toggle Between Line Chart and Histogram)

    // Ensure SwingNode initializes properly
    Platform.runLater(() -> swingNode.setContent(histogramPanel));

    chartContainer.getChildren().addAll(lineChart, swingNode);
    swingNode.setVisible(false); // Hide Histogram Initially

    VBox centerPanel = new VBox(chartContainer);
    centerPanel.setPrefSize(600, 400);

    toggleChartBtn.setOnAction(
        e -> {
          if (lineChart.isVisible()) {
            toggleHistogramTypeBtn.setVisible(true);

            lineChart.setVisible(false);
            Platform.runLater(
                () -> swingNode.setContent(histogramPanel)); // Ensure correct embedding
            swingNode.setVisible(true);
            toggleChartBtn.setText("Switch to Performance Chart");
            isClickByCost = true;
            toggleHistogramTypeBtn.setText("Clicks by time");

          } else {
            swingNode.setVisible(false);
            lineChart.setVisible(true);
            toggleChartBtn.setText("Switch to Histogram");
            toggleHistogramTypeBtn.setVisible(false);
            isClickByCost = true;
            toggleHistogramTypeBtn.setText("Clicks by time");
          }
        });
//

    toggleHistogramTypeBtn.setOnAction(e -> {
      isClickByCost = !isClickByCost; // Toggle state first
      toggleHistogramTypeBtn.setText(isClickByCost ? "Clicks by Time" : "Clicks by Cost");

      updateHistogram();  // Ensure histogram updates correctly
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
            checkFileValid(selectedFile, "Impression");
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
            checkFileValid(selectedFile, "Click");
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
            checkFileValid(selectedFile, "Server");
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
          Boolean campaign_name_flag = checkCampaignNameValid(campaignName);
//          Boolean campaign_name_flag = true;
            if (impression_log_flag && click_log_flag && server_log_flag && campaign_name_flag) {
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
            } else {
              showAlert(null, "Files");
            }
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

  public void checkFileValid(File file, String kind){
    List<String> data = statsCalculator.getCSVStructure(file.getAbsolutePath());
    System.out.println(data);
    if (kind.equals("Impression")){
      List<String> fields = Arrays.asList("Date", "ID", "Gender", "Age", "Income", "Context", "Impression Cost");
      if((data.equals(fields))){
        impression_log_flag = true;
      }else{
        showAlert(file, "Impression");
      }
    }else if(kind.equals("Click")){
      List<String> fields = Arrays.asList("Date", "ID", "Click Cost");
      if((data.equals(fields))){
        click_log_flag = true;
      }else{
        showAlert(file, "Click");
      }

    }else if(kind.equals("Server")){
      List<String> fields = Arrays.asList("Entry Date", "ID", "Exit Date", "Pages Viewed", "Conversion");
      if((data.equals(fields))){
        server_log_flag = true;
      }else{
        showAlert(file, "Server");
      }

    }

  }

  public boolean checkCampaignNameValid(String campaignName){
    if(statsCalculator.isCampaignExists(campaignName)){
      showAlert(null,"campaignname");
      return false;
    }else{
      return true;
    }
  }

  // Function to display an alert
  public static void showAlert(File file, String type) {
    Alert alert = new Alert(Alert.AlertType.ERROR);  // You can adjust the AlertType as needed

    if (type.equals("Files")){
      alert.setTitle("Error");
      alert.setHeaderText("Wrong files");
      alert.setContentText("One or more files are either not entered or in the correct format");
    }else if(type.equals("campaignname")){
      alert.setTitle("Error");
      alert.setHeaderText("Campaign name wrong");
      alert.setContentText("Campaign name already exists");
    } else {
      alert.setTitle("Error");
      alert.setHeaderText("Wrong format");
      alert.setContentText(file.getName() + " has wrong format for " + type + " log file");
    }
    // Show and wait for the user to close the alert
    alert.showAndWait();
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
    if (!(statsCalculator.isCampaignExists(campaign.getName()))) {
      System.out.println("Campaign does not exist");
      statsCalculator.setup(
          campaign,
          campaign.getImpressionLogFile(),
          campaign.getClicksLogFile(),
          campaign.getServerLogFile());
    }
    if (!(currentCampaign.equals(campaign))){
      updateStats(campaign.getName());
      pageleftBounceToggle.fire();
      currentCampaign = campaign;
    }
  }

  private void updateBounceRate(String campaignName){
    double bounceRate;
    if(pageleftBounceToggle.isSelected()){
      bounceRate = statsCalculator.calculateBounceRate(campaignName).getOrDefault("Page Rate", 0.0);
    }else {
      bounceRate = statsCalculator.calculateBounceRate(campaignName).getOrDefault("Single Rate", 0.0);
    }
    bounceRateValue.setText(String.format("%,.2f%%", bounceRate * 100));
  }

  /**
   * Updates the labels when new campaign is selected
   */
  // Update statistics
  private void updateStats(String campaignName) {

  List<Double> costslist = statsCalculator.getCostsList(campaignName);
  isClickByCost = true;
  isDataDownloaded = true;

    double bounceRate =
      statsCalculator.calculateBounceRate(campaignName).getOrDefault("Page Rate", 0.0);
  double ctr = statsCalculator.calculateCTR(campaignName);
  double cpa = statsCalculator.calculateCPA(campaignName);
  double cpc = statsCalculator.calculateCPC(campaignName);
  double cpm = statsCalculator.calculateCPM(campaignName);
  double totalCost = statsCalculator.calculateTotalCost(campaignName);
  toggleHistogramTypeBtn.setText( "Clicks by Time");

  ChartPanel newHistogramPanel = new ChartPanel(new ClickCostHistogram(costslist).createHistogram());
  int indexofhistogram = chartContainer.getChildren().indexOf(swingNode);
    Platform.runLater(() -> swingNode.setContent(newHistogramPanel));
    chartContainer.getChildren().set(indexofhistogram, swingNode);
  Map<String, Double> coreMetrics = statsCalculator.getCoreMetrics(campaignName);
    // Toggle Histogram & Performance Chart
    toggleChartBtn.setOnAction(e -> {
      if (lineChart.isVisible()) {
        updateHistogram();
        lineChart.setVisible(false);
        Platform.runLater(() -> swingNode.setContent(newHistogramPanel));
        swingNode.setVisible(true);
        toggleChartBtn.setText("Switch to Performance Chart");
        toggleHistogramTypeBtn.setVisible(true);
        toggleHistogramTypeBtn.setText( "Clicks by Time");
        isClickByCost=true;
      } else {
        swingNode.setVisible(false);
        lineChart.setVisible(true);
        toggleChartBtn.setText("Switch to Histogram");
        toggleHistogramTypeBtn.setVisible(false);
        toggleHistogramTypeBtn.setText( "Clicks by Time");
        isClickByCost=true;
      }
    });

    // Toggle Between Click Cost & Click Time Histogram
    toggleHistogramTypeBtn.setOnAction(e -> {
      isClickByCost = !isClickByCost;
      toggleHistogramTypeBtn.setText(isClickByCost ? "Clicks by Time" : "Clicks by Cost");
      updateHistogram();
    });


    impressionsValue.setText(
        String.format("%,.0f", coreMetrics.getOrDefault("Impressions", 0.0)));
    clicksValue.setText(
        String.format("%,.0f", coreMetrics.getOrDefault("Clicks", 0.0)));
    uniquesValue.setText(
        String.format("%,.0f", coreMetrics.getOrDefault("Uniques", 0.0)));
    conversionsValue.setText(
        String.format("%,.0f", coreMetrics.getOrDefault("Conversions", 0.0)));
    bounceRateValue.setText(String.format("%,.2f%%", bounceRate * 100));
    ctrValue.setText(String.format("%,.2f%%", ctr));
    cpaValue.setText(String.format("%,.2f", cpa));
    cpcValue.setText(String.format("%,.2f", cpc));
    cpmValue.setText(String.format("%,.2f", cpm));
    totalCostValue.setText(String.format("£" + "%,.2f", totalCost));
  generateGraph(campaignName);
}
  private void generateGraph(String campaignName) {
    //real graph implementation
    String bouncetype;
    if (singlePageBounceToggle.isSelected()){
      bouncetype = "SinglePage";
    }
    else{
      bouncetype = "PageLeft";
    }


    Map<String, Map<String, Integer>> metricsOverTime = statsCalculator.getMetricsOverTime(campaignName, bouncetype);

    // Clear existing chart data
    lineChart.getData().clear();

    // Find max values for each metric
    int maxImpressions = metricsOverTime.get("Impressions").values().stream().max(Integer::compare).orElse(1);
    int maxClicks = metricsOverTime.get("Clicks").values().stream().max(Integer::compare).orElse(1);
    int maxUniques = metricsOverTime.get("Uniques").values().stream().max(Integer::compare).orElse(1);
    int maxConversions = metricsOverTime.get("Conversions").values().stream().max(Integer::compare).orElse(1);
    int maxBounces = metricsOverTime.get("Bounces").values().stream().max(Integer::compare).orElse(1);

    // Store all metrics in a list and sort them
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
    scaleFactors.put(secondLargest, 50.0 );
    scaleFactors.put(middleMetric, 75.0);
    scaleFactors.put(secondSmallest, 100.0 );
    scaleFactors.put(smallestMetric, 150.0);

    // Debug print to verify scaling
    System.out.println("Scaling Factors: " + scaleFactors);

    // Apply scaling dynamically
    addSeriesToChart(metricsOverTime.get("Impressions"), "Impressions (x" + scaleFactors.get("Impressions") + ")", scaleFactors.get("Impressions"));
    addSeriesToChart(metricsOverTime.get("Clicks"), "Clicks (x" + scaleFactors.get("Clicks") + ")", scaleFactors.get("Clicks"));
    addSeriesToChart(metricsOverTime.get("Uniques"), "Uniques (x" + scaleFactors.get("Uniques") + ")", scaleFactors.get("Uniques"));
    addSeriesToChart(metricsOverTime.get("Conversions"), "Conversions (x" + scaleFactors.get("Conversions") + ")", scaleFactors.get("Conversions"));
    addSeriesToChart(metricsOverTime.get("Bounces"), "Bounces (x" + scaleFactors.get("Bounces") + ")", scaleFactors.get("Bounces"));
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

  private void updateHistogram() {
    ChartPanel newHistogramPanel;
    if(!isDataDownloaded) {
      newHistogramPanel = new ChartPanel(clickCostHistogram.createBlankHistogram());
      }
    else if (isClickByCost) {
        List<Double> clickCosts = statsCalculator.getCostsList(currentCampaign.getName());
        newHistogramPanel = new ChartPanel(new ClickCostHistogram(clickCosts).createHistogram());
      }
    else{
      Map<String, Integer> clicksByDate = statsCalculator.getClicksOverTime(currentCampaign.getName());
      newHistogramPanel = new ChartPanel(new ClickCostHistogram(clicksByDate).createHistogram());

    }


    Platform.runLater(() -> swingNode.setContent(newHistogramPanel));
  }
  private void logout() {
    System.out.println("🔹 Logging out...");


    campaigns.clear();
    currentCampaign = new Campaign("", new File(""), new File(""), new File(""));
    impressionLogFile = null;
    clicksLogFile = null;
    serverLogFile = null;
    impression_log_flag = false;
    click_log_flag = false;
    server_log_flag = false;
    swingNode.setContent(null); // Clear histogram
    lineChart.getData().clear(); // Clear performance graph


    LoginPage loginPage = new LoginPage(primaryStage);
    loginPage.show(primaryStage);
  }


}