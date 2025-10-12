package vroong.laas.order.infrastructure.outbox;

import com.vroong.msa.kafka.eventpublisher.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import vroong.laas.order.core.domain.outbox.OutboxEventType;
import vroong.laas.order.core.domain.shared.AggregateRoot;
import vroong.laas.order.core.domain.outbox.required.OutboxEventClient;

/**
 * Kafka Outbox Event Client Adapter
 *
 * <p>Domain Event를 Kafka Outbox 라이브러리에 저장합니다.
 *
 * <p>책임:
 * - OutboxEventClient (Port) 구현
 * - Domain Model을 KafkaEvent로 변환 (KafkaOutboxEventMapper 사용)
 * - Outbox 라이브러리 호출 (OutboxEventService.registerEvent)
 *
 * <p>Outbox 패턴:
 * - Order 저장과 동일한 트랜잭션으로 Outbox 저장
 * - 별도 Worker가 Outbox → Kafka 전송 (비동기)
 * - 메시지 전송 실패 시 재시도 보장
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class KafkaOutboxEventClient implements OutboxEventClient {

  private final OutboxEventService outboxEventService;
  private final KafkaOutboxEventMapper outboxEventMapper = new KafkaOutboxEventMapper();

  /**
   * Domain Event를 Outbox에 저장
   *
   * @param eventType Outbox Event Type
   * @param aggregateRoot AggregateRoot model
   */
  @Override
  public void save(OutboxEventType eventType, AggregateRoot aggregateRoot) {
    KafkaOutboxEvent event = outboxEventMapper.map(eventType, aggregateRoot);
    outboxEventService.registerEvent(event.kafkaEvent(), event.eventKey());
  }

  /**
   * Outbox 테이블에서 미전송 이벤트를 Kafka로 발행
   *
   * @param batchSize 한 번에 처리할 이벤트 수
   * @return 발행된 이벤트 수
   */
  @Override
  public int publishPendingEvents(int batchSize) {
    return outboxEventService.processUnpublishedEvents(batchSize);
  }
}