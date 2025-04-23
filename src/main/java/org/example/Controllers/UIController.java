package org.example.Controllers;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Models.Campaign;
import org.example.Models.FiltersBox;
import org.example.Views.AddCampaignView;
import org.example.Views.AuthoriseUsersView;
import org.example.Views.LoginPage;
import org.example.Views.MainScreen;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller class for the Ad Campaign Dashboard application.
 * Follows MVC pattern by mediating between the views and the data controller.
 */
public class UIController {
    private final MainScreen mainScreen;
    public final DataController dataController;
    private List<Campaign> campaigns;
    private Campaign currentCampaign;
    private final Stage primaryStage;
  Map<String, Map<String, Integer>> metricsOverTime;

    /**
     * Constructor initializing the controller with required components.
     *
     * @param primaryStage the main application window
     */
    public UIController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.dataController = new DataController();
        this.campaigns = new ArrayList<>();
        this.currentCampaign = new Campaign("", new File(""), new File(""), new File(""));
        this.mainScreen = new MainScreen(primaryStage, this);
    }

    /**
     * Shows the main screen of the application.
     */
    public void showMainScreen(String role) {
        mainScreen.show(role);
    }

    /**
     * Opens the dialog to add a new campaign.
     *
     * @param titleLabel  the label to be replaced by menu button
     * @param rootContainer the container holding the label
     */
    public void openAddCampaignDialog(Label titleLabel, HBox rootContainer) {
        AddCampaignView campaignView = new AddCampaignView(primaryStage, this);
        campaignView.openAddCampaignDialog(titleLabel, rootContainer);
    }

    public void openAuthoriseUsersPage(){
        AuthoriseUsersView authoriseUsersView = new AuthoriseUsersView(primaryStage, this);
        authoriseUsersView.show();
    }

    /**
     * Adds a new campaign to the list of campaigns.
     *
     * @param campaign the campaign to add
     */
    public void addCampaign(Campaign campaign) {
        campaigns.add(campaign);
        dataController.setupCampaignData(campaign);
    }


    public boolean saveCampaign(String campaignName, File impressionLogFile, File clicksLogFile,
                                File serverLogFile, boolean impression_log_flag, boolean click_log_flag,
                                boolean server_log_flag, Label titleLabel, HBox rootContainer) {

        boolean campaign_name_flag = isValidCampaignName(campaignName);

        if (impression_log_flag && click_log_flag && server_log_flag && campaign_name_flag) {
            if (!campaignName.isEmpty()) {
                // Create campaign object
                Campaign newCampaign = new Campaign(
                        campaignName,
                        impressionLogFile.getAbsoluteFile(),
                        clicksLogFile.getAbsoluteFile(),
                        serverLogFile.getAbsoluteFile());

                // Update model
                addCampaign(newCampaign);

                // Update views
                updateCampaignMenu(titleLabel, rootContainer);
                selectCampaign(newCampaign);

                return true;
            } else {
                showAlert(null, "Files");
            }
        }
        return false;
    }


    /**
     * Updates the campaign menu in the UI.
     *
     * @param titleLabel the label to be replaced
     * @param topBar the container of the label
     */
    public void updateCampaignMenu(Label titleLabel, HBox topBar) {
        mainScreen.updateCampaignMenu(titleLabel, topBar);
    }

    /**
     * Handles campaign selection from the menu.
     *
     * @param campaign       the selected campaign
     */
    public void selectCampaign(Campaign campaign) {
        if (!currentCampaign.equals(campaign)) {
            currentCampaign = campaign;
            updateCampaignName(currentCampaign);
            dataController.resetFiltersSQL();
            updateStatistics(campaign.getName());
            FiltersBox filtersMap = new FiltersBox(null,campaign.getName(), null, null, 0, 0);

            filtersMap.selectFirstGenerationFilters();
            generateGraph(filtersMap);

            LocalDateTime startdate = dataController.getCalculator().getCampaignStartDate(campaign.getName());
            LocalDateTime enddate = dataController.getCalculator().getCampaignEndDate(campaign.getName());

      mainScreen.setFirstGenerationFilters(startdate, enddate, filtersMap.getCampaignName());
}}


    /**
     * Updates the campaign menu box to show the selected box
     *
     * @param campaign the selected campaign
     */
    public void updateCampaignName(Campaign campaign){
        mainScreen.changeSelectedCampaign(campaign);
    }

    /**
     * Updates statistics for the selected campaign.
     *
     * @param campaignName the name of the campaign
     */
    public void updateStatistics(String campaignName) {
        Map<String, Double> coreMetrics = dataController.getCoreMetrics(campaignName);
        double bounceRate = dataController.calculateBounceRate(campaignName).getOrDefault("Page Rate", 0.0);
        double ctr = dataController.calculateCTR(campaignName);
        double cpa = dataController.calculateCPA(campaignName);
        double cpc = dataController.calculateCPC(campaignName);
        double cpm = dataController.calculateCPM(campaignName);
        double totalCost = dataController.calculateTotalCost(campaignName);

        mainScreen.updateMetricsDisplay(coreMetrics, bounceRate, ctr, cpa, cpc, cpm, totalCost);
    }

  public void queryStatistics(
          FiltersBox filtersMap) {

    String gender = filtersMap.getGender();
    String age = filtersMap.getAge();
    String income = filtersMap.getIncome();
    String context = filtersMap.getContext();
    String campaignName = filtersMap.getCampaignName();
    LocalDateTime startDateTime = filtersMap.getStartDate();
    LocalDateTime endDateTime = filtersMap.getEndDate();
    Boolean wholeCampaign = false;

    LocalDateTime campaignStartDate = dataController.getCalculator().getCampaignStartDate(campaignName);
    LocalDateTime campaignEndDate = dataController.getCalculator().getCampaignEndDate(campaignName);

    if (campaignEndDate.equals(endDateTime)&&campaignStartDate.equals(startDateTime)){
        wholeCampaign = true;
    }

    String startDateTimeStr = startDateTime.toString().replace("T", " ");
    String endDateTimeStr = endDateTime.toString().replace("T", " ");

    ArrayList<String> sqlStatements = new ArrayList<>();

    String[] tablePrefixes = {"", "c", "s"};
    for (String table : tablePrefixes) {
        String sqlAppend = "";
        String prefix = "";
        String dateIdentifier = "DATE";

        if (!table.equals("")){
            prefix = "u.";
        }
        if (table.equals("s")){
            dateIdentifier = "Entry_Date";
        }

      if (gender != null && gender != "All") {
        sqlAppend += String.format(" %sGender = '%s' ", prefix, gender);
      }
      if (age != null && age != "All" ) {
        sqlAppend += String.format("~ %sAge = '%s' ", prefix, age);
      }
      if (income != null && income != "All") {
        sqlAppend += String.format("~ %sIncome = '%s' ", prefix, income);
      }
      if (context != null && context != "All") {
        sqlAppend += String.format("~ %sContext = '%s' ", prefix, context);
      }

      if (!wholeCampaign){
        sqlAppend +=
            String.format(
                "~ %s BETWEEN '%s' AND '%s'", dateIdentifier, startDateTimeStr, endDateTimeStr);
      }

      // Replace "~" with "AND" and remove the trailing "AND"
      sqlAppend = sqlAppend.replace("~", "AND");

      // Check for the trailing "AND" and remove it if present
      if (!sqlAppend.startsWith("AND")&&!sqlAppend.equals("")) {
        sqlAppend = "AND " + sqlAppend;
      }
      sqlStatements.add(sqlAppend);
    }

    dataController.primeForQueries(sqlStatements);
  }

    /**
     * Updates the bounce rate based on the selected definition.
     *
     * @param campaignName the name of the campaign
     * @param bounceType the type of bounce rate to calculate
     */
    public void updateBounceRate(String campaignName, String bounceType) {
        double bounceRate;
        if (bounceType.equals("PageLeft")) {
            bounceRate = dataController.calculateBounceRate(campaignName).getOrDefault("Page Rate", 0.0);
        } else {
            bounceRate = dataController.calculateBounceRate(campaignName).getOrDefault("Single Rate", 0.0);
        }
        mainScreen.updateBounceRateDisplay(bounceRate);
    }




  /**
   * Generates the performance graph for the selected campaign.
   *
   * @param filterSettings the name of the campaign
   */
  public void generateGraph(FiltersBox filterSettings) {
    metricsOverTime = dataController.getMetricsOverTime(filterSettings);
    mainScreen.updatePerformanceGraph(metricsOverTime, filterSettings.getMetric(), filterSettings.getGranularity());
  }

//

  /**
         * Updates the histogram display.
         *
         * @param isClickByCost flag indicating the type of histogram
         */
  public void updateHistogram(FiltersBox filters, boolean isClickByCost) {
    if (isClickByCost) {
      List<Double> clickCosts = dataController.getCostsList(filters);
      mainScreen.updateClickCostHistogram(clickCosts);
    } else {
      Map<String, Integer> clicksByDate = dataController.getClicksOverTime(filters);
      mainScreen.updateClickTimeHistogram(clicksByDate);
    }
  }


    /**
     * Checks if a file has the correct format for a specific log type.
     *
     * @param file the file to check
     * @param logType the type of log
     * @return true if the file is valid, false otherwise
     */
    public boolean checkFileFormat(File file, String logType) {
        List<String> data = dataController.getCSVStructure(file.getAbsolutePath());

        switch (logType) {
            case "Impression":
                List<String> impressionFields = List.of("Date", "ID", "Gender", "Age", "Income", "Context", "Impression Cost");
                return data.equals(impressionFields);
            case "Click":
                List<String> clickFields = List.of("Date", "ID", "Click Cost");
                return data.equals(clickFields);
            case "Server":
                List<String> serverFields = List.of("Entry Date", "ID", "Exit Date", "Pages Viewed", "Conversion");
                return data.equals(serverFields);
            default:
                return false;
        }
    }

    /**
     * Checks if a campaign name already exists.
     *
     * @param campaignName the name to check
     * @return true if the name is valid (doesn't exist), false otherwise
     */
    public boolean isValidCampaignName(String campaignName) {
        return !dataController.isCampaignExists(campaignName);
    }

    /**
     * Shows an alert for various error conditions.
     *
     * @param file the file that caused the error (can be null)
     * @param type the type of error
     */
    public void showAlert(File file, String type) {
        mainScreen.showAlert(file, type);
    }

    /**
     * Returns the list of campaigns.
     *
     * @return the list of campaigns
     */
    public List<Campaign> getCampaigns() {
        return campaigns;
    }

    /**
     * Handles the logout process.
     */
    public void logout() {
        campaigns.clear();
        currentCampaign = new Campaign("", new File(""), new File(""), new File(""));
        // You would create a new LoginPage or handle the transition
        LoginPage loginPage = new LoginPage(primaryStage);
        closeAppActions();
        loginPage.show(primaryStage);

    }

    public void addUser(String username, String password){
        dataController.addUser(username,password);
    }

    public void setup(){
        dataController.setupDatabase();
    }

    public ArrayList<Object> userExists(String username, String password){
        return dataController.userExists(username,password);
    }

    public ArrayList<String> getUnauthorisedUsers(){
        return dataController.getUnauthorisedUsers();
    }

    public void authoriseUser(String username, String role){
        dataController.authoriseUser(username, role);
    }

    public void deleteUser(String username){
        dataController.deleteUser(username);
    }

    public void closeAppActions(){
        dataController.closeAppActions();
    }

  public Map<String, String> extractFilterSummary(FiltersBox filters) {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("Gender", nullableToDisplay(filters.getGender()));
    map.put("Age", nullableToDisplay(filters.getAge()));
    map.put("Income", nullableToDisplay(filters.getIncome()));
    map.put("Context", nullableToDisplay(filters.getContext()));
    map.put("Bounce Type", filters.getBounceValue());
    map.put("Date Range", filters.getStartDate() + " to " + filters.getEndDate());
    map.put("time granularity ", filters.getGranularity());
    map.put("Metric", filters.getMetric());

    return map;
  }

  private String nullableToDisplay(String value) {
    return (value == null || value.equals("null")) ? "All" : value;
  }
  public Map<String, String> extractMetrics(String campaignName) {
    Map<String, String> metrics = new LinkedHashMap<>();
    Map<String, Double> core = dataController.getCoreMetrics(campaignName);

    metrics.put("Impressions", String.valueOf(core.getOrDefault("Impressions", 0.0)));
    metrics.put("Clicks", String.valueOf(core.getOrDefault("Clicks", 0.0)));
    metrics.put("Uniques", String.valueOf(core.getOrDefault("Uniques", 0.0)));
    metrics.put("Bounces", String.valueOf(core.getOrDefault("Bounces", 0.0)));
    metrics.put("Conversions", String.valueOf(core.getOrDefault("Conversions", 0.0)));

    metrics.put("Bounce Rate", String.format("%.2f%%", dataController.calculateBounceRate(campaignName).getOrDefault("Page Rate", 0.0)));
    metrics.put("CTR", String.format("%.2f%%", dataController.calculateCTR(campaignName)));
    metrics.put("CPA", String.format("%.2f", dataController.calculateCPA(campaignName)));
    metrics.put("CPC", String.format("%.2f", dataController.calculateCPC(campaignName)));
    metrics.put("CPM", String.format("%.2f", dataController.calculateCPM(campaignName)));
    metrics.put("Total Cost", String.format("%.2f", dataController.calculateTotalCost(campaignName)));

    return metrics;
  }

  public void exportTimeSeriesAsCSV(Map<String, Map<String, Integer>> fullMetrics, String campaignName) {
    try {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Save Metrics Over Time");
      fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV File", "*.csv"));
      File file = fileChooser.showSaveDialog(null);
      if (file == null) return;

      FileWriter writer = new FileWriter(file);

      // Header
      writer.write("Campaign:," + campaignName + "\n\n");

      // Determine all metrics and all unique dates
      Set<String> allDates = new TreeSet<>();
      List<String> metricNames = new ArrayList<>(fullMetrics.keySet());

      for (Map<String, Integer> metricData : fullMetrics.values()) {
        allDates.addAll(metricData.keySet());
      }

      // Write column headers
      writer.write("Date");
      for (String metric : metricNames) {
        writer.write("," + metric);
      }
      writer.write("\n");

      // Write rows per date
      for (String date : allDates) {
        writer.write(date);
        for (String metric : metricNames) {
          Map<String, Integer> values = fullMetrics.get(metric);
          Integer val = values.getOrDefault(date, 0);
          writer.write("," + val);
        }
        writer.write("\n");
      }

      writer.close();
      System.out.println(" CSV export completed: " + file.getAbsolutePath());

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Map<String, Map<String, Integer>> getAllMetricsOverTime(FiltersBox filters) {
    return dataController.getAllMetricsOverTime(filters); // full data for CSV
  }


  public Map<String, Integer> getClickByTimeData(FiltersBox filters) {
    return dataController.getClicksOverTime(filters);
  }
  public List<Double> getClickCostData(FiltersBox filters) {
    return dataController.getCostsList(filters);
  }

}