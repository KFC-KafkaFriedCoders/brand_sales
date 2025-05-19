package com.example.brand_sales.model;

/**
 * 스토어별 매출 정보
 */
public class StoreSalesData {
    private int store_id;
    private String store_name;
    private String region;
    private long total_sales;

    public StoreSalesData(int store_id, String store_name, String region, long total_sales) {
        this.store_id = store_id;
        this.store_name = store_name;
        this.region = region;
        this.total_sales = total_sales;
    }

    // Getters and Setters
    public int getStore_id() {
        return store_id;
    }

    public void setStore_id(int store_id) {
        this.store_id = store_id;
    }

    public String getStore_name() {
        return store_name;
    }

    public void setStore_name(String store_name) {
        this.store_name = store_name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public long getTotal_sales() {
        return total_sales;
    }

    public void setTotal_sales(long total_sales) {
        this.total_sales = total_sales;
    }

    /**
     * 판매액 추가
     */
    public void addSales(long amount) {
        this.total_sales += amount;
    }

    @Override
    public String toString() {
        return "StoreSalesData{" +
                "store_id=" + store_id +
                ", store_name='" + store_name + '\'' +
                ", region='" + region + '\'' +
                ", total_sales=" + total_sales +
                '}';
    }
}