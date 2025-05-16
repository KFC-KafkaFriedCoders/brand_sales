package com.example.brand_sales;

import com.example.brand_sales.functions.FranchiseKeySelector;
import com.example.brand_sales.functions.MinuteBrandSalesProcessor;
import com.example.brand_sales.functions.TodayReceiptFilter;
import com.example.brand_sales.model.ReceiptData;
import com.example.brand_sales.model.SalesTotalData;
import com.example.brand_sales.utils.SalesTotalJsonSerializationSchema;
import com.example.brand_sales.utils.SimpleAvroDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * 1분마다 브랜드별 매출을 집계하는 애플리케이션
 */
public class MinuteBrandSalesApp {
    private static final Logger LOG = LoggerFactory.getLogger(MinuteBrandSalesApp.class);

    public static void main(String[] args) throws Exception {
        // 설정 파일 로드
        Properties appProps = loadApplicationProperties();

        String bootstrapServers = appProps.getProperty("kafka.bootstrap.servers");
        String sourceTopic = appProps.getProperty("kafka.source.topic");
        String sinkTopic = appProps.getProperty("kafka.sink.topic") + "-minute"; // 분 단위 토픽 지정
        String consumerGroup = appProps.getProperty("kafka.consumer.group") + "-minute";
        long checkpointInterval = Long.parseLong(appProps.getProperty("flink.checkpoint.interval", "60000"));

        LOG.info("브랜드별 1분 단위 매출 집계 애플리케이션 시작");
        LOG.info("Kafka 서버: {}", bootstrapServers);
        LOG.info("소스 토픽: {}", sourceTopic);
        LOG.info("싱크 토픽: {}", sinkTopic);

        // Flink 실행 환경 설정
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);  // 로컬 테스트를 위해 병렬 처리를 1로 설정
        env.enableCheckpointing(checkpointInterval);

        // Kafka 컨슈머 설정
        Properties consumerProps = new Properties();
        consumerProps.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.setProperty(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        consumerProps.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        // Avro 데이터를 위한 Kafka 컨슈머 생성
        FlinkKafkaConsumer<ReceiptData> consumer = new FlinkKafkaConsumer<>(
                sourceTopic,
                new SimpleAvroDeserializationSchema<>(ReceiptData.class),
                consumerProps
        );

        // Kafka 프로듀서 설정
        Properties producerProps = new Properties();
        producerProps.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        producerProps.setProperty(ProducerConfig.RETRIES_CONFIG, "3");
        producerProps.setProperty(ProducerConfig.LINGER_MS_CONFIG, "10");
        producerProps.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, "16384");
        producerProps.setProperty(ProducerConfig.BUFFER_MEMORY_CONFIG, "33554432");

        // Kafka에서 데이터 스트림 생성
        DataStream<ReceiptData> receiptStream = env.addSource(consumer)
                .name("영수증 데이터 소스")
                .map(receipt -> {
                    LOG.info("영수증 수신: franchise_id={}, brand={}, time={}",
                            receipt.getFranchise_id(), receipt.getStore_brand(), receipt.getTime());
                    return receipt;
                });

        // 오늘 날짜 영수증만 필터링
        DataStream<ReceiptData> todayReceiptStream = receiptStream
                .filter(new TodayReceiptFilter())
                .name("오늘 영수증 필터");

        // franchise_id 기준으로 키 설정
        KeyedStream<ReceiptData, Integer> keyedStream = todayReceiptStream
                .keyBy(new FranchiseKeySelector());

        // 1분 단위 브랜드별 매출 집계 처리
        DataStream<SalesTotalData> minuteSalesStream = keyedStream
                .process(new MinuteBrandSalesProcessor())
                .name("1분 단위 브랜드별 매출 집계");

        // 출력을 위한 Kafka 프로듀서 생성
        FlinkKafkaProducer<SalesTotalData> producer = new FlinkKafkaProducer<>(
                sinkTopic,
                new SalesTotalJsonSerializationSchema(sinkTopic),
                producerProps,
                FlinkKafkaProducer.Semantic.AT_LEAST_ONCE
        );

        // Kafka 싱크 추가
        minuteSalesStream.addSink(producer)
                .name("브랜드별 1분 단위 매출 싱크");

        // 작업 실행
        env.execute("브랜드별 1분 단위 매출 집계");
    }

    private static Properties loadApplicationProperties() throws Exception {
        Properties props = new Properties();
        try (InputStream inputStream = MinuteBrandSalesApp.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("application.properties를 클래스패스에서 찾을 수 없습니다");
            }
            props.load(inputStream);
            return props;
        }
    }
}
