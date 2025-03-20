package org.example.Views;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.Controllers.UIController;

public class CompareGraphsView{
    private Stage primaryStage;
    private UIController uiController;
    private ComboBox<String> metricDropdown;
    private ComboBox<String> genderComboBox1, ageComboBox1, incomeComboBox1, contextComboBox1;
    private ComboBox<String> genderComboBox2, ageComboBox2, incomeComboBox2, contextComboBox2;
    private DatePicker datePicker1, datePicker2, datePicker3, datePicker4;

    public CompareGraphsView(Stage primaryStage, UIController uiController){
        this.primaryStage = primaryStage;
        this.uiController = uiController;
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

    VBox graph1Filters = createFiltersBox("Choose Filters for Graph 1:", true);
    VBox graph2Filters = createFiltersBox("Choose Filters for Graph 2:", false);

    Button compareButton = new Button("Compare");
    compareButton.setOnAction(e -> handleCompareButton(stage));

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

    private VBox createFiltersBox(String title, boolean isGraph){
        Label filterTitle = new Label(title);

        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Male", "Female");
        genderBox.setPromptText("Gender");

        ComboBox<String> ageBox = new ComboBox<>();
        ageBox.getItems().addAll("<25 ", "25-34", "35-44", "45-54", ">54");
        ageBox.setPromptText("Age");

        ComboBox<String> incomeBox = new ComboBox<>();
        incomeBox.getItems().addAll("Low ", "Medium", "High");
        incomeBox.setPromptText("Income");

        ComboBox<String> contextBox = new ComboBox<>();
    contextBox.getItems().addAll("News", "Shopping", "Social Media", "Blog", "Hobbies", "Travel");
        contextBox.setPromptText("Context");

        HBox dateBox = new HBox();
        DatePicker startDate = new DatePicker();
        startDate.setPrefSize(100, 25);
        DatePicker endDate = new DatePicker();
        endDate.setPrefSize(100, 25);
        dateBox.getChildren().addAll(startDate, new Label("-"), endDate);
        dateBox.setSpacing(5);

        if (isGraph) {
            genderComboBox1 = genderBox;
            ageComboBox1 = ageBox;
            incomeComboBox1 = incomeBox;
            contextComboBox1 = contextBox;
            datePicker1 = startDate;
            datePicker2 = endDate;
        } else {
            genderComboBox2 = genderBox;
            ageComboBox2 = ageBox;
            incomeComboBox2 = incomeBox;
            contextComboBox2 = contextBox;
            datePicker3 = startDate;
            datePicker4 = endDate;
        }
        VBox filterBox = new VBox(5, filterTitle, genderBox, ageBox, incomeBox, contextBox, dateBox);
        filterBox.setAlignment(Pos.CENTER);
        return filterBox;
    }

    private void handleCompareButton(Stage compareStage) {
        String metric= metricDropdown.getValue();
        String gender1 = genderComboBox1.getValue();
        String age1 = ageComboBox1.getValue();
        String income1 = incomeComboBox1.getValue();
        String context1 = contextComboBox1.getValue();
        String startDate1 = (datePicker1.getValue() != null) ? datePicker1.getValue().toString() : null;
        String endDate1 = (datePicker2.getValue() != null) ? datePicker2.getValue().toString() : null;

        String gender2 = genderComboBox2.getValue();
        String age2 = ageComboBox2.getValue();
        String income2 = incomeComboBox2.getValue();
        String context2 = contextComboBox2.getValue();
        String startDate2 = (datePicker3.getValue() != null) ? datePicker3.getValue().toString() : null;
        String endDate2 = (datePicker4.getValue() != null) ? datePicker4.getValue().toString() : null;

        CompareGraphsResultView resultView = new CompareGraphsResultView( primaryStage, uiController,metric, gender1, age1, income1, context1, startDate1, endDate1,
                gender2, age2, income2, context2, startDate2, endDate2);
        resultView.show();
        compareStage.close();
    }

}