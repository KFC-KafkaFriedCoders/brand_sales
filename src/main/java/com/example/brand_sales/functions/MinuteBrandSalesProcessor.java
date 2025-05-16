package com.example.brand_sales.functions;

import com.example.brand_sales.model.ReceiptData;
import com.example.brand_sales.model.SalesTotalData;
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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 1분 단위로 브랜드별 매출을 집계하는 프로세서
 */
public class MinuteBrandSalesProcessor extends KeyedProcessFunction<Integer, ReceiptData, SalesTotalData> {
    private static final Logger LOG = LoggerFactory.getLogger(MinuteBrandSalesProcessor.class);
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 브랜드별 상태
    private MapState<String, Long> brandSalesState;           // 브랜드별 매출 합계
    private MapState<String, Set<Integer>> brandStoresState;  // 브랜드별 매장 ID 세트

    @Override
    public void open(Configuration parameters) throws Exception {
        // 브랜드별 매출 합계 상태 초기화
        brandSalesState = getRuntimeContext().getMapState(
                new MapStateDescriptor<>(
                        "brandSales",
                        Types.STRING,
                        Types.LONG
                )
        );

        // 브랜드별 매장 ID 세트 상태 초기화 - 이 부분이 빠져있었습니다
        brandStoresState = getRuntimeContext().getMapState(
                new MapStateDescriptor<>(
                        "brandStores",
                        Types.STRING,
                        new TypeHint<Set<Integer>>(){}.getTypeInfo()
                )
        );
    }

    @Override
    public void processElement(ReceiptData receipt, Context ctx, Collector<SalesTotalData> out) throws Exception {
        // 현재 시간 기준으로 다음 1분 경계 계산
        long currentProcessingTime = ctx.timerService().currentProcessingTime();
        long nextMinuteBoundary = (currentProcessingTime / 60000 + 1) * 60000; // 다음 분의 경계

        // 타이머 등록 - 다음 1분 경계에 실행
        ctx.timerService().registerProcessingTimeTimer(nextMinuteBoundary);

        // 브랜드 추출
        String brand = receipt.getStore_brand();

        // 브랜드별 매출 상태 업데이트
        Long brandSales = brandSalesState.contains(brand) ? brandSalesState.get(brand) : 0L;
        brandSales += receipt.getTotal_price();
        brandSalesState.put(brand, brandSales);

        // 브랜드별 매장 ID 세트 업데이트
        Set<Integer> storeSet;
        if (brandStoresState.contains(brand)) {
            storeSet = brandStoresState.get(brand);
        } else {
            storeSet = new HashSet<>();
        }
        storeSet.add(receipt.getStore_id());
        brandStoresState.put(brand, storeSet);

        LOG.info("Updated brand sales - franchise_id: {}, brand: {}, current sales: {}, stores: {}",
                receipt.getFranchise_id(), brand, brandSales, storeSet.size());
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<SalesTotalData> out) throws Exception {
        // 타이머가 발동될 때 브랜드별 결과 출력
        int franchiseId = ctx.getCurrentKey();
        String currentTime = DATETIME_FORMAT.format(new Date(timestamp));

        LOG.info("Timer fired at {} for franchise {}", currentTime, franchiseId);

        // 각 브랜드별로 집계 결과 출력
        for (String brand : brandSalesState.keys()) {
            long brandSales = brandSalesState.get(brand);
            Set<Integer> storeSet = brandStoresState.get(brand);
            int storeCount = storeSet != null ? storeSet.size() : 0;

            // 결과 생성
            SalesTotalData result = new SalesTotalData(
                    franchiseId,
                    brand,
                    storeCount,     // 매장 수
                    brandSales,     // 브랜드별 합계 매출
                    currentTime     // 현재 시간
            );

            LOG.info("Minute aggregation - franchise: {}, brand: {}, stores: {}, sales: {}",
                    franchiseId, brand, storeCount, brandSales);

            out.collect(result);
        }

        // 상태 초기화 (새로운 1분 구간을 위해)
        brandSalesState.clear();
        brandStoresState.clear();

        // 다음 타이머 등록 (다음 1분 경계)
        long nextMinuteBoundary = ((timestamp / 60000) + 1) * 60000;
        ctx.timerService().registerProcessingTimeTimer(nextMinuteBoundary);
    }
}
