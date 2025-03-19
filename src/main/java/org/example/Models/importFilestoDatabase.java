package org.example.Models;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.*;



public class importFilestoDatabase {

    /**
     * Creates the database maindata.db + tables for Impressions, Clicks, Server files
     * and a new UserProfiles table for unique user demographic data
     */
    public void createDataTables() {
        String url = "jdbc:sqlite:mainData.db";

        try (var conn = DriverManager.getConnection(url)) {
            var meta = conn.getMetaData();
            System.out.println("The driver name is " + meta.getDriverName());
            System.out.println("A new database has been created.");
            var stmt = conn.createStatement();
            stmt.execute(sqlcreateClicks);
            stmt.execute(sqlcreateImpressions);
            stmt.execute(sqlcreateServer);
            stmt.execute(sqlcreateUserProfiles);


        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Gets the contents of csv file passed in, returns List of Lists containing data
     * String pathtofile = path to the loG
     */
    public List<List<String>> getCSVData(String pathtofile){
        try (Stream<String> lines = Files.lines(Paths.get(pathtofile))) {
            List<List<String>> records = lines.map(line -> Arrays.asList(line.split(",")))
                    .collect(Collectors.toList());
            records.remove(0);
            return records;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getCSVStructure(String pathtofile) {
        try (Stream<String> lines = Files.lines(Paths.get(pathtofile))) {
            // Read only the first line, split by commas, and return as a list
            return lines
                    .findFirst()  // Get the first line
                    .map(line -> Arrays.asList(line.split(",")))  // Split by commas and convert to List
                    .orElse(null);  // Return null if the file is empty
        } catch (IOException e) {
            // Handle any IO exceptions
            throw new RuntimeException("Error reading CSV file: " + pathtofile, e);
        }
    }

    /**
     * Inserts data into each table (database tables must exist)
     * campaignNumber -> default 1, will be more when we compare graphs
     * table -> either "Impressions", "Server" or "Clicks" based on table needed to input data
     * data -> data returned from getCSVData
     */
    public void insertDataintoTables(String campaignName, String table, List<List<String>> data) {
        final String url = "jdbc:sqlite:mainData.db";
        final String sql;
        final int BATCH_SIZE = 100_000;
        int numberProcessed = 0;

        // SQL Insert Statements for each table
        if (table.equals("Impressions")) {
            sql = "INSERT OR IGNORE INTO Impressions (Campaign, Date, ID, Gender, Age, Income, Context, Impression_Cost) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        } else if (table.equals("Server")) {
            sql = "INSERT OR IGNORE INTO Server (Campaign, Entry_Date, ID, Exit_Date, Pages_Viewed, Conversion) VALUES (?, ?, ?, ?, ?, ?)";
        } else if (table.equals("Clicks")) {
            sql = "INSERT OR IGNORE INTO Clicks (Campaign, Date, ID, Click_Cost) VALUES (?, ?, ?, ?)";
        } else {
            throw new IllegalStateException("Unexpected table name: " + table);
        }

        // Database connection and transaction
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);  // Enable transaction for performance


            for (List<String> record : data) {
                pstmt.setString(1, campaignName); // Set Campaign

                if (table.equals("Impressions")) {
                    importImpressions(record, pstmt);
                    pstmt.addBatch();
                } else if (table.equals("Server")) {
                    importServer(record, pstmt);
                    pstmt.addBatch();
                } else if (table.equals("Clicks")) {
                    importClicks(record, pstmt);
                    pstmt.addBatch();
                }

                numberProcessed++;

                // Execute batch periodically
                if (numberProcessed % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
//                    conn.commit(); // Commit changes
                }
            }

//             Ensure final batch is executed
            pstmt.executeBatch();
            conn.commit();

            System.out.println("Data inserted successfully into table: " + table);

        } catch (SQLException e) {
            throw new RuntimeException("Error while inserting data into the " + table + " table", e);
        }
    }

  public void insertDataUserProfiles(String campaignName, List<List<String>> data) {
    final String url = "jdbc:sqlite:mainData.db";
    final String sql =
        "INSERT OR IGNORE INTO UserProfiles (Campaign, ID, Gender, Age, Income, Context) VALUES (?, ?, ?, ?, ?, ?)";
    final int BATCH_SIZE = 100_000; // Increased batch size
    int numberProcessed = 0;

    // Create a HashSet just to track IDs we've seen - much lighter weight than storing whole
    // records
    Set<String> processedIds = new HashSet<>();

    try (Connection conn = DriverManager.getConnection(url);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      conn.setAutoCommit(false);
      for (List<String> record : data) {
        String userId = record.get(1);

        if (processedIds.add(
            userId)) { // add returns true if the set did not already contain the ID
          importUserProfiles(campaignName, record, pstmt, userId);
          pstmt.addBatch();
          numberProcessed++;

          // Execute batch periodically
          if (numberProcessed % BATCH_SIZE == 0) {
            pstmt.executeBatch();

          }
        }
      }
      pstmt.executeBatch();
      conn.commit();
      System.out.println("Total unique users inserted: " + numberProcessed);
    } catch (SQLException e) {
      throw new RuntimeException("Error while inserting data into the UserProfiles table", e);
    }
  }

    private static void importUserProfiles(String campaignName, List<String> record, PreparedStatement pstmt, String userId) throws SQLException {
        pstmt.setString(1, campaignName);
        pstmt.setLong(2, Long.parseLong(userId));
        pstmt.setString(3, record.get(2)); // Gender
        pstmt.setString(4, record.get(3)); // Age
        pstmt.setString(5, record.get(4)); // Income
        pstmt.setString(6, record.get(5)); // Context
    }


    private static void importClicks(List<String> record, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(2, record.get(0)); // Set Date
        pstmt.setLong(3, Long.parseLong(record.get(1)));
        pstmt.setDouble(4, Double.parseDouble(record.get(2))); // Set Click_Cost
    }

    private static void importServer(List<String> record, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(2, record.get(0)); // Set Entry_Date
        pstmt.setLong(3, Long.parseLong(record.get(1))); // Set ID as long
        pstmt.setString(4, record.get(2)); // Set Exit_Date
        pstmt.setInt(5, Integer.parseInt(record.get(3))); // Set Pages_Viewed
        pstmt.setString(6, record.get(4)); // Set Conversion (e.g., "Yes" or "No")
    }

    private static void importImpressions(List<String> record, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(2, record.get(0)); // Set Date (e.g., "2015-01-14 11:59:54")
        pstmt.setLong(3, Long.parseLong(record.get(1))); // Set ID as long
        pstmt.setString(4, record.get(2)); // Set Gender (e.g., "Female")
        pstmt.setString(5, record.get(3)); // Set Age (e.g., "25-34")
        pstmt.setString(6, record.get(4)); // Set Income (e.g., "High")
        pstmt.setString(7, record.get(5)); // Set Context (e.g., "Shopping")
        pstmt.setDouble(8, Double.parseDouble(record.get(6)));  // Set Impression_Cost
    }


    String sqlcreateImpressions =
            "CREATE TABLE IF NOT EXISTS Impressions (\n"
                    + "    Campaign TEXT, \n"
                    + "    Date DATETIME,\n"
                    + "    ID BIGINT, \n"
                    + "    Gender TEXT, \n"
                    + "    Age TEXT, \n"
                    + "    Income TEXT, \n"
                    + "    Context TEXT,\n"
                    + "    Impression_Cost REAL\n"
                    + ");";

    String sqlcreateServer = "CREATE TABLE IF NOT EXISTS Server (\n" +
            "    Campaign TEXT, \n" +
            "    Entry_Date DATETIME,\n" +
            "    ID BIGINT,\n" +
            "    Exit_Date DATETIME,\n" +
            "    Pages_Viewed INTEGER CHECK (Pages_Viewed >= 0),\n" +
            "    Conversion TEXT CHECK (Conversion IN ('Yes', 'No'))\n" +
            ");";

    String sqlcreateClicks = "CREATE TABLE IF NOT EXISTS Clicks (\n" +
            "    Campaign TEXT, \n" +
            "    Date DATETIME,\n" +
            "    ID BIGINT,\n" +
            "    Click_Cost REAL CHECK (Click_Cost >= 0)\n" +
            ");";

    // New table for unique user profiles with demographic data
    String sqlcreateUserProfiles = "CREATE TABLE IF NOT EXISTS UserProfiles (\n" +
            "    Campaign TEXT, \n" +
            "    ID BIGINT, \n" +
            "    Gender TEXT, \n" +
            "    Age TEXT, \n" +
            "    Income TEXT, \n" +
            "    Context TEXT\n" +
            ");";

    public static void main (String[] args){
        importFilestoDatabase object = new importFilestoDatabase();
        object.createDataTables();
        var hi = object.getCSVData("src/main/resources/testCSV/impression_log2month.csv"); //change to any of the other ones




        LocalTime userStart = LocalTime.now();
        object.insertDataUserProfiles("Try6", hi);
        LocalTime userEnd = LocalTime.now();


        LocalTime start = LocalTime.now();
        object.insertDataintoTables("Try6", "Impressions", hi);
        LocalTime end = LocalTime.now();

        System.out.println("Impressions time: " + start.until(end, ChronoUnit.SECONDS));
        System.out.println("User profiles time: " + userStart.until(userEnd, ChronoUnit.SECONDS));





        // Populate user profiles from existing impression data if needed
        // object.populateUserProfiles("LOL");
    }
}