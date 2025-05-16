package com.example.brand_sales.functions;

import com.example.brand_sales.model.ReceiptData;
import org.apache.flink.api.java.functions.KeySelector;

public class FranchiseKeySelector implements KeySelector<ReceiptData, Integer> {
    @Override
    public Integer getKey(ReceiptData receipt) throws Exception {
        return receipt.getFranchise_id();
    }
}