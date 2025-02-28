package org.example;

public class StatsCalculator {

  //  Total Cost Calculation
  public static double calculateTotalCost() {
    double totalImpressionCost = 0;
    double totalClickCost = 0;

//    // Loop through impressions and add costs
//    for (...) {  //  will specify the list later
//      totalImpressionCost += ...;  // Extract impression cost
//    }

    // Loop through clicks and add costs
//    for (...) {
//      totalClickCost += ...;  // Extract click cost
//    }

    return totalImpressionCost + totalClickCost;
  }

  //  Click-Through Rate (CTR) = (Clicks / Impressions) * 100
  public static double calculateCTR() {
    int impressions = 0;
    int clicks = 0;

//    // Extract impression and click count
//        ...

    return impressions == 0 ? 0 : ((double) clicks / impressions) * 100;
  }

  //  Cost-Per-Click (CPC) = Total Click Cost / Clicks
  public static double calculateCPC() {
    double totalClickCost = 0;
    int clickCount = 0;

//    for (...) {
//      totalClickCost += ...;  // Extract click cost
//      clickCount++;
//    }

    return clickCount == 0 ? 0 : totalClickCost / clickCount;
  }

  //  Cost-Per-Acquisition (CPA) = Total Cost / Conversions
  public static double calculateCPA() {
    double totalCost = 0;
    int conversions = 0;

// Extract total cost and conversion count

    return conversions == 0 ? 0 : totalCost / conversions;
  }

  //  Cost-Per-Thousand Impressions (CPM) = (Total Impression Cost * 1000) / Impressions
  public static double calculateCPM() {
    double totalImpressionCost = 0;
    int impressions = 0;

//    for (...) {
//      totalImpressionCost += ...;  // Extract impression cost
//      impressions++;
//    }

    return impressions == 0 ? 0 : (totalImpressionCost * 1000) / impressions;
  }

  //  Bounce Rate = (Bounces / Clicks)
//  bounce Rate Calculation (User Chooses Definition)
  public static double calculateBounceRate() {
    int bounces = 0;
    int clicks = 0;

//    // Loop through server logs to count bounces
//    for (...) {
//      boolean isBounce = false;
//
//      // Option 1: Page-Based Bounce (Default)
//      if (... == 1) {  // Condition for a bounce (only 1 page viewed)
//        isBounce = true;
//      }
//
//      // Option 2: Time-Based Bounce (If we have session duration)
//      if (... < 5) {  // User spent less than 5 seconds (example threshold)
//        isBounce = true;
//      }
//
//      if (isBounce) {
//        bounces++;
//      }
    //}

    return clicks == 0 ? 0 : ((double) bounces / clicks);
  }
}

