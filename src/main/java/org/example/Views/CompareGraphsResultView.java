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
import org.example.Models.FiltersBox;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CompareGraphsResultView {
    private Stage primaryStage;
    private UIController controller;
    private FiltersBox rightGraphFilters;
    private FiltersBox leftGraphFilters;
    private LineChart<String, Number> lineChartLeft;
    private LineChart<String, Number> lineChartRight;


    public CompareGraphsResultView(Stage primaryStage, UIController uiController, FiltersBox rightGraphFilters, FiltersBox leftGraphFilters) {
        this.primaryStage = primaryStage;
        this.controller = uiController;

        this.rightGraphFilters = rightGraphFilters;
        this.leftGraphFilters = leftGraphFilters;

    }

    public void show() {
        Stage compareResultStage = new Stage();
        compareResultStage.setTitle("Comparison Result");

        String metric = rightGraphFilters.getMetric();
        lineChartLeft = createLineChart("Graph 1: " + metric);
        lineChartRight = createLineChart("Graph 2: " + metric);

        // Generate graphs using existing controller method
        generateGraphForChart(lineChartLeft, rightGraphFilters);
        generateGraphForChart(lineChartRight, leftGraphFilters);

        // Layout for two charts side by side
        HBox chartContainer = new HBox(20, lineChartLeft, lineChartRight);
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
                                       FiltersBox filtersBox) {
        // Use existing generateGraph() method from UIController

        String selectedMetric = filtersBox.getMetric();
        controller.queryStatistics(filtersBox);

        // Fetch graph data from controller
        Map<String, Map<String, Integer>> metricsOverTime = controller.dataController.getMetricsOverTime(filtersBox);
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