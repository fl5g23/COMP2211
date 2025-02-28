
package org.example;

import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainScreen {

  private final Stage primaryStage;

  public MainScreen(Stage stage) {
    this.primaryStage = stage;
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

    topBar.getChildren().addAll(title, addCampaignBtn);
    topBar.setSpacing(20);

    // Left panel with campaign statistics
    VBox leftPanel = new VBox();
    leftPanel.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 15px;");
    leftPanel.setSpacing(10);

    String[] metrics = {
        "Number of Impressions:",
        "Clicks on Advertisement:",
        "Unique Clicks on Advertisement:",
        "Bounces on Advertisement:",
        "Conversions:",
        "Bounce Rate:",
        "CTR - Click Through Rate:",
        "CPA - Cost Per Acquisition:",
        "CPC - Cost Per Click:",
        "CPM - Cost Per Thousand Impressions:",
        "Total Cost:"
    };

    for (String metric : metrics) {
      leftPanel.getChildren().add(new Label(metric));
    }

    // Center panel with LineChart for campaign performance
    NumberAxis xAxis = new NumberAxis();
    NumberAxis yAxis = new NumberAxis();
    LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
    lineChart.setTitle("Campaign Performance Over Time");

    XYChart.Series<Number, Number> series = new XYChart.Series<>();
    series.setName("Clicks");

    // Dummy data for chart
    series.getData().add(new XYChart.Data<>(1, 10));
    series.getData().add(new XYChart.Data<>(2, 25));
    series.getData().add(new XYChart.Data<>(3, 30));
    series.getData().add(new XYChart.Data<>(4, 15));
    series.getData().add(new XYChart.Data<>(5, 40));

    lineChart.getData().add(series);

    // Layout
    BorderPane root = new BorderPane();
    root.setTop(topBar);
    root.setLeft(leftPanel);
    root.setCenter(lineChart);

    Scene scene = new Scene(root, 900, 600);
    primaryStage.setScene(scene);
    primaryStage.show();
  }
}
