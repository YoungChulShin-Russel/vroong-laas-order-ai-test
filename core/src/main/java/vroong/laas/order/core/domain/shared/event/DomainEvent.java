package vroong.laas.order.core.domain.shared.event;

import java.time.Instant;

/**
 * Domain Event 마커 인터페이스
 *
 * <p>비즈니스 이벤트를 식별합니다.
 *
 * <p>이벤트 발생 시각만 공통으로 정의하며, 나머지는 각 이벤트 클래스에서 정의합니다.
 */
public interface DomainEvent {

  /**
   * 이벤트 발생 시각
   *
   * <p>이벤트 순서 보장 및 감사 로그에 사용됩니다.
   *
   * @return 이벤트가 발생한 시각
   */
  Instant occurredAt();
}

