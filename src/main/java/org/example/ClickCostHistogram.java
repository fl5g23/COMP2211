package org.example;

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
import java.util.stream.Collectors;

public class ClickCostHistogram {

  private List<Double> clickCosts;
  private Map<String, Integer> clicksByDate;
  private boolean isClickByCost;

  // ✅ Constructor for Click Cost Histogram
  public ClickCostHistogram(List<Double> clickCosts) {
    this.clickCosts = clickCosts;
    this.isClickByCost = true;
  }
  public ClickCostHistogram() {
    this.isClickByCost = true;
  }

  // ✅ Constructor for Clicks Over Time Histogram (Grouped by Day)
  public ClickCostHistogram(Map<String, Integer> clicksByDate) {
    this.clicksByDate = clicksByDate;
    this.isClickByCost = false;
  }

  // ✅ Create Histogram Based on Selected Type
  public JFreeChart createHistogram() {
    if (isClickByCost) {
      return createClickCostHistogram();
    } else {
      return createClicksOverTimeHistogram();
    }
  }

  // ✅ Click Cost Histogram (Bins Click Costs)
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
    DefaultCategoryDataset dataset = new DefaultCategoryDataset(); // ✅ Use a category dataset

    if (clicksByDate != null && !clicksByDate.isEmpty()) {
      List<String> sortedDates = new ArrayList<>(clicksByDate.keySet());
      Collections.sort(sortedDates); // ✅ Ensure chronological order

      int dayIndex = 1; // Start counting days from 1
      for (String date : sortedDates) {
        int clickCount = clicksByDate.get(date); // ✅ Get the actual click count
        dataset.addValue(clickCount, "Clicks", "Day " + dayIndex); // ✅ Add "Day X" as category
        dayIndex++; // Move to next day
      }
    }

    JFreeChart chart = ChartFactory.createBarChart(
        "Clicks Over Time", // Chart title
        "Day", // X-axis label
        "Clicks", // Y-axis label
        dataset // ✅ Use category dataset
    );

    // ✅ Improve visualization (optional)
    CategoryPlot plot = (CategoryPlot) chart.getPlot();
    BarRenderer renderer = (BarRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, new java.awt.Color(255, 102, 102)); // Red bars

    return chart;
  }

  // ✅ Blank Histogram (For Initial Display)
  public JFreeChart createBlankHistogram() {
    // Create a dataset with a dummy value (e.g., 0.0) to avoid the IllegalArgumentException
    HistogramDataset dataset = new HistogramDataset();

    // Use a dummy value (e.g., 0.0) to ensure the dataset is not empty
    dataset.addSeries("Click Costs", new double[]{0.0}, 1);

    // Create and return a histogram chart with the dummy dataset
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
