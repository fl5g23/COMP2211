package org.example.Views;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Controllers.UIController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CompareGraphsResultView {
    private Stage primaryStage;
    private UIController controller;
    Map<String, String> filtersMap1;
    Map<String, String> filtersMap2;
    LocalDateTime startselectedDateLeft;
    LocalDateTime endselectedDateLeft;
    LocalDateTime startselectedDateRight;
    LocalDateTime endselectedDateRight;


    private LineChart<String, Number> lineChart1;
    private LineChart<String, Number> lineChart2;


    public CompareGraphsResultView(Stage primaryStage, UIController uiController, Map<String, String> filtersMap1, LocalDateTime startselectedDateLeft, LocalDateTime endselectedDateLeft, Map<String, String> filtersMap2, LocalDateTime startselectedDateRight, LocalDateTime endselectedDateRight) {
        this.primaryStage = primaryStage;
        this.controller = uiController;
        this.filtersMap1 = filtersMap1;
        this.filtersMap2 = filtersMap2;
        this.startselectedDateLeft = startselectedDateLeft;
        this.startselectedDateRight = startselectedDateRight;
        this.endselectedDateLeft = endselectedDateLeft;
        this.endselectedDateRight = endselectedDateRight;

    }

    public void show() {
        Stage compareResultStage = new Stage();
        compareResultStage.setTitle("Comparison Result");

        String metric = filtersMap1.get("selectedMetric");

        // Create two charts
        lineChart1 = createLineChart("Graph 1: " + metric);
        lineChart2 = createLineChart("Graph 2: " + metric);

        // Generate graphs using existing controller method
        generateGraphForChart(lineChart1, filtersMap1, startselectedDateLeft, endselectedDateLeft);
        generateGraphForChart(lineChart2, filtersMap2, startselectedDateRight, endselectedDateRight);

        // Layout for two charts side by side
        HBox chartContainer = new HBox(20, lineChart1, lineChart2);
        chartContainer.setAlignment(Pos.CENTER);
        chartContainer.setPadding(new Insets(20));

        // Exit Button
        Button exitButton = new Button("Close");
        exitButton.setOnAction(e -> compareResultStage.close());

        VBox layout = new VBox(10, chartContainer, exitButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 1000, 600);
        compareResultStage.setScene(scene);
        compareResultStage.show();
    }

    private LineChart<String, Number> createLineChart(String title) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Count");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setAnimated(false);
        return chart;
    }

    private void generateGraphForChart(LineChart<String, Number> chart,
                                       Map<String, String> filtersMap, LocalDateTime startDate, LocalDateTime endDate) {
        // Use existing generateGraph() method from UIController

        String selectedMetric = filtersMap.get("selectedMetric");
        controller.queryStatistics(filtersMap,startDate,endDate);

        // Fetch graph data from controller
        Map<String, Map<String, Integer>> metricsOverTime = controller.dataController.getMetricsOverTime(filtersMap);
        Map<String,Integer> dataMap = metricsOverTime.get(selectedMetric);

        System.out.println("Data received in UI: " + metricsOverTime);

        chart.getData().clear();

        if (!metricsOverTime.containsKey(selectedMetric)) {
            System.out.println("Metric not found: " + selectedMetric);
            return;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(selectedMetric);



        List<String> sortedDates = new ArrayList<>(dataMap.keySet());
        sortedDates.sort(Comparator.naturalOrder());

        for (String date : sortedDates) {
            double yValue = dataMap.get(date) * 1.0;
            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(date, yValue);

            // Add tooltip showing raw and scaled values
            Tooltip tooltip = new Tooltip(selectedMetric + "\nDate: " + date +
                    "\nRaw: " + dataMap.get(date) + "\nScaled: " + yValue);
            Platform.runLater(() -> Tooltip.install(dataPoint.getNode(), tooltip));

            series.getData().add(dataPoint);
        }


        Platform.runLater(() -> chart.getData().add(series));

    }
}