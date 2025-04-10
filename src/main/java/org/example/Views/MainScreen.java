package org.example.Views;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

import javax.imageio.ImageIO;
import org.example.Models.Campaign;
import org.example.Models.ClickCostHistogram;
import org.example.Controllers.UIController;
import org.example.Models.FiltersBox;
import org.jfree.chart.ChartPanel;
import javafx.embed.swing.SwingNode;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

public class MainScreen {

  private final Stage primaryStage;
  private MenuButton campaignMenuButton; // Replaces the title label after a campaign is added
  private SwingNode swingNode = new SwingNode();
  private LineChart<String, Number> lineChart;
  private StackPane chartContainer;
  private ChartPanel histogramPanel;
  private ClickCostHistogram clickCostHistogram = new ClickCostHistogram();
  private boolean isClickByCost = true; // Track histogram type (true = Clicks by Cost, false = Clicks by Time)
  private UIController controller; // Reference to the controller
  Boolean firstGraphGeneration = true; //solves bug with chart generation
  private LocalDate startDate;
  private LocalDate endDate;

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
  FiltersBox filtersPanel;


  private ToggleButton toggleHistogramTypeBtn = new ToggleButton("Clicks by time");
  ComboBox<String> metricDropdown = new ComboBox<>();
  private JFreeChart currentHistogramChart;

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

    exportButton.setOnAction(
        e -> {
          Campaign selectedCampaign = getSelectedCampaign();
          if (selectedCampaign != null) {
            if (lineChart.isVisible()) {
              exportChartWithDialog(lineChart); // Export JavaFX LineChart
            } else {
              exportHistogramAsImage(); // Export JFreeChart Histogram
            }
          }else{
            showAlert(null, "exportingbeforecampaignloaded");
          }
        });

    Button compareGraphButton = new Button("Compare Graphs");
    compareGraphButton.setOnAction(e -> {
      Campaign selectedCampaign = getSelectedCampaign();
      if (selectedCampaign != null){
        CompareGraphsView compareGraphsView = new CompareGraphsView(primaryStage, controller, startDate, endDate, selectedCampaign);
        compareGraphsView.show();
      } else {
        showAlert(null, "comparingbeforecampaignloaded");
      }
    });


    Button authoriseUsersButton = new Button("Authorise Users");
    authoriseUsersButton.setOnAction(e -> controller.openAuthoriseUsersPage());

    topBar.getChildren().addAll(title, logoutButton, toggleChartBtn, exportSelectBox, exportButton, compareGraphButton, toggleHistogramTypeBtn, authoriseUsersButton);
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

    Scene scene = new Scene(root, 1100, 600);
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
    filtersPanel = new FiltersBox(null, null, null, null, 132,  500);

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
    metricBox.setPadding(new Insets(0, 0, 5, 0));

    // keeping the apply Filters button at bottom
    Button applyButton = new Button("Apply filters");
    applyButton.setPrefSize(133, 26);
    applyButton.setOnAction(
        e -> {
          Campaign selectedCampaign = getSelectedCampaign();
          if (selectedCampaign != null) {
            LocalDateTime startselectedDate = filtersPanel.getStartDate();
            LocalDateTime endselectedDate = filtersPanel.getEndDate();

            if (startselectedDate.isAfter(endselectedDate)) {
              showAlert(null, "calendardateswrongorder");
            } else {

              filtersPanel.setMetric(metricDropdown.getValue());
              controller.queryStatistics(filtersPanel);
              controller.updateStatistics(selectedCampaign.getName());
              controller.generateGraph(filtersPanel);
              controller.updateBounceRate(
                  selectedCampaign.getName(), filtersPanel.getBounceValue());
            }
          }
        });

    // Extract existing children
    ObservableList<Node> originalChildren = FXCollections.observableArrayList(filtersPanel.getChildren());

// Create your returnBox
    VBox returnBox = new VBox();
    returnBox.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 15px;");
    returnBox.setPrefSize(132, 500);
    returnBox.setAlignment(Pos.CENTER);


// Add in desired order
    returnBox.getChildren().add(metricBox); // MetricBox (second)
    for (int i = 0; i < originalChildren.size(); i++) {
      returnBox.getChildren().add(originalChildren.get(i));
    }
    returnBox.getChildren().add(applyButton);
    returnBox.setAlignment(Pos.CENTER);

    return returnBox;
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
    } else if (type.equals("calendardateswrongorder")){
      alert.setTitle("Error");
      alert.setHeaderText("Dates wrong order");
      alert.setContentText("The starting date must come before the ending date");
    }else if (type.equals("comparingbeforecampaignloaded")){
      alert.setTitle("Error");
      alert.setHeaderText("Compare Graphs Failed");
      alert.setContentText("A campaign must be loaded before you compare graphs");
    }else if (type.equals("exportingbeforecampaignloaded")){
      alert.setTitle("Error");
      alert.setHeaderText("Export Graph failed");
      alert.setContentText("A campaign must be loaded before you export");
    }
    else {
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
  public void updatePerformanceGraph(Map<String, Map<String, Integer>> metricsOverTime, String selectedMetric, String granularity) {
    System.out.println("Data received in UI: " + metricsOverTime);

    //    if (!lastGranularity.equals(granularity)){
    //      lineChart.setAnimated(false);
    //      lastGranularity = granularity;
    //    }else{
    //      lineChart.setAnimated(true);
    //    }

    if (!firstGraphGeneration) {
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
    ClickCostHistogram histogram = new ClickCostHistogram(clickCosts);
    currentHistogramChart = histogram.getChart();  // ✅ Store for exporting
    ChartPanel newHistogramPanel = new ChartPanel(currentHistogramChart);
    Platform.runLater(() -> swingNode.setContent(newHistogramPanel));
  }

  /**
   * Update the histogram for click times
   */
  public void updateClickTimeHistogram(Map<String, Integer> clicksByDate) {
    ClickCostHistogram histogram = new ClickCostHistogram(clicksByDate);
    currentHistogramChart = histogram.getChart();  //  Store for exporting
    ChartPanel newHistogramPanel = new ChartPanel(currentHistogramChart);
    Platform.runLater(() -> swingNode.setContent(newHistogramPanel));
  }



  // Add this to your main application class where you set up the primary stage
  private void setupCloseHandler(Stage primaryStage) {
    primaryStage.setOnCloseRequest(event -> {
      controller.closeAppActions();
    });
  }

  public void setFirstGenerationFilters(LocalDateTime startdatetime, LocalDateTime enddatetime, String campaignName){
    metricDropdown.setValue("Impressions");

     startDate = startdatetime.toLocalDate();
     endDate = enddatetime.toLocalDate();
    filtersPanel.selectFirstGenerationFilters(startDate, endDate, campaignName);
  }


  public void exportChartWithDialog(LineChart<String, Number> chart) {
    // Open FileChooser Dialog
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Chart As Image");

    // Add file format options
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPEG Image", "*.jpg"));

    // Show Save Dialog
    File file = fileChooser.showSaveDialog(null);

    if (file != null) {
      saveChartAsImage(chart, file);
    }
  }

  // Helper function to save the chart as an image
  private void saveChartAsImage(LineChart<String, Number> chart, File file) {
    WritableImage image = chart.snapshot(null, null);
    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

    // Detect file format
    String fileName = file.getName().toLowerCase();
    String format = fileName.endsWith(".jpg") ? "jpg" : "png";

    // If saving as JPG, remove transparency by drawing on a white background
    if (format.equals("jpg")) {
      BufferedImage whiteBackgroundImage = new BufferedImage(
          bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);

      Graphics2D g2d = whiteBackgroundImage.createGraphics();
      g2d.setColor(Color.WHITE);  // Set background color to white
      g2d.fillRect(0, 0, whiteBackgroundImage.getWidth(), whiteBackgroundImage.getHeight());
      g2d.drawImage(bufferedImage, 0, 0, null);
      g2d.dispose();

      bufferedImage = whiteBackgroundImage;  // Use the new image
    }

    try {
      ImageIO.write(bufferedImage, format, file);
      System.out.println("Chart saved as: " + file.getAbsolutePath());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public void exportHistogramAsImage() {
    if (currentHistogramChart == null) {
      System.out.println(" No histogram available to export.");
      return;
    }

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Histogram As Image");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPEG Image", "*.jpg"));

    File file = fileChooser.showSaveDialog(null);
    if (file != null) {
      try {
        ChartUtils.saveChartAsPNG(file, currentHistogramChart, 800, 600);
        System.out.println("Histogram saved as: " + file.getAbsolutePath());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}