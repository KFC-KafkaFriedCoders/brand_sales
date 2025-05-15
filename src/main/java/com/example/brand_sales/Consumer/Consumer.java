package com.example.brand_sales.Consumer;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.*;

public class Consumer {
    public static void main(String[] args) {
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "13.209.157.53:9092,15.164.111.153:9092,3.34.32.69:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", "http://43.201.175.172:8081,http://43.202.127.159:8081");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "brand-sales-consumer-group");

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);

        KafkaConsumer<String, GenericRecord> consumer = new KafkaConsumer<>(props);

        String topic = "test-topic";
        consumeWithManualAssignment(consumer, topic);
    }

    public static void consumeWithManualAssignment(KafkaConsumer<String, GenericRecord> consumer, String topic) {
        try {
            // 브로커 연결 테스트
            System.out.println("브로커 연결 시도 중...");
            consumer.listTopics(Duration.ofSeconds(10));
            System.out.println("브로커 연결 성공");

            System.out.println("파티션 정보 확인 중...");
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);
            if (partitionInfos == null || partitionInfos.isEmpty()) {
                System.out.println("토픽에 파티션이 없습니다: " + topic);
                return;
            }
            System.out.println("파티션 수: " + partitionInfos.size());

            List<TopicPartition> partitions = new ArrayList<>();
            for (PartitionInfo partitionInfo : partitionInfos) {
                partitions.add(new TopicPartition(topic, partitionInfo.partition()));
            }
            consumer.assign(partitions);

            for (TopicPartition partition : partitions) {
                consumer.seek(partition, 0);
            }

            while (true) {
                System.out.println("메시지 폴링 중...");
                ConsumerRecords<String, GenericRecord> records = consumer.poll(Duration.ofMillis(1000));
                System.out.println("수신된 레코드 수: " + records.count());

                for (TopicPartition partition : partitions) {
                    long lastOffset = -1;
                    for (ConsumerRecord<String, GenericRecord> record : records.records(partition)) {
                        processRecord(record);
                        lastOffset = record.offset();
                    }

                    if (lastOffset != -1) {
                        Map<TopicPartition, OffsetAndMetadata> offsetMap = new HashMap<>();
                        offsetMap.put(partition, new OffsetAndMetadata(lastOffset + 1));
                        consumer.commitSync(offsetMap);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("오류 발생: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("컨슈머를 종료합니다.");
            consumer.close();
        }
    }

    private static void processRecord(ConsumerRecord<String, GenericRecord> record) {
        GenericRecord avroRecord = record.value();
        String id = avroRecord.get("id").toString(); // Avro 스키마 필드명에 맞춰 변경
        double amount = (double) avroRecord.get("amount");
        System.out.printf("ID: %s, 금액: %.2f%n", id, amount);
    }
}