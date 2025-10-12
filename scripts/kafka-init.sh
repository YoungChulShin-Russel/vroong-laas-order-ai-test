#!/bin/bash

echo "⏳ Kafka 준비 대기 중..."

# Kafka가 준비될 때까지 대기 (최대 30초)
cub kafka-ready -b localhost:9092 1 30

echo "📝 토픽 생성 중..."

# order.order.events 토픽 생성
kafka-topics --create \
  --if-not-exists \
  --bootstrap-server localhost:9092 \
  --replication-factor 1 \
  --partitions 3 \
  --topic order.order.events

echo "✅ 토픽 생성 완료!"

# 생성된 토픽 목록 출력
kafka-topics --list --bootstrap-server localhost:9092

