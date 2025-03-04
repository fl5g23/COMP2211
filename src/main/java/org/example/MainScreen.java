
package org.example;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.Map;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import javafx.embed.swing.SwingNode;
public class MainScreen {

  private final Stage primaryStage;
  private StatsCalculator statsCalculator;
  private ClickCostHistogram clickCostHistogram;
  private StackPane chartContainer;
  private LineChart<Number, Number> lineChart;
  private ChartPanel histogramPanel;

  public MainScreen(Stage stage) {
    this.primaryStage = stage;
    this.statsCalculator = new StatsCalculator();
    this.clickCostHistogram = new ClickCostHistogram(statsCalculator.getCostsList());

  }

  public void show() {
    primaryStage.setTitle("Ad Campaign Dashboard");

    // Top bar with title and Add Campaign button
    HBox topBar = new HBox();
    topBar.setStyle("-fx-background-color: #ddd; -fx-padding: 10px;");
    Label title = new Label("Campaign Name");
    title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
    Button addCampaignBtn = new Button("+ Add Campaign");
    addCampaignBtn.setStyle("-fx-background-color: #555; -fx-text-fill: white;");
    Button toggleChartBtn = new Button("Switch to Histogram");
    toggleChartBtn.setStyle("-fx-background-color: #555; -fx-text-fill: white;");


    topBar.getChildren().addAll(title, addCampaignBtn,toggleChartBtn);
    topBar.setSpacing(20);

    // Left panel with campaign statistics
    VBox leftPanel = new VBox();
    leftPanel.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 15px;");
    leftPanel.setSpacing(10);

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

    Map<String, Double> coreMetrics = statsCalculator.getCoreMetrics();
    double bounceRate = statsCalculator.calculateBounceRate().getOrDefault("Single Rate", 0.0);
    double ctr = statsCalculator.calculateCTR();
    double cpa = statsCalculator.calculateCPA();
    double cpc = statsCalculator.calculateCPC();
    double cpm = statsCalculator.calculateCPM();
    double totalCost = statsCalculator.calculateTotalCost();

    impressionsLabel.setText("Number of Impressions: " + coreMetrics.getOrDefault("Impressions", 0.0));
    clicksLabel.setText("Clicks on Advertisement: " + coreMetrics.getOrDefault("Clicks", 0.0));
    uniquesLabel.setText("Unique Clicks on Advertisement: " + coreMetrics.getOrDefault("Uniques", 0.0));
    conversionsLabel.setText("Conversions: " + coreMetrics.getOrDefault("Conversions", 0.0));
    bounceRateLabel.setText("Bounce Rate: " + String.format("%.2f%%", bounceRate * 100));
    ctrLabel.setText("CTR - Click Through Rate: " + String.format("%.2f%%", ctr));
    cpaLabel.setText("CPA - Cost Per Acquisition: £" + String.format("%.2f", cpa));
    cpcLabel.setText("CPC - Cost Per Click: £" + String.format("%.2f", cpc));
    cpmLabel.setText("CPM - Cost Per Thousand Impressions: £" + String.format("%.2f", cpm));
    totalCostLabel.setText("Total Cost: £" + String.format("%.2f", totalCost));

    leftPanel.getChildren().addAll(
        impressionsLabel, clicksLabel, uniquesLabel, conversionsLabel,
        bounceRateLabel, ctrLabel, cpaLabel, cpcLabel, cpmLabel, totalCostLabel
    );

//  CENTER PANEL WITH LINE CHART
    NumberAxis xAxis = new NumberAxis();
    NumberAxis yAxis = new NumberAxis();
    lineChart = new LineChart<>(xAxis, yAxis);
    lineChart.setTitle("Campaign Performance Over Time");

    XYChart.Series<Number, Number> series = new XYChart.Series<>();
    series.setName("Clicks");

//  Dummy Data (Ensure Some Data Exists)
    series.getData().add(new XYChart.Data<>(1, 10));
    series.getData().add(new XYChart.Data<>(2, 25));
    series.getData().add(new XYChart.Data<>(3, 30));
    series.getData().add(new XYChart.Data<>(4, 15));
    series.getData().add(new XYChart.Data<>(5, 40));
    lineChart.getData().add(series);

//  HISTOGRAM PANEL (JFreeChart)
    histogramPanel = new ChartPanel(clickCostHistogram.createHistogram());

//  STACK PANE (To Toggle Between Line Chart and Histogram)
    chartContainer = new StackPane();
    SwingNode swingNode = new SwingNode();

  // Ensure SwingNode initializes properly
    Platform.runLater(() -> swingNode.setContent(histogramPanel));

    chartContainer.getChildren().addAll(lineChart, swingNode);
    swingNode.setVisible(false); // Hide Histogram Initially

    VBox centerPanel = new VBox(chartContainer);
    centerPanel.setPrefSize(600, 400);
    toggleChartBtn.setOnAction(e -> {
      if (lineChart.isVisible()) {
        lineChart.setVisible(false);
        Platform.runLater(() -> swingNode.setContent(histogramPanel)); // Ensure correct embedding
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
}
