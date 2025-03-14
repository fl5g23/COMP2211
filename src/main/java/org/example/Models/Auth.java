package org.example.Models;



import java.sql.*;
import java.util.ArrayList;


public class Auth {


    String sqlcreateUsers =
            "CREATE TABLE IF NOT EXISTS Users (\n"
                    + "    Username TEXT PRIMARY KEY, \n"
                    + "    Password DATETIME,\n"
                    + "    Authorised INT, \n"
                    + "    Role TEXT \n"
                    + ");";

    String sqlinitialAdmin =
            "INSERT OR IGNORE INTO Users (Username, Password, Authorised, Role) VALUES ('admin', 'admin', 1, 'Admin')";

    public void setup(){
        String url = "jdbc:sqlite:mainData.db";
        try (var conn = DriverManager.getConnection(url)) {
            var stmt = conn.createStatement();
            stmt.execute(sqlcreateUsers);
            stmt.execute(sqlinitialAdmin);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void addUser(String username, String password) {
        final String url = "jdbc:sqlite:mainData.db";
        final String sql = "INSERT OR IGNORE INTO Users (Username, Password, Authorised, Role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false); // Start transaction

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, "0"); // Default to non-authorised
            pstmt.setString(4, "User");

            pstmt.executeUpdate(); // Execute insert

            conn.commit(); // Commit transaction

            System.out.println("User " + username + " added successfully.");

        } catch (SQLException e) {
            throw new RuntimeException("Error inserting user into Users table", e);
        }
    }



    /**
     * Checks if a user exists, validates password, and gets authorization status and role
     * @param username The username to check
     * @param password The password to validate
     * @return ArrayList containing [usernameExists, passwordCorrect, authorised, role]
     */
    public ArrayList<Object> userExists(String username, String password) {
        String usernameExistsSQL = "SELECT COUNT(*) FROM Users WHERE Username = ?";
        String passwordCorrectSQL = "SELECT COUNT(*) FROM Users WHERE Username = ? AND Password = ?";
        String userAuthorisedSQL = "SELECT COUNT(*) FROM Users WHERE Username = ? AND Password = ? AND Authorised = 1";
        String roleSQL = "SELECT Role FROM Users WHERE Username = ?";

        boolean usernameExists = false;
        boolean passwordCorrect = false;
        boolean authorised = false;
        String role = "";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:mainData.db")) {
            // Check if the username exists
            try (PreparedStatement stmt = conn.prepareStatement(usernameExistsSQL)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        usernameExists = true;
                    }
                }
            }

            // If the username exists, check if the password is correct
            if (usernameExists) {
                try (PreparedStatement stmt = conn.prepareStatement(passwordCorrectSQL)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            passwordCorrect = true;
                        }
                    }
                }
            }

            // If the password is correct, check if the user is authorised
            if (passwordCorrect) {
                try (PreparedStatement stmt = conn.prepareStatement(userAuthorisedSQL)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            authorised = true;
                        }
                    }
                }

                // Get the user's role
                try (PreparedStatement stmt = conn.prepareStatement(roleSQL)) {
                    stmt.setString(1, username);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            role = rs.getString("Role");
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        ArrayList<Object> toReturn = new ArrayList<>();
        toReturn.add(usernameExists);
        toReturn.add(passwordCorrect);
        toReturn.add(authorised);
        toReturn.add(role);

        return toReturn;
    }

    /**
     * Authorizes a user with a specific role
     * @param username The username to authorize
     * @param role The role to assign ("user" or "admin")
     */
    public void authoriseUser(String username, String role) {
        String authoriseUserSQL = "UPDATE Users SET Authorised = 1, Role = ? WHERE Username = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:mainData.db");
             PreparedStatement stmt = conn.prepareStatement(authoriseUserSQL)) {

            // Fix parameter order: first parameter is role, second is username
            stmt.setString(1, role);
            stmt.setString(2, username);

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("User " + username + " authorised successfully as " + role + ".");
            } else {
                System.out.println("User " + username + " not found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteUser(String username) {
        String deleteUserSQL = "DELETE FROM Users WHERE Username = ?;";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:mainData.db");
             PreparedStatement stmt = conn.prepareStatement(deleteUserSQL)) {

            stmt.setString(1, username);
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("User " + username + " deleted successfully.");
            } else {
                System.out.println("User " + username + " not found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public ArrayList<String> getUnauthorisedUsers() {
        ArrayList<String> unauthorisedUsers = new ArrayList<>();
        String query = "SELECT Username FROM Users WHERE Authorised = 0";

        // Connect to the database and execute the query
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:mainData.db");
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            // Loop through the result set and add users to the list
            while (rs.next()) {
                String username = rs.getString("Username");
                unauthorisedUsers.add(username);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return unauthorisedUsers; // Return the list of unauthorised users
    }

    public static void main(String[] args){
        Auth test = new Auth();
        test.authoriseUser("flynnblaw", "Admin");
        System.out.println(test.getUnauthorisedUsers());
    }


}
