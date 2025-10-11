package vroong.laas.order.infrastructure.outbox;

import com.vroong.msa.kafka.eventpublisher.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import vroong.laas.order.core.domain.outbox.OutboxEventType;
import vroong.laas.order.core.domain.shared.AggregateRoot;
import vroong.laas.order.core.domain.outbox.required.OutboxEventClient;

/**
 * Outbox Event Client Adapter
 *
 * <p>Domain Event를 Outbox 라이브러리에 저장합니다.
 *
 * <p>책임:
 * - OutboxEventClient (Port) 구현
 * - Domain Event → Outbox 라이브러리 호출
 *
 * <p>Outbox 패턴:
 * - Order 저장과 동일한 트랜잭션으로 Outbox 저장
 * - 별도 Worker가 Outbox → Kafka 전송
 *
 * <p>TODO: 자체 Outbox 라이브러리 연동 필요
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
}