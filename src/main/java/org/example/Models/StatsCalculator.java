package org.example.Models;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.sql.*;

public class StatsCalculator {

  importFilestoDatabase importer = new importFilestoDatabase();

  public void setup(){
    importer.createDataTables();
  }


  public void addData(Campaign campaign, File impression_log, File click_log, File server_log){
    var impressionData = importer.getCSVData(impression_log.getAbsolutePath());
    var clickData = importer.getCSVData(click_log.getAbsolutePath());
    var serverData = importer.getCSVData(server_log.getAbsolutePath());
    importer.insertDataintoTables(campaign.getName(), "Impressions", impressionData);
    importer.insertDataintoTables(campaign.getName(), "Clicks", clickData);
    importer.insertDataintoTables(campaign.getName(), "Server", serverData);
  }




  /**
   * Returns key metrics:
   * Number of Impressions, Clicks, Uniques, Conversions
   * Bounces are in calculateBounceRate()
   *
   */
  public Map<String, Double> getCoreMetrics(String campaignname) {
    Map<String, Double> coreMetrics = new HashMap<>();

    // SQL query with placeholders
    String coreMetricsSQL =
            "SELECT COUNT(*) FROM impressions WHERE Campaign = ? UNION ALL " +
                    "SELECT COUNT(*) FROM clicks WHERE Campaign = ? UNION ALL " +
                    "SELECT COUNT(DISTINCT ID) FROM clicks WHERE Campaign = ? UNION ALL " +
                    "SELECT COUNT(*) FROM server WHERE conversion = 'Yes' AND Campaign = ?";

    // Create a list of parameters to bind to the SQL query
    List<String> parameters = Arrays.asList(campaignname, campaignname, campaignname, campaignname);

    // Call the executeSQL method with the SQL query and parameters
    try (ResultSet rs = executeSQL(coreMetricsSQL, parameters)) {
      if (rs != null) {
        // Process each result row and assign to the respective metric
        if (rs.next()) {
          coreMetrics.put("Impressions", rs.getDouble(1));
          System.out.println("Number of impressions: " + rs.getDouble(1));
        }
        if (rs.next()) {
          coreMetrics.put("Clicks", rs.getDouble(1));
          System.out.println("Number of clicks: " + rs.getDouble(1));
        }
        if (rs.next()) {
          coreMetrics.put("Uniques", rs.getDouble(1));
          System.out.println("Number of uniques: " + rs.getDouble(1));
        }
        if (rs.next()) {
          coreMetrics.put("Conversions", rs.getDouble(1));
          System.out.println("Number of conversions: " + rs.getDouble(1));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return coreMetrics;
  }

  /**
   * Returns total (impression + click) cost
   */
  public double calculateTotalCost(String campaignname) {
    double totalImpressionCost = 0;
    double totalClickCost = 0;
    double totalCost = 0;

    // SQL queries to get total costs
    String totalImpressionsSQL = "SELECT SUM(impression_cost) FROM impressions WHERE Campaign = ?";
    String totalClicksSQL = "SELECT SUM(click_cost) FROM clicks WHERE Campaign = ?";

    // Create a list of parameters to bind to the SQL queries
    List<String> parameters = Arrays.asList(campaignname);

    try {
      // Execute SQL queries
      ResultSet rs1 = executeSQL(totalImpressionsSQL, parameters);
      if (rs1 != null && rs1.next()) {
        totalImpressionCost = rs1.getDouble(1);
        System.out.println("Total impression cost: " + totalImpressionCost);
      }

      ResultSet rs2 = executeSQL(totalClicksSQL, parameters);
      if (rs2 != null && rs2.next()) {
        totalClickCost = rs2.getDouble(1);
        System.out.println("Total click cost: " + totalClickCost);
      }

      // Close ResultSets
      if (rs1 != null) rs1.close();
      if (rs2 != null) rs2.close();

    } catch (SQLException e) {
      e.printStackTrace();
    }

    totalCost = totalImpressionCost + totalClickCost;
    return totalCost/100;
  }

  /**
   * Returns click-through rate -> total clicks / total impressions x 100
   */
  public double calculateCTR(String campaignname) {
    double clickThroughRate = 0;

    // SQL query updated to use campaignname as a parameter
    String clickThroughRateSQL =
            "SELECT CASE "
                    + "    WHEN (SELECT COUNT(Impression_Cost) FROM Impressions WHERE Campaign = ?) = 0 "
                    + "    THEN 0  "
                    + "    ELSE (SELECT COUNT(Click_Cost) FROM Clicks WHERE Campaign = ?) * 1.0 / "
                    + "         (SELECT COUNT(Impression_Cost) FROM Impressions WHERE Campaign = ?) "
                    + "END AS CTR";

    // Create a list of parameters to bind to the SQL query
    List<String> parameters = Arrays.asList(campaignname, campaignname, campaignname);

    try {
      // Execute SQL query with the campaignname parameter
      ResultSet rs = executeSQL(clickThroughRateSQL, parameters);
      if (rs != null && rs.next()) {
        clickThroughRate = rs.getDouble("CTR") * 100; // Convert to percentage
      }

      // Close ResultSet
      if (rs != null) rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    System.out.println("Click Through Rate: " + clickThroughRate + " %");
    return clickThroughRate/100;
  }

  /**
   * Returns cost per click -> click cost / number of clicks
   */
  public double calculateCPC(String campaignname) {
    double costPerClick = 0;

    // Updated SQL query to use campaignname as a parameter
    String cpcSQL = "SELECT SUM(click_cost) / NULLIF(COUNT(click_cost), 0) FROM clicks WHERE Campaign = ?";

    try {
      // Execute SQL query with the campaignname parameter
      ResultSet rs = executeSQL(cpcSQL, Arrays.asList(campaignname));
      if (rs != null && rs.next()) {
        costPerClick = rs.getDouble(1);
        System.out.println("Cost per Click: " + costPerClick);
      }

      // Close ResultSet
      if (rs != null) rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return costPerClick/100;
  }

  /**
   * Returns cost per acquisition -> total cost / number of conversions
   */
  public double calculateCPA(String campaignname) {
    double CPA = 0;

    // Updated SQL query to use campaignname as a parameter
    String cpaSQL =
            "SELECT \n"
                    + "    ( (SELECT SUM(Impression_Cost) FROM Impressions WHERE Campaign = ?) + \n"
                    + "      (SELECT SUM(Click_Cost) FROM Clicks WHERE Campaign = ?) ) / \n"
                    + "    NULLIF((SELECT COUNT(*) FROM Server WHERE Conversion = 'Yes' AND Campaign = ?), 0) \n"
                    + "    AS CPA;";

    try {
      // Execute SQL query with the campaignname parameter
      ResultSet rs = executeSQL(cpaSQL, Arrays.asList(campaignname, campaignname, campaignname));
      if (rs != null && rs.next()) {
        CPA = rs.getDouble(1);
        System.out.println("Cost per Acquisition: " + CPA);
      }

      // Close ResultSet
      if (rs != null) rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return CPA/100;
  }

  /**
   * Returns cost per 1000 impressions -> total cost x 1000 / number of impressions
   */
  public double calculateCPM(String campaignname) {
    double CPM = 0;

    // Updated SQL query to use campaignname as a parameter
    String cpmSQL = "SELECT ( (SELECT SUM(impression_cost) FROM Impressions WHERE Campaign = ?) + " +
            " (SELECT SUM(click_cost) FROM Clicks WHERE Campaign = ?) ) * 1000 / " +
            " (SELECT COUNT(*) FROM Impressions WHERE Campaign = ?);";

    try {
      // Execute SQL query with the campaignname parameter
      ResultSet rs = executeSQL(cpmSQL, Arrays.asList(campaignname, campaignname, campaignname));
      if (rs != null && rs.next()) {
        CPM = rs.getDouble(1);
        System.out.println("Cost per Thousand Impressions: " + CPM);
      }

      // Close ResultSet
      if (rs != null) rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return CPM/100;
  }

  /**
   * Returns bounce rate and number of bounces as a Map
   * keys:
   * Single - Number of Single Page View Bounces
   * Page - Number of Page Left Bounces
   * Single Rate - Bounce Rate for the Single Page View Bounces
   * Page Rate - Bounce Rate for Page Left Bounces
   */
  public Map<String, Double> calculateBounceRate(String campaignname) {
    double singlePageBounces = 0;
    double pageleftBounces = 0;
    double totalClicks = 1; // Avoid division by zero
    Map<String, Double> bouncesMap = new HashMap<>();

    // Updated SQL queries to use campaignname as a parameter
    String singlePageBouncesSQL = "SELECT COUNT(*) FROM Server WHERE Pages_Viewed = 1 AND Campaign = ?";
    String timeSpentSQL = "SELECT COUNT(*) FROM Server WHERE Exit_Date != 'n/a' AND Conversion = 'No' AND Campaign = ?";
    String totalClicksSQL = "SELECT COUNT(click_cost) FROM Clicks WHERE Campaign = ?";

    try {
      // Execute SQL queries with the campaignname parameter
      ResultSet rs1 = executeSQL(singlePageBouncesSQL, Arrays.asList(campaignname));
      if (rs1 != null && rs1.next()) {
        singlePageBounces = rs1.getDouble(1);
        bouncesMap.put("Single", singlePageBounces);
        System.out.println("Single Page Bounces: " + singlePageBounces);
      }
      if (rs1 != null) rs1.close();

      ResultSet rs2 = executeSQL(timeSpentSQL, Arrays.asList(campaignname));
      if (rs2 != null && rs2.next()) {
        pageleftBounces = rs2.getDouble(1);
        bouncesMap.put("Page", pageleftBounces);
        System.out.println("Page left bounces: " + pageleftBounces);
      }
      if (rs2 != null) rs2.close();

      ResultSet rs3 = executeSQL(totalClicksSQL, Arrays.asList(campaignname));
      if (rs3 != null && rs3.next()) {
        totalClicks = rs3.getDouble(1);
      }
      if (rs3 != null) rs3.close();

      // Calculate bounce rates
      double singlePageBounceRate = (totalClicks == 0) ? 0 : singlePageBounces / totalClicks;
      double pageleftBounceRate = (totalClicks == 0) ? 0 : pageleftBounces / totalClicks;

      bouncesMap.put("Single Rate", singlePageBounceRate);
      bouncesMap.put("Page Rate", pageleftBounceRate);

      System.out.println("Single Page Bounce Rate: " + singlePageBounceRate);
      System.out.println("Page left Bounce Rate: " + pageleftBounceRate);

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return bouncesMap;
  }

  /**
   * Returns all the costs of clicks in ascending order in a List
   * Used for click cost histogram
   */
  public List<Double> getCostsList(String campaignname) {
    // Updated SQL query to filter by campaignname
    String costsListSQL = "SELECT Click_Cost FROM Clicks WHERE Campaign = ? ORDER BY Click_Cost ASC;";
    List<Double> costsList = new ArrayList<>();

    try {
      // Execute SQL query with campaignname as parameter
      ResultSet rs = executeSQL(costsListSQL, Arrays.asList(campaignname));

      // Process result set
      while (rs != null && rs.next()) {
        costsList.add(rs.getDouble("Click_Cost"));
      }
      if (rs != null) rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return costsList;
  }

  /**
   * Executes SQL Statements on the database
   * ResultSet - set of results of sql statement
   */
  public ResultSet executeSQL(String sqlstmt, List<String> parameters) {
    try {
      // Establish connection
      Connection conn = DriverManager.getConnection("jdbc:sqlite:mainData.db");

      // Prepare the SQL statement
      PreparedStatement stmt = conn.prepareStatement(sqlstmt);

      // Dynamically bind parameters
      for (int i = 0; i < parameters.size(); i++) {
        stmt.setString(i + 1, parameters.get(i)); // PreparedStatement uses 1-based index
      }

      return stmt.executeQuery();
    } catch (SQLException e) {
      e.getMessage();
    }
    return null;
  }

  /**
   * Returns true/false depending on if campaign exists
   */
  public boolean isCampaignExists(String campaignName) {
    String query = "SELECT COUNT(*) FROM Impressions WHERE Campaign = ?";
    List<String> parameters = List.of(campaignName);

    try (ResultSet rs = executeSQL(query, parameters)) {
      if (rs != null && rs.next()) {
        return rs.getInt(1) > 0; // If count > 0, campaign exists
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return false; // Default to false if something goes wrong
  }
  //daily
  public Map<String, Map<String, Integer>> getMetricsOverTime(String campaignName, String bounceType, String selectedGender, String timeGranularity, String selectedMetric) {

    Map<String, Map<String, Integer>> metrics = new TreeMap<>();

    String sql = "";

    // Validate and normalize inputs
    if (selectedGender == null || selectedGender.isEmpty()) {
      selectedGender = "All";
    }

    // Validate time granularity option
    if (!Arrays.asList("daily", "hourly", "weekly").contains(timeGranularity.toLowerCase())) {
      throw new IllegalArgumentException("Invalid time granularity. Must be 'daily', 'hourly', or 'weekly'");
    }

    // Create result map structure

    metrics.put(selectedMetric, new TreeMap<>());
    // Define time format based on granularity
    String timeFormat;
    switch (timeGranularity.toLowerCase()) {
      case "hourly":
        timeFormat = "%Y-%m-%d %H:00";
        break;
      case "daily":
        timeFormat = "%Y-%m-%d";
        break;
      case "weekly":
        // Weekly requires special handling
        return getMetricsWeekly(campaignName, bounceType, selectedGender, selectedMetric);
      default:
        timeFormat = "%Y-%m-%d"; // Default to daily if something goes wrong
    }

    switch (selectedMetric) {
      case "Impressions":
        sql = "SELECT strftime('" + timeFormat + "', Date) AS Time, COUNT(*) FROM Impressions WHERE Campaign = ?";
        break;

      case "Clicks":
        sql = "SELECT strftime('" + timeFormat + "', c.Date) AS Time, COUNT(*) FROM Clicks c JOIN Impressions i ON c.ID = i.ID WHERE i.Campaign = ?";
        break;

      case "Uniques":
        sql = "SELECT strftime('" + timeFormat + "', c.Date) AS Time, COUNT(DISTINCT c.ID) FROM Clicks c JOIN Impressions i ON c.ID = i.ID WHERE i.Campaign = ?";
        break;

      case "Conversions":
        sql = "SELECT strftime('" + timeFormat + "', s.Entry_Date) AS Time, COUNT(*) FROM Server s JOIN Impressions i ON s.ID = i.ID WHERE s.Conversion = 'Yes' AND i.Campaign = ?";
        break;

      case "Bounces":
        if (bounceType.equals("SinglePage")) {
          sql = "SELECT strftime('" + timeFormat + "', s.Entry_Date) AS Time, COUNT(*) FROM Server s JOIN Impressions i ON s.ID = i.ID WHERE s.Pages_Viewed = 1 AND i.Campaign = ?";
        } else if (bounceType.equals("PageLeft")) {
          sql = "SELECT strftime('" + timeFormat + "', s.Entry_Date) AS Time, COUNT(*) FROM Server s JOIN Impressions i ON s.ID = i.ID WHERE s.Exit_Date != 'n/a' AND s.Conversion = 'No' AND i.Campaign = ?";
        } else {
          throw new IllegalArgumentException("Invalid bounce type: " + bounceType);
        }
    }

    // Setup parameters
    List<String> parameters = new ArrayList<>();
    parameters.add(campaignName);

    // Apply gender filtering if needed
    if (!selectedGender.equalsIgnoreCase("All") && selectedMetric.equals("Impressions")) {
      sql += " AND Gender = ?";
      parameters.add(selectedGender);

    }else if (!selectedMetric.equals("Impressions") && !selectedGender.equals("All")){
      String genderCondition = " AND i.Gender = ?";

      sql += genderCondition;

      parameters.add(selectedGender);
    }
    // Group by time
    sql += " GROUP BY Time";

    // Execute queries and populate result maps
    try {
      addDataToMap(metrics.get(selectedMetric), sql, parameters);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return metrics;
  }

  // Keep this method separate for weekly metrics since it requires more complex date calculations
  public Map<String, Map<String, Integer>> getMetricsWeekly(String campaignName, String bounceType, String selectedGender, String selectedMetric) {

    String sql = "";

    String bounceCondition;

    if (selectedGender == null || selectedGender.isEmpty()) {
      selectedGender = "All";
    }

    LocalDate campaignStart = getCampaignStartDate(campaignName);
    if (campaignStart == null) {
      throw new IllegalStateException("Campaign start date not found for " + campaignName);
    }

    LocalDate campaignEnd = getCampaignEndDate(campaignName);
    if (campaignEnd == null) {
      throw new IllegalStateException("Campaign end date not found for " + campaignName);
    }

    Map<String, Map<String, Integer>> weeklyMetrics = new TreeMap<>();
    weeklyMetrics.put(selectedMetric, new TreeMap<>());


    switch (selectedMetric) {
      case "Impressions":
        sql = "SELECT DATE(?, '+' || (CAST((julianday(Date) - julianday(?)) / 7 AS INT) * 7) || ' days') AS WeekStart, COUNT(*) " +
                "FROM Impressions " +
                "WHERE Campaign = ? AND Date BETWEEN ? AND ?";
        break;

      case "Clicks":
        sql = "SELECT DATE(?, '+' || (CAST((julianday(c.Date) - julianday(?)) / 7 AS INT) * 7) || ' days') AS WeekStart, COUNT(*) " +
                "FROM Clicks c JOIN Impressions i ON c.ID = i.ID " +
                "WHERE i.Campaign = ? AND c.Date BETWEEN ? AND ?";
        break;

      case "Uniques":
        sql = "SELECT DATE(?, '+' || (CAST((julianday(c.Date) - julianday(?)) / 7 AS INT) * 7) || ' days') AS WeekStart, COUNT(DISTINCT c.ID) " +
                "FROM Clicks c JOIN Impressions i ON c.ID = i.ID " +
                "WHERE i.Campaign = ? AND c.Date BETWEEN ? AND ?";
        break;

      case "Conversions":
        sql = "SELECT DATE(?, '+' || (CAST((julianday(s.Entry_Date) - julianday(?)) / 7 AS INT) * 7) || ' days') AS WeekStart, COUNT(*) " +
                "FROM Server s JOIN Impressions i ON s.ID = i.ID " +
                "WHERE s.Conversion = 'Yes' AND i.Campaign = ? AND s.Entry_Date BETWEEN ? AND ?";
        break;

      case "Bounces":
        if ("SinglePage".equalsIgnoreCase(bounceType)) {
          bounceCondition = "s.Pages_Viewed = 1";
        } else if ("PageLeft".equalsIgnoreCase(bounceType)) {
          bounceCondition = "s.Exit_Date != 'n/a' AND s.Conversion = 'No'";
        } else {
          throw new IllegalArgumentException("Invalid bounce type: " + bounceType);
        }
        sql = "SELECT DATE(?, '+' || (CAST((julianday(s.Entry_Date) - julianday(?)) / 7 AS INT) * 7) || ' days') AS WeekStart, COUNT(*) " +
                "FROM Server s JOIN Impressions i ON s.ID = i.ID " +
                "WHERE " + bounceCondition + " AND i.Campaign = ? AND s.Entry_Date BETWEEN ? AND ?";
    }

    List<String> impParams = new ArrayList<>();
    impParams.add(campaignStart.toString());
    impParams.add(campaignStart.toString());
    impParams.add(campaignName);
    impParams.add(campaignStart.toString());
    impParams.add(campaignEnd.toString());

    // Apply gender filtering if needed
    if (!selectedGender.equalsIgnoreCase("All") && selectedMetric.equals("Impressions")) {
      sql += " AND Gender = ?";
    }else if (!selectedMetric.equals("Impressions") && !selectedGender.equals("All")){
      String genderCondition = " AND i.Gender = ?";

      sql += genderCondition;

      impParams.add(selectedGender);
    }

    sql += " GROUP BY WeekStart";

    try {
      addWeeklyDataToMap(weeklyMetrics.get(selectedMetric), sql, impParams);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return weeklyMetrics;
  }

  public LocalDate getCampaignStartDate(String campaignName) {
    String sql = "SELECT MIN(Date) FROM Impressions WHERE Campaign = ?";
    LocalDate startDate = null;
    try (ResultSet rs = executeSQL(sql, List.of(campaignName))) {
      if (rs != null && rs.next()) {
        startDate = LocalDate.parse(rs.getString(1).substring(0, 10));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return startDate;
  }

  public LocalDate getCampaignEndDate(String campaignName) {
    String sql = "SELECT MAX(Date) FROM Impressions WHERE Campaign = ?";
    LocalDate endDate = null;
    try (ResultSet rs = executeSQL(sql, List.of(campaignName))) {
      if (rs != null && rs.next()) {
        endDate = LocalDate.parse(rs.getString(1).substring(0, 10));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return endDate;
  }

  // Helper method to execute the query and put results into the provided map.
  private void addWeeklyDataToMap(Map<String, Integer> map, String sql, List<String> parameters) throws SQLException {
    ResultSet rs = executeSQL(sql, parameters);
    while (rs != null && rs.next()) {
      String week = rs.getString("WeekStart");
      int count = rs.getInt(2);
      map.put(week, count);
    }
  }


  // Helper function to fill data maps
  private void addDataToMap(Map<String, Integer> dataMap, String sql, List<String> parameters) throws SQLException {
    try (ResultSet rs = executeSQL(sql, parameters)) {
      while (rs != null && rs.next()) {
        String time = rs.getString(1); // Date (YYYY-MM-DD)
        int count = rs.getInt(2);
        dataMap.put(time, count);
      }
    }
  }

  public List<String> getCSVStructure(String pathtofile){
    return importer.getCSVStructure(pathtofile);
  }



  public Map<String, Integer> getClicksOverTime(String campaignName) {
    Map<String, Integer> clicksOverTime = new TreeMap<>();

    // SQL query to group clicks by day
    String clicksByTimeSQL = "SELECT strftime('%Y-%m-%d', Date) AS Day, COUNT(*) FROM Clicks WHERE Campaign = ? GROUP BY Day ORDER BY Day;";

    try (ResultSet rs = executeSQL(clicksByTimeSQL, List.of(campaignName))) {
      while (rs != null && rs.next()) {
        String day = rs.getString(1);  // Get date as YYYY-MM-DD
        int clickCount = rs.getInt(2);  // Count of clicks on that day
        clicksOverTime.put(day, clickCount);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return clicksOverTime;
  }

  public void closeAppActions() {
    String url = "jdbc:sqlite:mainData.db";

    try (var conn = DriverManager.getConnection(url);
         var stmt = conn.createStatement()) {

      stmt.execute("DROP TABLE IF EXISTS Impressions");
      stmt.execute("DROP TABLE IF EXISTS Clicks");
      stmt.execute("DROP TABLE IF EXISTS Server");

    } catch (SQLException e) {
      System.err.println(e.getMessage());
    }
  }


  //testing
  public static void main (String[] args){
    StatsCalculator stats = new StatsCalculator();
    //    stats.getCoreMetrics("1");
    //    stats.calculateTotalCost("1");
    //    stats.calculateCTR("1");
    //    stats.calculateCPC("1");
    //    stats.calculateCPA("1");
    //    stats.calculateCPM("1");
    //    stats.calculateBounceRate("1");
    //    stats.getCostsList("1");
//    System.out.println(stats.isCampaignExists(new Campaign("!",new File(""),new File(""),new File(""))));
  }
}
