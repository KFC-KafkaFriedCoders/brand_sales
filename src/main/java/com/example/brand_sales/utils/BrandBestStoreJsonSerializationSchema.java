package com.example.brand_sales.utils;

import com.example.brand_sales.model.BrandBestStoreData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;

/**
 * 브랜드별 베스트 스토어 정보 JSON 직렬화 스키마
 */
public class BrandBestStoreJsonSerializationSchema implements KafkaSerializationSchema<BrandBestStoreData> {
    private static final Logger LOG = LoggerFactory.getLogger(BrandBestStoreJsonSerializationSchema.class);
    private final ObjectMapper objectMapper;
    private final String topic;

    public BrandBestStoreJsonSerializationSchema(String topic) {
        this.topic = topic;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public ProducerRecord<byte[], byte[]> serialize(BrandBestStoreData element, @Nullable Long timestamp) {
        try {
            // Use franchise_id and brand as key
            String key = element.getFranchise_id() + "_" + element.getStore_brand();
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] valueBytes = objectMapper.writeValueAsBytes(element);

            LOG.debug("Serializing BrandBestStoreData for brand {}, best store: {}",
                    element.getStore_brand(), element.getBest_store_name());

            return new ProducerRecord<>(topic, keyBytes, valueBytes);
        } catch (Exception e) {
            LOG.error("Failed to serialize BrandBestStoreData", e);
            throw new RuntimeException("Failed to serialize BrandBestStoreData", e);
        }
    }
}
