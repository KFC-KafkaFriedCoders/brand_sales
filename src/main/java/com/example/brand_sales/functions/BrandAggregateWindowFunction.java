package com.example.brand_sales.functions;

import com.example.brand_sales.model.BrandAggregate;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Window function to aggregate payment amounts by brand
 */
public class BrandAggregateWindowFunction
        extends ProcessWindowFunction<BrandAggregate, BrandAggregate, String, TimeWindow> {

    private static final Logger logger = LoggerFactory.getLogger(BrandAggregateWindowFunction.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Override
    public void process(String brandName, Context context,
                        Iterable<BrandAggregate> elements,
                        Collector<BrandAggregate> out) throws Exception {

        // Aggregate all brand data in this window
        BrandAggregate aggregate = new BrandAggregate();
        aggregate.setBrandName(brandName);
        aggregate.setTotalAmount(0);
        aggregate.setTransactionCount(0);

        // Merge all brand data in the window
        for (BrandAggregate element : elements) {
            aggregate.merge(element);
        }

        // Set window time information
        LocalDateTime windowStart = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(context.window().getStart()), ZONE_ID);
        LocalDateTime windowEnd = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(context.window().getEnd()), ZONE_ID);

        aggregate.setWindowStartTime(windowStart.format(TIME_FORMATTER));
        aggregate.setWindowEndTime(windowEnd.format(TIME_FORMATTER));

        logger.info("Aggregated for brand: {}, total amount: {}, transactions: {}, window: {} to {}",
                brandName, aggregate.getTotalAmount(), aggregate.getTransactionCount(),
                aggregate.getWindowStartTime(), aggregate.getWindowEndTime());

        out.collect(aggregate);
    }
}