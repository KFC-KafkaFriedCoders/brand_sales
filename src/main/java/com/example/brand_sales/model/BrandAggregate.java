package com.example.brand_sales.model;

import java.time.LocalDateTime;

/**
 * Brand payment aggregate data
 */
public class BrandAggregate {
    private String brandName;
    private int totalAmount;
    private int transactionCount;
    private String windowStartTime;
    private String windowEndTime;

    public BrandAggregate() {
    }

    public BrandAggregate(String brandName, int totalAmount, int transactionCount) {
        this.brandName = brandName;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
    }

    // Getters and Setters
    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    public String getWindowStartTime() {
        return windowStartTime;
    }

    public void setWindowStartTime(String windowStartTime) {
        this.windowStartTime = windowStartTime;
    }

    public String getWindowEndTime() {
        return windowEndTime;
    }

    public void setWindowEndTime(String windowEndTime) {
        this.windowEndTime = windowEndTime;
    }

    /**
     * Adds amount from another transaction
     */
    public void addAmount(int amount) {
        this.totalAmount += amount;
        this.transactionCount++;
    }

    /**
     * Merges with another aggregate
     */
    public void merge(BrandAggregate other) {
        this.totalAmount += other.totalAmount;
        this.transactionCount += other.transactionCount;
    }

    @Override
    public String toString() {
        return "BrandAggregate{" +
                "brandName='" + brandName + '\'' +
                ", totalAmount=" + totalAmount +
                ", transactionCount=" + transactionCount +
                ", windowStartTime='" + windowStartTime + '\'' +
                ", windowEndTime='" + windowEndTime + '\'' +
                '}';
    }
}