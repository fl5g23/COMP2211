package org.example.Models;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.*;



public class importFilestoDatabase {

    /**
     * Creates the database maindata.db + tables for Impressions, Clicks, Server files
     *
     *
     */
    public void createDatabases() {
        String url = "jdbc:sqlite:mainData.db";

        try (var conn = DriverManager.getConnection(url)) {
            var meta = conn.getMetaData();
            System.out.println("The driver name is " + meta.getDriverName());
            System.out.println("A new database has been created.");
            var stmt = conn.createStatement();
            stmt.execute(sqlcreateClicks);
            stmt.execute(sqlcreateImpressions);
            stmt.execute(sqlcreateServer);


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
        final int BATCH_SIZE = 1_000;  // Process in chunks (tune this if needed)
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
                } else if (table.equals("Server")) {
                    importServer(record, pstmt);
                } else if (table.equals("Clicks")) {
                    importClicks(record, pstmt);
                }

                pstmt.addBatch();
                numberProcessed++;

                // Execute batch periodically
                if (numberProcessed % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                    conn.commit(); // Commit changes
                }
            }

            // Ensure final batch is executed
            pstmt.executeBatch();
            conn.commit();

            System.out.println("Data inserted successfully into table: " + table);

        } catch (SQLException e) {
            throw new RuntimeException("Error while inserting data into the " + table + " table", e);
        }
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

    public static void main (String[] args){
        importFilestoDatabase object = new importFilestoDatabase();
        object.createDatabases();
        var hi = object.getCSVData("src/main/resources/testCSV/server_log2month.csv"); //change to any of the other ones
        object.insertDataintoTables("LOL","Server",hi);

  }
}
