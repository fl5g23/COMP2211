package org.example;
import java.io.File;
import java.util.*;
import java.sql.*;

public class StatsCalculator {

  importFilestoDatabase importer = new importFilestoDatabase();

  public void setup(Campaign campaign, File impression_log, File click_log, File server_log){
    importer.createDatabases();
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
    return totalCost;
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
    return clickThroughRate;
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

    return costPerClick;
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

    return CPA;
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

      // Execute and return the result set
      return stmt.executeQuery();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Returns true/false depending on if campaign exists
   */
  public boolean isCampaignExists(Campaign campaign) {
    String query = "SELECT COUNT(*) FROM Impressions WHERE Campaign = ?";
    List<String> parameters = List.of(campaign.getName());

    try (ResultSet rs = executeSQL(query, parameters)) {
      if (rs != null && rs.next()) {
        return rs.getInt(1) > 0; // If count > 0, campaign exists
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return false; // Default to false if something goes wrong
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
    System.out.println(stats.isCampaignExists(new Campaign("!",new File(""),new File(""),new File(""))));
  }
}
