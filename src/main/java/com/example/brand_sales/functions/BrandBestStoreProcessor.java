package com.example.brand_sales.functions;

import com.example.brand_sales.model.BrandBestStoreData;
import com.example.brand_sales.model.ReceiptData;
import com.example.brand_sales.model.StoreSalesData;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 브랜드별 최고 매출 스토어를 계산하는 프로세서
 */
public class BrandBestStoreProcessor extends KeyedProcessFunction<Integer, ReceiptData, BrandBestStoreData> {
    private static final Logger LOG = LoggerFactory.getLogger(BrandBestStoreProcessor.class);
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 브랜드별 스토어 매출 상태
    private MapState<String, Map<Integer, StoreSalesData>> brandStoresSalesState;

    @Override
    public void open(Configuration parameters) throws Exception {
        // 브랜드별 스토어 매출 상태 초기화
        brandStoresSalesState = getRuntimeContext().getMapState(
                new MapStateDescriptor<>(
                        "brandStoresSales",
                        Types.STRING,
                        new TypeHint<Map<Integer, StoreSalesData>>(){}.getTypeInfo()
                )
        );
    }

    @Override
    public void processElement(ReceiptData receipt, Context ctx, Collector<BrandBestStoreData> out) throws Exception {
        // 다음 1분 경계 계산 및 타이머 등록
        long currentProcessingTime = ctx.timerService().currentProcessingTime();
        long nextMinuteBoundary = (currentProcessingTime / 60000 + 1) * 60000; // 다음 분의 경계
        ctx.timerService().registerProcessingTimeTimer(nextMinuteBoundary);

        // 브랜드, 스토어 정보 추출
        String brand = receipt.getStore_brand();
        int storeId = receipt.getStore_id();
        String storeName = receipt.getStore_name();
        String region = receipt.getRegion();
        int amount = receipt.getTotal_price();

        // 브랜드별 스토어 매출 상태 업데이트
        Map<Integer, StoreSalesData> storeMap;
        if (brandStoresSalesState.contains(brand)) {
            storeMap = brandStoresSalesState.get(brand);
        } else {
            storeMap = new HashMap<>();
        }

        // 스토어 매출 데이터 업데이트
        StoreSalesData storeSales = storeMap.get(storeId);
        if (storeSales == null) {
            storeSales = new StoreSalesData(storeId, storeName, region, amount);
        } else {
            storeSales.addSales(amount);
        }
        storeMap.put(storeId, storeSales);
        brandStoresSalesState.put(brand, storeMap);

        LOG.info("Updated store sales - franchise_id: {}, brand: {}, store: {}, amount: {}, total: {}",
                receipt.getFranchise_id(), brand, storeName, amount, storeSales.getTotal_sales());
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<BrandBestStoreData> out) throws Exception {
        // 타이머가 발동될 때 브랜드별 최고 매출 스토어 출력
        int franchiseId = ctx.getCurrentKey();
        String currentTime = DATETIME_FORMAT.format(new Date(timestamp));

        LOG.info("Best store timer fired at {} for franchise {}", currentTime, franchiseId);

        // 각 브랜드별 최고 매출 스토어 찾기
        for (String brand : brandStoresSalesState.keys()) {
            Map<Integer, StoreSalesData> storeMap = brandStoresSalesState.get(brand);

            if (storeMap != null && !storeMap.isEmpty()) {
                // 최고 매출 스토어 찾기
                StoreSalesData bestStore = findBestStore(storeMap);

                // 결과 생성
                BrandBestStoreData result = new BrandBestStoreData(
                        franchiseId,
                        brand,
                        bestStore.getStore_id(),
                        bestStore.getStore_name(),
                        bestStore.getRegion(),
                        bestStore.getTotal_sales(),
                        currentTime
                );

                LOG.info("Best store for brand {} is {} with sales {}",
                        brand, bestStore.getStore_name(), bestStore.getTotal_sales());

                out.collect(result);
            }
        }

        // 상태 초기화 (새로운 1분 구간을 위해)
        brandStoresSalesState.clear();

        // 다음 타이머 등록 (다음 1분 경계)
        long nextMinuteBoundary = ((timestamp / 60000) + 1) * 60000;
        ctx.timerService().registerProcessingTimeTimer(nextMinuteBoundary);
    }

    /**
     * 최고 매출 스토어 찾기
     */
    private StoreSalesData findBestStore(Map<Integer, StoreSalesData> storeMap) {
        return storeMap.values().stream()
                .max(Comparator.comparing(StoreSalesData::getTotal_sales))
                .orElseThrow(() -> new RuntimeException("No stores found"));
    }
}
