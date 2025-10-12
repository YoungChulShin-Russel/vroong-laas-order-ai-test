package vroong.laas.order.core.domain.outbox.required;

import vroong.laas.order.core.domain.outbox.OutboxEventType;
import vroong.laas.order.core.domain.shared.AggregateRoot;

/**
 * Outbox Event Client Port
 *
 * <p>AggregateRoot를 받아서 Outbox 라이브러리에 저장하는 Port입니다.
 *
 * <p>책임:
 * - OutboxEventType과 AggregateRoot를 받아서 Outbox에 저장
 * - Infrastructure에서 Adapter로 구현 (KafkaOutboxEventClient)
 * - Adapter에서 AggregateRoot → Kafka Payload 변환 수행
 *
 * <p>Outbox 패턴:
 * - DB 트랜잭션과 이벤트 발행을 원자적으로 처리
 * - Order 저장 + Outbox 저장 = 하나의 트랜잭션
 * - 별도 Worker가 Outbox → Kafka 전송 (비동기)
 *
 * <p>흐름:
 * <pre>
 * OutboxEventAppender (Domain Service)
 *   → OutboxEventClient (Port)
 *     → KafkaOutboxEventClient (Adapter)
 *       → KafkaOutboxEventMapper (Mapper)
 *         → AggregateRoot → KafkaEvent
 *       → OutboxEventService.registerEvent() (외부 라이브러리)
 * </pre>
 */
public interface OutboxEventClient {

  /**
   * AggregateRoot를 Outbox에 저장
   *
   * @param type Outbox Event Type (ORDER_CREATED, ORDER_CANCELLED 등)
   * @param aggregateRoot AggregateRoot (Order, Payment 등)
   */
  void save(OutboxEventType type, AggregateRoot aggregateRoot);

  /**
   * Outbox 테이블에서 미전송 이벤트를 Kafka로 발행
   *
   * <p>Job에서 주기적으로 호출하여 Outbox에 쌓인 이벤트를 Kafka로 전송합니다.
   *
   * @param batchSize 한 번에 처리할 이벤트 수
   * @return 발행된 이벤트 수
   */
  int publishPendingEvents(int batchSize);
}

