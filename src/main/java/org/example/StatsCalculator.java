package org.example;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.sql.*;
import java.util.Map;

public class StatsCalculator {

  /**
   * Returns total (impression + click) cost
   */
  public static double calculateTotalCost() {
    double totalImpressionCost = 0;
    double totalClickCost = 0;

    String url = "jdbc:sqlite:mainData.db";
    String totalImpressionsSQL = "SELECT SUM(impression_cost) FROM impressions";
    String totalClicksSQL = "SELECT SUM(click_cost) FROM clicks";

    try (Connection conn = DriverManager.getConnection(url);
         Statement stmt = conn.createStatement()) {

      // Get total impression cost
      ResultSet rs1 = stmt.executeQuery(totalImpressionsSQL);
      if (rs1.next()) {
        totalImpressionCost = rs1.getDouble(1);
        System.out.println("Total impression cost: " + totalImpressionCost); // Get the SUM(impression_cost)
      }

      // Get total click cost
      ResultSet rs2 = stmt.executeQuery(totalClicksSQL);
      if (rs2.next()) {
        totalClickCost = rs2.getDouble(1);  // Get the SUM(click_cost)
        System.out.println("Total click cost: " + totalClickCost);
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return totalImpressionCost + totalClickCost;
  }

  /**
   * Returns click-through rate -> total clicks / total impressions x 100
   */
  public static double calculateCTR() {
    double clickThroughRate = 0;
    String url = "jdbc:sqlite:mainData.db";
    String clickThroughRateSQL =
            "SELECT CASE "
                    + "    WHEN (SELECT COUNT(Impression_Cost) FROM Impressions WHERE Campaign = 1) = 0 "
                    + "    THEN 0  "
                    + "    ELSE (SELECT COUNT(Click_Cost) FROM Clicks WHERE Campaign = 1) * 1.0 / "
                    + "         (SELECT COUNT(Impression_Cost) FROM Impressions WHERE Campaign = 1) "
                    + "END AS CTR";

    try (Connection conn = DriverManager.getConnection(url);
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(clickThroughRateSQL)) {

      if (rs.next()) {
        clickThroughRate = rs.getDouble("CTR")*100;  // Use alias instead of column index
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    System.out.println("CLick through rate :" + clickThroughRate);
    return clickThroughRate;
  }

  /**
   * Returns cost per click -> click cost / number of clicks
   */
  public static double calculateCPC() {
    double costPerClick = 0;

    String url = "jdbc:sqlite:mainData.db";
    String totalClicksSQL = "SELECT SUM(click_cost)/COUNT(click_cost) FROM clicks";

    try (Connection conn = DriverManager.getConnection(url);
         Statement stmt = conn.createStatement()) {

      // Get total impression cost
      ResultSet rs1 = stmt.executeQuery(totalClicksSQL);
      if (rs1.next()) {
        costPerClick = rs1.getDouble(1);
        System.out.println("Cost per Click: " + costPerClick); // Get the SUM(impression_cost)
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return costPerClick;
  }

  /**
   * Returns cost per acquisition -> total cost / number of conversions
   */
  public static double calculateCPA() {
    double totalCost = 0;

    String url = "jdbc:sqlite:mainData.db";
    String totalClicksSQL =
        "SELECT \n"
            + "    ( (SELECT SUM(Impression_Cost) FROM Impressions WHERE Campaign = 1) + \n"
            + "      (SELECT SUM(Click_Cost) FROM Clicks WHERE Campaign = 1) ) / \n"
            + "    (SELECT COUNT(*) FROM Server WHERE Conversion = 'Yes' AND Campaign = 1) \n"
            + "    AS CPA;";

    try (Connection conn = DriverManager.getConnection(url);
         Statement stmt = conn.createStatement()) {

      // Get total impression cost
      ResultSet rs1 = stmt.executeQuery(totalClicksSQL);
      if (rs1.next()) {
        totalCost = rs1.getDouble(1);
        System.out.println("Cost per Acquisition: " + totalCost); // Get the SUM(impression_cost)
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }


    return totalCost;
  }

  /**
   * Returns cost per 1000 impressions -> total cost x 1000 / number of impressions
   */
  public static double calculateCPM() {
    double CPM = 0;
    String url = "jdbc:sqlite:mainData.db";
    String cpmSQL = "SELECT ((SELECT SUM(impression_cost) FROM impressions WHERE Campaign = 1) + (SELECT SUM(click_cost) FROM clicks WHERE Campaign = 1) *1000)/ COUNT(*) FROM Impressions WHERE Campaign = 1;";

    try (Connection conn = DriverManager.getConnection(url);
         Statement stmt = conn.createStatement()) {

      // Get total impression cost
      ResultSet rs1 = stmt.executeQuery(cpmSQL);
      if (rs1.next()) {
        CPM = rs1.getDouble(1);
        System.out.println("Cost per Thousand Impressions: " + CPM); // Get the SUM(impression_cost)
      }

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
  public static Map<String, Double> calculateBounceRate() {
    double singlePageBounces = 0;
    double pageleftBounces = 0;
    double totalClicks;
    Map<String, Double> bouncesMap = new HashMap<>();


    String url = "jdbc:sqlite:mainData.db";
    String singlepageBouncesSQL = "SELECT COUNT(*) FROM Server WHERE Pages_Viewed = 1 AND Campaign = 1";
    String timeSpentSQL = "SELECT COUNT(*) FROM Server WHERE Exit_Date != 'n/a' AND Conversion = 'No' AND Campaign = 1";
    String totalClicksSQL = "SELECT COUNT(click_cost) FROM Clicks";

    try (Connection conn = DriverManager.getConnection(url);
        Statement stmt = conn.createStatement()) {

      // Get total impression cost
      ResultSet rs1 = stmt.executeQuery(singlepageBouncesSQL);
      if (rs1.next()) {
        singlePageBounces = rs1.getInt(1);
        System.out.println("Single Page Bounces: " + singlePageBounces); // Get the SUM(impression_cost)
        bouncesMap.put("Single", singlePageBounces);
      }

      // Get total click cost
      ResultSet rs2 = stmt.executeQuery(timeSpentSQL);
      if (rs2.next()) {
        pageleftBounces = rs2.getInt(1);  // Get the SUM(click_cost)
        System.out.println("Page left bounces: " + pageleftBounces);
        bouncesMap.put("Page", pageleftBounces);
      }

      ResultSet rs3 = stmt.executeQuery(totalClicksSQL);
      if (rs3.next()) {
        totalClicks = rs3.getDouble(1);  // Get the SUM(click_cost)
        double singlePageBounceRate = singlePageBounces / totalClicks;
        double pageleftBounceRate = pageleftBounces / totalClicks;
        bouncesMap.put("Single Rate", singlePageBounceRate);
        bouncesMap.put("Page Rate", pageleftBounceRate);
        System.out.println("Single Page Bounce Rate: " + singlePageBounceRate);
        System.out.println("Page left Bounce Rate: " + pageleftBounceRate);
      }

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
    String url = "jdbc:sqlite:mainData.db";
    String costsListSQL = "SELECT Click_Cost FROM Clicks ORDER BY Click_Cost ASC;";
    List<Double> costsList = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(url);
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(costsListSQL)) {

      // Iterate through the result set and add costs to the list
      while (rs.next()) {
        costsList.add(rs.getDouble("Click_Cost"));
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    // Print or return the list (modify as needed)
    return costsList;
  }

  //testing
  public static void main (String[] args){
    StatsCalculator stats = new StatsCalculator();
    stats.calculateTotalCost();
    stats.calculateCTR();
    stats.calculateCPC();
    stats.calculateCPA();
    stats.calculateCPM();
    stats.calculateBounceRate();
    stats.getCostsList();
  }
}

