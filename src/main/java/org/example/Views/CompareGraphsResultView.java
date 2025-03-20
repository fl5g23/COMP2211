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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Controllers.UIController;

import java.util.Map;

public class CompareGraphsResultView {
    private Stage primaryStage;
    private UIController controller;
    private String metric;
    private String gender1, age1, income1, context1, startDate1, endDate1;
    private String gender2, age2, income2, context2, startDate2, endDate2;

    private LineChart<String, Number> lineChart1;
    private LineChart<String, Number> lineChart2;

    public CompareGraphsResultView(
            Stage primaryStage, UIController controller, String metric,
            String gender1, String age1, String income1, String context1, String startDate1, String endDate1,
            String gender2, String age2, String income2, String context2, String startDate2, String endDate2) {
        this.primaryStage = primaryStage;
        this.controller = controller;
        this.metric = metric;
        this.gender1 = gender1;
        this.age1 = age1;
        this.income1 = income1;
        this.context1 = context1;
        this.startDate1 = startDate1;
        this.endDate1 = endDate1;
        this.gender2 = gender2;
        this.age2 = age2;
        this.income2 = income2;
        this.context2 = context2;
        this.startDate2 = startDate2;
        this.endDate2 = endDate2;
    }

    public void show() {
        Stage compareResultStage = new Stage();
        compareResultStage.setTitle("Comparison Result");

        // Create two charts
        lineChart1 = createLineChart("Graph 1: " + metric);
        lineChart2 = createLineChart("Graph 2: " + metric);

        // Generate graphs using existing controller method
        generateGraphForChart(lineChart1, gender1, age1, income1, context1, startDate1, endDate1);
        generateGraphForChart(lineChart2, gender2, age2, income2, context2, startDate2, endDate2);

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
                                       String gender, String age, String income, String context,
                                       String startDate, String endDate) {
        // Use existing generateGraph() method from UIController
        controller.generateGraph("Campaign1", "PageLeft", metric, "Daily", gender);

        // Fetch graph data from controller
        Map<String, Map<String, Integer>> metricsOverTime = controller.dataController.getMetricsOverTime("Campaign1", "PageLeft", gender);

        if (metricsOverTime.containsKey(metric)) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(metric);

            metricsOverTime.get(metric).forEach((date, value) -> {
                series.getData().add(new XYChart.Data<>(date, value));
            });

            Platform.runLater(() -> chart.getData().add(series));
        }
    }
}