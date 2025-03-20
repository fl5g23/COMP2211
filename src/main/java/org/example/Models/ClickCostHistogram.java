package org.example.Models;

import java.util.ArrayList;
import java.util.Collections;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.List;
import java.util.Map;

public class ClickCostHistogram {

  private List<Double> clickCosts;
  private Map<String, Integer> clicksByDate;
  private boolean isClickByCost;
  private JFreeChart chart;
  // Constructor for Click Cost Histogram
  public ClickCostHistogram(List<Double> clickCosts) {
    this.clickCosts = clickCosts;
    this.isClickByCost = true;
    this.chart = createHistogram();

  }
  public ClickCostHistogram() {
    this.isClickByCost = true;
  }

  public ClickCostHistogram(Map<String, Integer> clicksByDate) {
    this.clicksByDate = clicksByDate;
    this.isClickByCost = false;
    this.chart = createHistogram();
  }

  public JFreeChart getChart() {
    return chart;
  }
  //Create Histogram Based on Selected Type
  public JFreeChart createHistogram() {
    if (isClickByCost) {
      return createClickCostHistogram();
    } else {
      return createClicksOverTimeHistogram();
    }
  }

  // click Cost Histogram
  private JFreeChart createClickCostHistogram() {
    HistogramDataset dataset = new HistogramDataset();
    dataset.setType(HistogramType.FREQUENCY);

    if (clickCosts != null && !clickCosts.isEmpty()) {
      double[] costArray = clickCosts.stream().mapToDouble(Double::doubleValue).toArray();
      int numBins = Math.min(50, (int) Math.ceil(clickCosts.stream().max(Double::compare).orElse(1.0)) + 1);
      dataset.addSeries("Click Costs", costArray, numBins);
    }

    return ChartFactory.createHistogram(
        "Click Cost Distribution",
        "Click Cost (pence)",
        "Frequency",
        dataset,
        PlotOrientation.VERTICAL,
        false,
        true,
        false
    );
  }


  private JFreeChart createClicksOverTimeHistogram() {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset(); //  Use a category dataset

    if (clicksByDate != null && !clicksByDate.isEmpty()) {
      List<String> sortedDates = new ArrayList<>(clicksByDate.keySet());
      Collections.sort(sortedDates); // Ensure chronological order

      int dayIndex = 1; // Start counting days from 1
      for (String date : sortedDates) {
        int clickCount = clicksByDate.get(date); //  Get the actual click count
        dataset.addValue(clickCount, "Clicks", "Day " + dayIndex);
        dayIndex++; // Move to next day
      }
    }

    JFreeChart chart = ChartFactory.createBarChart(
        "Clicks Over Time", // Chart title
        "Day", // X-axis label
        "Clicks", // Y-axis label
        dataset
    );

    CategoryPlot plot = (CategoryPlot) chart.getPlot();
    BarRenderer renderer = (BarRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, new java.awt.Color(255, 102, 102));

    return chart;
  }

  // Blank Histogram (For Initial Display)
  public JFreeChart createBlankHistogram() {
    HistogramDataset dataset = new HistogramDataset();

    // Use a dummy values to ensure the dataset is not empty
    dataset.addSeries("Click Costs", new double[]{0.0}, 1);

    return ChartFactory.createHistogram(
        "Click Cost",
        "",
        "",
        dataset,
        PlotOrientation.VERTICAL,
        false,
        true,
        false
    );
  }


}
