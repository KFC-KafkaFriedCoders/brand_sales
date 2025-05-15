#  브랜드별 실시간 매출 집계 (Duplicate Payment Detector)

실시간으로 영수증 데이터를 분석하여 브랜드별 매출 총합을 구하는 Apache Flink 기반 애플리케이션입니다.

## 주요 기능

- Kafka에서 실시간 영수증 데이터 소비
- 사용자별로 10초 이내 반복 결제 탐지
- 중복 결제 감지 시 알람 생성
- 알람을 Kafka 토픽으로 전송

## 시스템 구조

```
[test-topic] → [Flink Application] → [duplicate-alert-topic]
                      ↓
               [필터링 및 그룹화]
                      ↓
               [10초 윈도우 처리]
                      ↓
               [중복 결제 탐지]
```

## 필요 환경

- Java 17+
- Apache Kafka 3.x
- Apache Flink 1.18.x
- Gradle 8.x

## 설치 및 실행

### 1. 프로젝트 빌드 - 수정필요

```bash
# 프로젝트 디렉토리로 이동
cd 

# Gradle 빌드
./gradlew build
```

### 2. Kafka 토픽 생성

```bash
# topic 생성
kafka-topics.sh --create \
    --topic duplicate-alert-topic \
    --bootstrap-server localhost:9092 \
    --partitions 3 \
    --replication-factor 1
```

### 3. 애플리케이션 실행

```bash
# Flink 클러스터 모드
flink run -c com.kafka.detector.DuplicatePaymentDetectorApp \
    build/libs/duplicate-payment-detector-1.0.0.jar

# 로컬 개발 모드
./gradlew run
```

## 설정

`src/main/resources/application.properties` 파일에서 설정을 변경할 수 있습니다.

```properties
# Kafka 설정
kafka.bootstrap.servers=localhost:9092
kafka.source.topic=test-topic
kafka.sink.topic=duplicate-alert-topic

# 윈도우 크기 (초)
flink.window.size.seconds=10
```

## 데이터 형식

### 입력 데이터 (test-topic)

영수증 데이터 (Avro 형식):
- `user_id`, `user_name`, `user_gender`, `user_age`
- `store_brand`, `store_name`
- `time`
- 기타 영수증 정보

### 출력 데이터 (duplicate-alert-topic)

중복 결제 알람 (JSON 형식):
```json
{
  "userName": "홍길동",
  "userGender": "M",
  "userAge": 30,
  "duplicateStores": ["스타벅스 강남점", "스타벅스 역삼점"],
  "alertMessage": "결제자: 홍길동, 스타벅스 강남점, 스타벅스 역삼점 결제 이상 탐지",
  "detectionTime": "2025-01-14 10:30:45"
}
```

## 모니터링

로그는 다음 위치에서 확인할 수 있습니다:
- 콘솔 출력
- `duplicate-payment-detector.log` 파일

## 주의사항

- Kafka가 실행 중이어야 합니다
- test-topic에 데이터가 들어와야 탐지가 시작됩니다
- 메모리 설정은 처리량에 따라 조정이 필요할 수 있습니다

## 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.
