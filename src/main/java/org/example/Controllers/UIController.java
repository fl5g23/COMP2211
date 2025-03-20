package org.example.Controllers;
import java.time.LocalDateTime;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.Models.Campaign;
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
    public void selectCampaign(Campaign campaign ) {
        if (!currentCampaign.equals(campaign)) {
            currentCampaign = campaign;
            updateCampaignName(currentCampaign);
            dataController.resetFiltersSQL();
            updateStatistics(campaign.getName());

            Map<String, String> filtersMap = new HashMap<>();
            filtersMap.put("campaignName", campaign.getName());
            filtersMap.put("bounceDefinition", "PageLeft");
            filtersMap.put("selectedMetric", "Impressions");
            filtersMap.put("Granularity","Daily");
            filtersMap.put("Gender", "All");

            generateGraph(filtersMap);

            LocalDateTime startdate = dataController.getCalculator().getCampaignStartDate(campaign.getName());
            LocalDateTime enddate = dataController.getCalculator().getCampaignEndDate(campaign.getName());

            mainScreen.setFirstGenerationFilters(startdate, enddate);

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
      Map<String, String> filtersMap, LocalDateTime startDateTime, LocalDateTime endDateTime) {

    String gender = filtersMap.get("Gender");
    String age = filtersMap.get("Age");
    String income = filtersMap.get("Income");
    String context = filtersMap.get("Context");
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
      sqlAppend += String.format("~ %s BETWEEN '%s' AND '%s'", dateIdentifier, startDateTimeStr, endDateTimeStr);

      // Replace "~" with "AND" and remove the trailing "AND"
      sqlAppend = sqlAppend.replace("~", "AND");

      // Check for the trailing "AND" and remove it if present
      if (!sqlAppend.startsWith("AND")) {
        sqlAppend = "AND " + sqlAppend;
      }
      sqlStatements.add(sqlAppend);
    }

    dataController.primeForQueries(sqlStatements);
    String campaignName = filtersMap.get("campaignName");
    updateStatistics(campaignName);
    generateGraph(filtersMap);
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
  public void generateGraph(Map<String,String> filterSettings) {
    Map<String, Map<String, Integer>> metricsOverTime;
    metricsOverTime = dataController.getMetricsOverTime(filterSettings);
    mainScreen.updatePerformanceGraph(metricsOverTime, filterSettings.get("selectedMetric"), filterSettings.get("Granularity"));
  }

//

  /**
         * Updates the histogram display.
         *
         * @param campaignName the name of the campaign
         * @param isClickByCost flag indicating the type of histogram
         */
    public void updateHistogram(String campaignName, boolean isClickByCost) {
        if (isClickByCost) {
            List<Double> clickCosts = dataController.getCostsList(campaignName);
            mainScreen.updateClickCostHistogram(clickCosts);
        } else {
            Map<String, Integer> clicksByDate = dataController.getClicksOverTime(campaignName);
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



}