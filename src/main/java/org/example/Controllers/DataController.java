package org.example.Controllers;

import javafx.util.Pair;
import org.example.Models.Auth;
import org.example.Models.Campaign;
import org.example.Models.StatsCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller specifically for handling data operations.
 * Acts as an intermediary between the UI controller and the StatsCalculator.
 */
public class DataController {
    private final StatsCalculator statsCalculator;
    private final Auth authenticator;

    public DataController() {
        this.statsCalculator = new StatsCalculator();
        this.authenticator = new Auth();
    }

    /**
     * Sets up campaign data in the StatsCalculator.
     */
    public void setupCampaignData(Campaign campaign) {
        if (!isCampaignExists(campaign.getName())) {
            statsCalculator.addData(
                campaign,
                campaign.getImpressionLogFile(),
                campaign.getClicksLogFile(),
                campaign.getServerLogFile()
            );
        }
    }

    /**
     * Checks if a campaign exists.
     */
    public boolean isCampaignExists(String campaignName) {
        return statsCalculator.isCampaignExists(campaignName);
    }

    /**
     * Gets core metrics for a campaign.
     */
    public Map<String, Double> getCoreMetrics(String campaignName) {
        return statsCalculator.getCoreMetrics(campaignName);
    }

    /**
     * Calculates bounce rate for a campaign.
     */
    public Map<String, Double> calculateBounceRate(String campaignName) {
        return statsCalculator.calculateBounceRate(campaignName);
    }

    /**
     * Calculates CTR for a campaign.
     */
    public double calculateCTR(String campaignName) {
        return statsCalculator.calculateCTR(campaignName);
    }

    /**
     * Calculates CPA for a campaign.
     */
    public double calculateCPA(String campaignName) {
        return statsCalculator.calculateCPA(campaignName);
    }

    /**
     * Calculates CPC for a campaign.
     */
    public double calculateCPC(String campaignName) {
        return statsCalculator.calculateCPC(campaignName);
    }

    /**
     * Calculates CPM for a campaign.
     */
    public double calculateCPM(String campaignName) {
        return statsCalculator.calculateCPM(campaignName);
    }

    /**
     * Calculates total cost for a campaign.
     */
    public double calculateTotalCost(String campaignName) {
        return statsCalculator.calculateTotalCost(campaignName);
    }

    /**
     * Gets metrics over time for a campaign.
     */
    public Map<String, Map<String, Integer>> getMetricsOverTime(String campaignName, String bounceType) {
        return statsCalculator.getMetricsOverTime(campaignName, bounceType);
    }

    /**
     * Gets costs list for a campaign.
     */
    public List<Double> getCostsList(String campaignName) {
        return statsCalculator.getCostsList(campaignName);
    }

    /**
     * Gets clicks over time for a campaign.
     */
    public Map<String, Integer> getClicksOverTime(String campaignName) {
        return statsCalculator.getClicksOverTime(campaignName);
    }

    /**
     * Gets CSV structure for a file.
     */
    public List<String> getCSVStructure(String filePath) {
        return statsCalculator.getCSVStructure(filePath);
    }

    /**
     * Gets the StatsCalculator instance.
     */
    public StatsCalculator getCalculator() {
        return statsCalculator;
    }

    public void addUser(String username, String password){
        authenticator.addUser(username, password);
    }

    public void setupDatabase(){
        statsCalculator.setup();
        authenticator.setup();
    }

    public ArrayList<Object> userExists(String username, String password){
        return authenticator.userExists(username, password);
    }

    public ArrayList<String> getUnauthorisedUsers(){
        return authenticator.getUnauthorisedUsers();
    }

    public void authoriseUser(String username, String role){
        authenticator.authoriseUser(username, role);
    }

    public void deleteUser(String username){
        authenticator.deleteUser(username);
    }

    public void closeAppActions(){
        statsCalculator.closeAppActions();
    }
}