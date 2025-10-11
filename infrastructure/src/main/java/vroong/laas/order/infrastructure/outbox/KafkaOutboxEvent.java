package vroong.laas.order.infrastructure.outbox;

import com.vroong.msa.kafka.event.KafkaEvent;
import com.vroong.msa.kafka.event.KafkaEventPayload;

/**
 * Kafka Outbox Event
 *
 * <p>Kafka Event와 Event Key를 담는 DTO입니다.
 *
 * <p>필드:
 * - eventKey: Kafka 파티션 키 (예: orderId)
 * - kafkaEvent: Kafka Event (eventType, eventSource, payload 포함)
 *
 * <p>용도:
 * - Mapper가 생성한 데이터를 Client에 전달
 * - OutboxEventService.registerEvent() 호출 시 사용
 */
record KafkaOutboxEvent(
    String eventKey,
    KafkaEvent<KafkaEventPayload> kafkaEvent
) {

}
