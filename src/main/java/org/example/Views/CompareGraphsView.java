package org.example.Views;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.Controllers.UIController;
import org.example.Models.Campaign;
import org.example.Models.FiltersBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class CompareGraphsView{
    private Stage primaryStage;
    private UIController uiController;
    private ComboBox<String> metricDropdown;
    private LocalDate startDate, endDate;
    private Campaign campaign;

    FiltersBox graph1Filters;
    FiltersBox graph2Filters;


    public CompareGraphsView(Stage primaryStage, UIController uiController, LocalDate startDate, LocalDate endDate, Campaign campaign){
        this.primaryStage = primaryStage;
        this.uiController = uiController;
        this.startDate = startDate;
        this.endDate = endDate;
        this.campaign = campaign;
    }

    public void show(){
    Stage stage = new Stage();
    stage.setTitle("Compare Graphs");

    HBox topBar = new HBox();
    topBar.setAlignment(Pos.TOP_LEFT);

    VBox metricBox = new VBox();
    metricBox.setAlignment(Pos.CENTER);
    Label metricLabel = new Label("Choose a Metric:");
    metricDropdown = new ComboBox<>();
    metricDropdown.getItems().addAll("Impressions", "Clicks", "Uniques", "Bounces" );
    metricDropdown.setValue("Impressions");
    metricBox.getChildren().addAll(metricLabel, metricDropdown);
    metricBox.setSpacing(5);

    String metric = metricDropdown.getValue();
    String campaignName = campaign.getName();
    graph1Filters = new FiltersBox("Choose Filters for Graph 1:", campaignName, startDate, endDate, 300, 500);
    graph2Filters = new FiltersBox("Choose Filters for Graph 2:", campaignName, startDate, endDate, 300, 500);
    graph1Filters.setMetric(metric);
    graph2Filters.setMetric(metric);

    Button compareButton = new Button("Compare");
    compareButton.setOnAction(e -> {
        CompareGraphsResultView resultView = new CompareGraphsResultView( primaryStage, uiController, graph1Filters, graph2Filters);
        resultView.show();
        stage.close();
    });

    HBox filtersContainer = new HBox(40, graph1Filters, graph2Filters);
    filtersContainer.setAlignment(Pos.CENTER);
    filtersContainer.setPadding(new Insets(20, 0, 20, 0));

    VBox root = new VBox(15, topBar, metricBox, filtersContainer, compareButton);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(20));

    Scene showScene = new Scene(root, 800, 500);
    stage.setScene(showScene);
    stage.show();
    }


}