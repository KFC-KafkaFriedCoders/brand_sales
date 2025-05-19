package com.example.brand_sales.model;

/**
 * 브랜드별 베스트 스토어 정보
 */
public class BrandBestStoreData {
    private int franchise_id;        // 프랜차이즈 ID
    private String store_brand;      // 스토어 브랜드
    private int best_store_id;       // 최고 매출 스토어 ID
    private String best_store_name;  // 최고 매출 스토어 이름
    private String region;           // 지역
    private long store_sales;        // 해당 스토어 매출
    private String update_time;      // 업데이트 시간

    // Default constructor
    public BrandBestStoreData() {}

    // Constructor with all fields
    public BrandBestStoreData(int franchise_id, String store_brand, int best_store_id,
                              String best_store_name, String region, long store_sales,
                              String update_time) {
        this.franchise_id = franchise_id;
        this.store_brand = store_brand;
        this.best_store_id = best_store_id;
        this.best_store_name = best_store_name;
        this.region = region;
        this.store_sales = store_sales;
        this.update_time = update_time;
    }

    // Getters and Setters
    public int getFranchise_id() {
        return franchise_id;
    }

    public void setFranchise_id(int franchise_id) {
        this.franchise_id = franchise_id;
    }

    public String getStore_brand() {
        return store_brand;
    }

    public void setStore_brand(String store_brand) {
        this.store_brand = store_brand;
    }

    public int getBest_store_id() {
        return best_store_id;
    }

    public void setBest_store_id(int best_store_id) {
        this.best_store_id = best_store_id;
    }

    public String getBest_store_name() {
        return best_store_name;
    }

    public void setBest_store_name(String best_store_name) {
        this.best_store_name = best_store_name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public long getStore_sales() {
        return store_sales;
    }

    public void setStore_sales(long store_sales) {
        this.store_sales = store_sales;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    @Override
    public String toString() {
        return "BrandBestStoreData{" +
                "franchise_id=" + franchise_id +
                ", store_brand='" + store_brand + '\'' +
                ", best_store_id=" + best_store_id +
                ", best_store_name='" + best_store_name + '\'' +
                ", region='" + region + '\'' +
                ", store_sales=" + store_sales +
                ", update_time='" + update_time + '\'' +
                '}';
    }
}
