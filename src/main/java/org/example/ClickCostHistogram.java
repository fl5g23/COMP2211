package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import java.util.Collections;
import java.util.List;

public class ClickCostHistogram {


  public JFreeChart createHistogram(List<Double> costsList) {
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

  /**
   * Returns blank histogram for when no campaign is loaded
   */
  public JFreeChart createBlankHistogram() {
    // Create a dataset with a dummy value (e.g., 0.0) to avoid the IllegalArgumentException
    HistogramDataset dataset = new HistogramDataset();

    // Use a dummy value (e.g., 0.0) to ensure the dataset is not empty
    dataset.addSeries("Click Costs", new double[]{0.0}, 1);

    // Create and return a histogram chart with the dummy dataset
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
