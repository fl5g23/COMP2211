package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import java.util.Collections;
import java.util.List;

public class ClickCostHistogram {

  private List<Double> costsList;

  public ClickCostHistogram(List<Double> costsList) {
    this.costsList = costsList;
  }

  public JFreeChart createHistogram() {
    HistogramDataset dataset = new HistogramDataset();

    // Convert list to array
    double[] costArray = costsList.stream().mapToDouble(Double::doubleValue).toArray();

    // Define appropriate bin count (avoid too many or too few bins)
// Ensure bin count is based on integer values, adding 1 for rounding up
    int numBins = Math.min(100, (int) Math.ceil(Collections.max(costsList)) + 1);

    // Ensure correct frequency counts
    dataset.setType(HistogramType.FREQUENCY);
    dataset.addSeries("Click Costs", costArray, numBins);

    // Create Histogram Chart
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
}
