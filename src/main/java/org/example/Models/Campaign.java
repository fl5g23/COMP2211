package org.example.Models;

import java.io.File;

/**
 * Represents an advertising campaign with associated log files.
 * Stores campaign name and paths to impression, click, and server log files.
 */
public class Campaign {
    private String name;
    private File impressionLogFile;
    private File clicksLogFile;
    private File serverLogFile;


    /**
     * Constructs a new Campaign with specified name and log files.
     *
     * @param name The name of the campaign
     * @param impressionLogFile The file containing impression logs
     * @param clicksLogFile The file containing click logs
     * @param serverLogFile The file containing server logs
     */
    public Campaign(String name, File impressionLogFile, File clicksLogFile, File serverLogFile) {
        this.name = name;
        this.impressionLogFile = impressionLogFile;
        this.clicksLogFile = clicksLogFile;
        this.serverLogFile = serverLogFile;
    }

    /**
     * Gets the campaign name.
     * @return The name of the campaign
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the impression log file.
     * @return The file containing impression data
     */
    public File getImpressionLogFile() {
        return impressionLogFile;
    }


    /**
     * Gets the clicks log file.
     * @return The file containing click data
     */
    public File getClicksLogFile() {
        return clicksLogFile;
    }

    /**
     * Gets the server log file.
     * @return The file containing server data
     */
    public File getServerLogFile() {
        return serverLogFile;
    }

    @Override
    public String toString() {
        return name; // Used for displaying in the MenuButton
    }
}