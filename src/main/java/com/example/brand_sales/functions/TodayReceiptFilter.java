package com.example.brand_sales.functions;

import com.example.brand_sales.model.ReceiptData;
import org.apache.flink.api.common.functions.FilterFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TodayReceiptFilter implements FilterFunction<ReceiptData> {
    private static final Logger LOG = LoggerFactory.getLogger(TodayReceiptFilter.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public boolean filter(ReceiptData receipt) throws Exception {
        String today = DATE_FORMAT.format(new Date());
        boolean isToday = receipt.getTime().startsWith(today);

        // 디버깅을 위해 모든 데이터 로깅
        LOG.info("Receipt time: {}, Today: {}, Filter result: {}",
                receipt.getTime(), today, isToday);

        // 임시로 모든 데이터 통과
        return true;
        // return isToday;
    }
}