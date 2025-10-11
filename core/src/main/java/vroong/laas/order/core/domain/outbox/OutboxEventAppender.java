package vroong.laas.order.core.domain.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vroong.laas.order.core.domain.outbox.required.OutboxEventClient;
import vroong.laas.order.core.domain.shared.AggregateRoot;

/**
 * Outbox Event Store (Domain Service)
 *
 * <p>Domain Event를 Outbox에 저장하는 Domain Service입니다.
 *
 * <p>책임:
 * - Domain Event 목록을 받아서 Outbox에 저장
 * - OutboxEventClient (Port)를 통해 Infrastructure 호출
 *
 * <p>특징:
 * - 공통으로 사용되는 Domain Service (Order, Payment, Delivery 등 모든 Aggregate에서 사용)
 * - 트랜잭션은 호출하는 쪽에서 관리 (예: OrderCreator)
 */
@Service
@RequiredArgsConstructor
public class OutboxEventAppender {

  private final OutboxEventClient outboxEventClient;

  public void append(OutboxEventType type, AggregateRoot aggregateRoot) {

  }
}
