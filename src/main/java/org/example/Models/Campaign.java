package org.example.Models;

import java.io.File;

public class Campaign {
    private String name;
    private File impressionLogFile;
    private File clicksLogFile;
    private File serverLogFile;

    public Campaign(String name, File impressionLogFile, File clicksLogFile, File serverLogFile) {
        this.name = name;
        this.impressionLogFile = impressionLogFile;
        this.clicksLogFile = clicksLogFile;
        this.serverLogFile = serverLogFile;
    }

    public String getName() {
        return name;
    }

    public File getImpressionLogFile() {
        return impressionLogFile;
    }

    public File getClicksLogFile() {
        return clicksLogFile;
    }

    public File getServerLogFile() {
        return serverLogFile;
    }

    @Override
    public String toString() {
        return name; // Used for displaying in the MenuButton
    }
}