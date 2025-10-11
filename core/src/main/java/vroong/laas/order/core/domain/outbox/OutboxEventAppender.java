package vroong.laas.order.core.domain.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vroong.laas.order.core.domain.outbox.required.OutboxEventClient;
import vroong.laas.order.core.domain.shared.AggregateRoot;

/**
 * Outbox Event Appender (Domain Service)
 *
 * <p>AggregateRoot의 Domain Event를 Outbox에 저장하는 Domain Service입니다.
 *
 * <p>책임:
 * - AggregateRoot를 받아서 Outbox에 저장
 * - OutboxEventClient (Port)를 통해 Infrastructure 호출
 * - Domain Event 발행 후 초기화 (clearDomainEvents)
 *
 * <p>특징:
 * - 공통으로 사용되는 Domain Service (Order, Payment, Delivery 등 모든 Aggregate에서 사용)
 * - 트랜잭션은 호출하는 쪽에서 관리 (예: OrderCreator)
 *
 * <p>전체 흐름:
 * <pre>
 * OrderCreator (Domain Service)
 *   1. orderRepository.store(order)        → Order 저장 (DB)
 *   2. outboxEventAppender.append(order)   → Outbox 저장 (DB, 같은 트랜잭션)
 *      → outboxEventClient.save()           → Infrastructure Layer
 *        → KafkaOutboxEventMapper.map()      → Order → KafkaEvent 변환
 *        → outboxEventService.registerEvent() → Outbox 라이브러리 호출
 *      → aggregateRoot.clearDomainEvents()  → Domain Event 초기화
 * </pre>
 *
 * <p>사용 예시:
 * <pre>
 * // OrderCreator에서 사용
 * Order order = orderRepository.store(...);
 * outboxEventAppender.append(OutboxEventType.ORDER_CREATED, order);
 * </pre>
 */
@Service
@RequiredArgsConstructor
public class OutboxEventAppender {

  private final OutboxEventClient outboxEventClient;

  /**
   * AggregateRoot의 Domain Event를 Outbox에 저장하고 초기화
   *
   * @param type Outbox Event Type
   * @param aggregateRoot AggregateRoot (Domain Event를 포함)
   */
  public void append(OutboxEventType type, AggregateRoot aggregateRoot) {
    outboxEventClient.save(type, aggregateRoot);
    aggregateRoot.clearDomainEvents();
  }
}
