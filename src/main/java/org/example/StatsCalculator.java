package org.example;

import java.sql.*;

public class StatsCalculator {

  //  Total Cost Calculation
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

  //  Click-Through Rate (CTR) = (Clicks / Impressions) * 100
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

  //  Cost-Per-Click (CPC) = Total Click Cost / Clicks
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

  //  Cost-Per-Acquisition (CPA) = Total Cost / Conversions
  public static double calculateCPA() {
    double totalCost = 0;
    int conversions = 0;

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
        System.out.println("Cost per Aquisition: " + totalCost); // Get the SUM(impression_cost)
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

// Extract total cost and conversion count

    return conversions == 0 ? 0 : totalCost / conversions;
  }

  //  Cost-Per-Thousand Impressions (CPM) = (Total Impression Cost * 1000) / Impressions
  public static double calculateCPM() {
    double totalImpressionCost = 0;
    int impressions = 0;

//    for (...) {
//      totalImpressionCost += ...;  // Extract impression cost
//      impressions++;
//    }

    return impressions == 0 ? 0 : (totalImpressionCost * 1000) / impressions;
  }

  //  Bounce Rate = (Bounces / Clicks)
//  bounce Rate Calculation (User Chooses Definition)
  public static double calculateBounceRate() {
    int bounces = 0;
    int clicks = 0;

//    // Loop through server logs to count bounces
//    for (...) {
//      boolean isBounce = false;
//
//      // Option 1: Page-Based Bounce (Default)
//      if (... == 1) {  // Condition for a bounce (only 1 page viewed)
//        isBounce = true;
//      }
//
//      // Option 2: Time-Based Bounce (If we have session duration)
//      if (... < 5) {  // User spent less than 5 seconds (example threshold)
//        isBounce = true;
//      }
//
//      if (isBounce) {
//        bounces++;
//      }
    //}

    return clicks == 0 ? 0 : ((double) bounces / clicks);
  }

  public static void main (String[] args){
    StatsCalculator stats = new StatsCalculator();
    stats.calculateTotalCost();
    stats.calculateCTR();
    stats.calculateCPC();
    stats.calculateCPA();

  }
}

