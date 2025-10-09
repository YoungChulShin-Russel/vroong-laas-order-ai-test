package vroong.laas.order.core.domain.shared.required;

import java.util.List;
import vroong.laas.order.core.domain.shared.event.DomainEvent;

/**
 * Outbox Event Publisher Port
 *
 * <p>Domain Event를 Outbox 테이블에 저장하는 Port입니다.
 *
 * <p>실제 구현은 Infrastructure Layer에서 외부 Outbox 라이브러리를 래핑합니다.
 *
 * <p>특징:
 * - Domain Layer는 구현을 알 필요 없음
 * - Infrastructure Layer에서 Adapter로 구현
 * - 여러 Aggregate에서 공통으로 사용
 *
 * <p>트랜잭션:
 * - UseCase의 @Transactional 안에서 호출
 * - Aggregate 저장 + Event 저장이 같은 트랜잭션으로 보장
 */
public interface OutboxEventPublisher {

  /**
   * Domain Event를 Outbox에 발행합니다.
   *
   * <p>실제로는 Outbox 테이블에 저장하며, 별도 Worker가 Kafka로 발행합니다.
   *
   * @param event 발행할 Domain Event
   */
  void publish(DomainEvent event);

  /**
   * 여러 Domain Event를 한 번에 발행합니다.
   *
   * <p>배치 처리가 필요한 경우 사용합니다.
   *
   * @param events 발행할 Domain Event 목록
   */
  void publishAll(List<DomainEvent> events);
}

