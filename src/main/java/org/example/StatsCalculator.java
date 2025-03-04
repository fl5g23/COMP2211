package org.example;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.sql.*;
import java.util.Map;

public class StatsCalculator {

  /**
   * Returns key metrics:
   * Number of Impressions, Clicks, Uniques, Conversions
   * Bounces are in calculateBounceRate()
   *
   */
  public Map<String, Double> getCoreMetrics() {
    Map<String, Double> coreMetrics = new HashMap<>();

    // Modified SQL query to exclude bounces
    String coreMetricsSQL =
            "SELECT COUNT(*) FROM impressions UNION ALL " +
                    "SELECT COUNT(*) FROM clicks UNION ALL " +
                    "SELECT COUNT(DISTINCT ID) FROM clicks UNION ALL " +
                    "SELECT COUNT(*) FROM server WHERE conversion = 'Yes'";

    try (ResultSet rs = executeSQL(coreMetricsSQL)) {
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
  public double calculateTotalCost() {

    double totalImpressionCost = 0;
    double totalClickCost = 0;
    double totalCost = 0;

    // SQL queries to get total costs
    String totalImpressionsSQL = "SELECT SUM(impression_cost) FROM impressions";
    String totalClicksSQL = "SELECT SUM(click_cost) FROM clicks";

    try {
      // Execute SQL queries
      ResultSet rs1 = executeSQL(totalImpressionsSQL);
      if (rs1 != null && rs1.next()) {
        totalImpressionCost = rs1.getDouble(1);
        System.out.println("Total impression cost: " + totalImpressionCost);
      }

      ResultSet rs2 = executeSQL(totalClicksSQL);
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
    return totalCost;}

  /**
   * Returns click-through rate -> total clicks / total impressions x 100
   */
  public double calculateCTR() {
    double clickThroughRate = 0;
    String clickThroughRateSQL =
            "SELECT CASE "
                    + "    WHEN (SELECT COUNT(Impression_Cost) FROM Impressions WHERE Campaign = 1) = 0 "
                    + "    THEN 0  "
                    + "    ELSE (SELECT COUNT(Click_Cost) FROM Clicks WHERE Campaign = 1) * 1.0 / "
                    + "         (SELECT COUNT(Impression_Cost) FROM Impressions WHERE Campaign = 1) "
                    + "END AS CTR";

    try {
      ResultSet rs = executeSQL(clickThroughRateSQL);
      if (rs != null && rs.next()) {
        clickThroughRate = rs.getDouble("CTR") * 100; // Convert to percentage
      }
      if (rs != null) rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    System.out.println("Click Through Rate: " + clickThroughRate + " %");
    return clickThroughRate;
  }

  /**
   * Returns cost per click -> click cost / number of clicks
   */
  public double calculateCPC() {
    double costPerClick = 0;

    String cpcSQL = "SELECT SUM(click_cost) / NULLIF(COUNT(click_cost), 0) FROM clicks WHERE Campaign = 1";

    try {
      ResultSet rs = executeSQL(cpcSQL);
      if (rs != null && rs.next()) {
        costPerClick = rs.getDouble(1);
        System.out.println("Cost per Click: " + costPerClick);
      }
      if (rs != null) rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return costPerClick;
  }

  /**
   * Returns cost per acquisition -> total cost / number of conversions
   */
  public double calculateCPA() {
    double CPA = 0;

    String cpaSQL =
            "SELECT \n"
                    + "    ( (SELECT SUM(Impression_Cost) FROM Impressions WHERE Campaign = 1) + \n"
                    + "      (SELECT SUM(Click_Cost) FROM Clicks WHERE Campaign = 1) ) / \n"
                    + "    NULLIF((SELECT COUNT(*) FROM Server WHERE Conversion = 'Yes' AND Campaign = 1), 0) \n"
                    + "    AS CPA;";

    try {
      ResultSet rs = executeSQL(cpaSQL);
      if (rs != null && rs.next()) {
        CPA = rs.getDouble(1);
        System.out.println("Cost per Acquisition: " + CPA);
      }
      if (rs != null) rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return CPA;
  }

  /**
   * Returns cost per 1000 impressions -> total cost x 1000 / number of impressions
   */
  public double calculateCPM() {
    double CPM = 0;
    String cpmSQL = "SELECT ( (SELECT SUM(impression_cost) FROM Impressions WHERE Campaign = 1) + " +
            " (SELECT SUM(click_cost) FROM Clicks WHERE Campaign = 1) ) * 1000 / " +
            " (SELECT COUNT(*) FROM Impressions WHERE Campaign = 1);";

    try {
      ResultSet rs = executeSQL(cpmSQL);
      if (rs != null && rs.next()) {
        CPM = rs.getDouble(1);
        System.out.println("Cost per Thousand Impressions: " + CPM);
      }
      if (rs != null) rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return CPM;
  }

  /**
   * Returns bounce rate and number of bounces as a Map
   * keys:
   * Single - Number of Single Page View Bounces
   * Page - Number of Page Left Bounces
   * Single Rate - Bounce Rate for the Single Page View Bounces
   * Page Rate - Bounce Rate for Page Left Bounces
   */
  public Map<String, Double> calculateBounceRate() {
    double singlePageBounces = 0;
    double pageleftBounces = 0;
    double totalClicks = 1; // Avoid division by zero
    Map<String, Double> bouncesMap = new HashMap<>();

    String singlePageBouncesSQL = "SELECT COUNT(*) FROM Server WHERE Pages_Viewed = 1 AND Campaign = 1";
    String timeSpentSQL = "SELECT COUNT(*) FROM Server WHERE Exit_Date != 'n/a' AND Conversion = 'No' AND Campaign = 1";
    String totalClicksSQL = "SELECT COUNT(click_cost) FROM Clicks";

    try {
      ResultSet rs1 = executeSQL(singlePageBouncesSQL);
      if (rs1 != null && rs1.next()) {
        singlePageBounces = rs1.getDouble(1);
        bouncesMap.put("Single", singlePageBounces);
        System.out.println("Single Page Bounces: " + singlePageBounces);
      }
      if (rs1 != null) rs1.close();

      ResultSet rs2 = executeSQL(timeSpentSQL);
      if (rs2 != null && rs2.next()) {
        pageleftBounces = rs2.getDouble(1);
        bouncesMap.put("Page", pageleftBounces);
        System.out.println("Page left bounces: " + pageleftBounces);
      }
      if (rs2 != null) rs2.close();

      ResultSet rs3 = executeSQL(totalClicksSQL);
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
  public List<Double> getCostsList() {
    String costsListSQL = "SELECT Click_Cost FROM Clicks ORDER BY Click_Cost ASC;";
    List<Double> costsList = new ArrayList<>();

    ResultSet rs = executeSQL(costsListSQL);
    try {
      while (rs != null && rs.next()) {
        costsList.add(rs.getDouble("Click_Cost"));
      }
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return costsList;
  }

  /**
   * Executes SQL Statements on the database
   * ResultSet - set of results of sql statement
   */
  public ResultSet executeSQL(String sqlstmt) {
    try {
      Connection conn = DriverManager.getConnection("jdbc:sqlite:mainData.db");
      Statement stmt = conn.createStatement();
      return stmt.executeQuery(sqlstmt);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  //testing
  public static void main (String[] args){
    StatsCalculator stats = new StatsCalculator();
    stats.getCoreMetrics();
    stats.calculateTotalCost();
    stats.calculateCTR();
    stats.calculateCPC();
    stats.calculateCPA();
    stats.calculateCPM();
    stats.calculateBounceRate();
    stats.getCostsList();

  }
}
