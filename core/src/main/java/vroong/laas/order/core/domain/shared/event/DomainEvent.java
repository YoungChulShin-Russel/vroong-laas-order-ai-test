package vroong.laas.order.core.domain.shared.event;

import java.time.Instant;

/**
 * Domain Event 기본 인터페이스
 *
 * <p>Domain에서 발생한 중요한 사건을 나타냅니다.
 *
 * <p>특징:
 * - 불변 객체 (Immutable)
 * - 과거형 이름 (OrderCreated, OrderCancelled)
 * - 발생 시각 포함
 */
public interface DomainEvent {

  /**
   * 이벤트 발생 시각
   *
   * @return 이벤트 발생 시각
   */
  Instant occurredAt();

  /**
   * 이벤트 타입 (이벤트 구분용)
   *
   * @return 이벤트 타입 문자열
   */
  default String eventType() {
    return this.getClass().getSimpleName();
  }
}
