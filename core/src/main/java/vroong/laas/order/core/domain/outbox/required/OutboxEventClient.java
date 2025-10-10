package vroong.laas.order.core.domain.outbox.required;

import vroong.laas.order.core.domain.shared.event.DomainEvent;

/**
 * Outbox Event Client Port
 *
 * <p>Domain Event를 Outbox 라이브러리에 저장하는 Port입니다.
 *
 * <p>책임:
 * - Domain Event → Outbox 라이브러리 호출
 * - Infrastructure에서 Adapter로 구현
 *
 * <p>Outbox 패턴:
 * - DB 트랜잭션과 이벤트 발행을 원자적으로 처리
 * - 별도 Worker가 Outbox → Kafka 전송
 */
public interface OutboxEventClient {

  /**
   * Domain Event를 Outbox에 저장
   *
   * @param event Domain Event
   */
  void save(DomainEvent event);
}

