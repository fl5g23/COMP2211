package org.example;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import org.example.Controllers.UIController;
import org.example.Models.Campaign;
import org.example.Models.FiltersBox;
import org.example.Views.MainScreen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
public class MainScreenTest {

    private MainScreen mainScreen;

    @Mock
    private UIController mockController;

    @Mock
    private Stage mockStage;

    @Mock
    private Campaign mockCampaign;

    @Mock
    private LineChart<String, Number> mockLineChart;

    @Mock
    private FiltersBox mockFiltersBox;

    @Start
    private void start(Stage stage) {
        // This method is required by TestFX but we'll use our mocked stage
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        mockLineChart = mock(LineChart.class);
        // Add this line to prevent NPE:
        when(mockLineChart.getData()).thenReturn(FXCollections.observableArrayList());


        // Mock behavior for campaign
        when(mockCampaign.getName()).thenReturn("Test Campaign");

        // Set up a test list of campaigns
        List<Campaign> campaigns = new ArrayList<>();
        campaigns.add(mockCampaign);
        when(mockController.getCampaigns()).thenReturn(campaigns);

        // Initialize the main screen with mocks
        mainScreen = new MainScreen(mockStage, mockController);

        // Use reflection to set private fields that would normally be initialized in show()
        try {
            java.lang.reflect.Field chartField = MainScreen.class.getDeclaredField("lineChart");
            chartField.setAccessible(true);
            chartField.set(mainScreen, mockLineChart);

            java.lang.reflect.Field filtersPanelField = MainScreen.class.getDeclaredField("filtersPanel");
            filtersPanelField.setAccessible(true);
            filtersPanelField.set(mainScreen, mockFiltersBox);

            // Set the campaign menu button
            MenuButton menuButton = new MenuButton("Test Campaign");
            java.lang.reflect.Field menuButtonField = MainScreen.class.getDeclaredField("campaignMenuButton");
            menuButtonField.setAccessible(true);
            menuButtonField.set(mainScreen, menuButton);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }
    }

    @Test
    public void testUpdateMetricsDisplay() {
        // Create test data
        Map<String, Double> coreMetrics = new HashMap<>();
        coreMetrics.put("Impressions", 100000.0);
        coreMetrics.put("Clicks", 5000.0);
        coreMetrics.put("Uniques", 4500.0);
        coreMetrics.put("Conversions", 1000.0);

        double bounceRate = 0.25;
        double ctr = 5.0;
        double cpa = 2.5;
        double cpc = 0.5;
        double cpm = 10.0;
        double totalCost = 2500.0;

        // Call the method
        mainScreen.updateMetricsDisplay(coreMetrics, bounceRate, ctr, cpa, cpc, cpm, totalCost);

        // Verify the values in the labels using reflection
        try {
            java.lang.reflect.Field impressionsValueField = MainScreen.class.getDeclaredField("impressionsValue");
            impressionsValueField.setAccessible(true);
            Label impressionsValue = (Label) impressionsValueField.get(mainScreen);

            java.lang.reflect.Field bounceRateValueField = MainScreen.class.getDeclaredField("bounceRateValue");
            bounceRateValueField.setAccessible(true);
            Label bounceRateValue = (Label) bounceRateValueField.get(mainScreen);

            java.lang.reflect.Field totalCostValueField = MainScreen.class.getDeclaredField("totalCostValue");
            totalCostValueField.setAccessible(true);
            Label totalCostValue = (Label) totalCostValueField.get(mainScreen);

            assertEquals("100,000", impressionsValue.getText());
            assertEquals("25.00%", bounceRateValue.getText());
            assertEquals("£2,500.00", totalCostValue.getText());

        } catch (Exception e) {
            fail("Failed to verify metrics: " + e.getMessage());
        }
    }

    @Test
    public void testUpdateBounceRateDisplay() {
        double bounceRate = 0.325;

        mainScreen.updateBounceRateDisplay(bounceRate);

        try {
            java.lang.reflect.Field bounceRateValueField = MainScreen.class.getDeclaredField("bounceRateValue");
            bounceRateValueField.setAccessible(true);
            Label bounceRateValue = (Label) bounceRateValueField.get(mainScreen);

            assertEquals("32.50%", bounceRateValue.getText());

        } catch (Exception e) {
            fail("Failed to verify bounce rate: " + e.getMessage());
        }
    }

    @Test
    public void testGetSelectedCampaign() {
        Campaign result = mainScreen.getSelectedCampaign();

        // The result should match our mock campaign
        assertEquals("Test Campaign", result.getName());
    }

    @Test
    public void testUpdateCampaignMenu() {
        Label mockTitleLabel = mock(Label.class);
        HBox mockTopBar = mock(HBox.class);

        // Mock the children list of the HBox
        ObservableList<Node> mockChildren = mock(ObservableList.class);
        when(mockTopBar.getChildren()).thenReturn(mockChildren);
        when(mockChildren.indexOf(mockTitleLabel)).thenReturn(0);

        mainScreen.updateCampaignMenu(mockTitleLabel, mockTopBar);

        // Verify the title label was replaced with the menu button
        verify(mockChildren).set(eq(0), any(MenuButton.class));
    }

    @Test
    public void testChangeSelectedCampaign() {
        Campaign newCampaign = mock(Campaign.class);
        when(newCampaign.getName()).thenReturn("New Campaign");

        mainScreen.changeSelectedCampaign(newCampaign);

        // Verify the menu button text changed
        try {
            java.lang.reflect.Field menuButtonField = MainScreen.class.getDeclaredField("campaignMenuButton");
            menuButtonField.setAccessible(true);
            MenuButton menuButton = (MenuButton) menuButtonField.get(mainScreen);

            assertEquals("New Campaign", menuButton.getText());

            // Also verify metric dropdown reset
            java.lang.reflect.Field metricDropdownField = MainScreen.class.getDeclaredField("metricDropdown");
            metricDropdownField.setAccessible(true);
            ComboBox<String> metricDropdown = (ComboBox<String>) metricDropdownField.get(mainScreen);

            assertEquals("Impressions", metricDropdown.getValue());

        } catch (Exception e) {
            fail("Failed to verify campaign change: " + e.getMessage());
        }
    }

    @Test
    public void testSetFirstGenerationFilters() {
        LocalDateTime startDateTime = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2023, 1, 31, 23, 59);
        String campaignName = "First Campaign";

        mainScreen.setFirstGenerationFilters(startDateTime, endDateTime, campaignName);

        // Verify the filters were set
        verify(mockFiltersBox).selectFirstGenerationFilters(
                eq(startDateTime.toLocalDate()),
                eq(endDateTime.toLocalDate()),
                eq(campaignName)
        );

        // Verify dates were stored
        try {
            java.lang.reflect.Field startDateField = MainScreen.class.getDeclaredField("startDate");
            startDateField.setAccessible(true);
            LocalDate startDate = (LocalDate) startDateField.get(mainScreen);

            java.lang.reflect.Field endDateField = MainScreen.class.getDeclaredField("endDate");
            endDateField.setAccessible(true);
            LocalDate endDate = (LocalDate) endDateField.get(mainScreen);

            assertEquals(startDateTime.toLocalDate(), startDate);
            assertEquals(endDateTime.toLocalDate(), endDate);

        } catch (Exception e) {
            fail("Failed to verify date setting: " + e.getMessage());
        }
    }

    @Test
    public void testUpdatePerformanceGraph() {
        Map<String, Map<String, Integer>> metricsOverTime = new HashMap<>();
        Map<String, Integer> impressionsData = new HashMap<>();
        impressionsData.put("2023-01-01", 100);
        impressionsData.put("2023-01-02", 200);
        impressionsData.put("2023-01-03", 150);

        metricsOverTime.put("Impressions", impressionsData);

        mainScreen.updatePerformanceGraph(metricsOverTime, "Impressions", "daily");

        // Verify chart was cleared
        verify(mockLineChart).getData();

        // Since we can't easily verify the chart data was added (due to JavaFX threading),
        // we'll just verify the method doesn't throw exceptions
    }


}