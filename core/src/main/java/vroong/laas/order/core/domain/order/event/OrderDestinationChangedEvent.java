package vroong.laas.order.core.domain.order.event;

import java.time.Instant;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.shared.event.DomainEvent;

/**
 * 주문 도착지 변경 이벤트
 *
 * <p>주문의 도착지가 변경되었을 때 발행되는 Domain Event입니다.
 *
 * <p>특징:
 * - 불변 객체 (record)
 * - 변경 전/후 Destination 모두 포함
 * - 다른 서비스(배송, 알림 등)에서 도착지 변경에 대응 가능
 */
public record OrderDestinationChangedEvent(
    Long orderId,
    Destination oldDestination,
    Destination newDestination,
    Instant changedAt,
    Instant occurredAt)
    implements DomainEvent {

  /**
   * 도착지 변경 정보로 이벤트 생성
   *
   * @param orderId 주문 ID
   * @param oldDestination 변경 전 도착지
   * @param newDestination 변경 후 도착지
   * @param changedAt 변경 시각
   * @return OrderDestinationChangedEvent
   */
  public static OrderDestinationChangedEvent of(
      Long orderId, Destination oldDestination, Destination newDestination, Instant changedAt) {
    return new OrderDestinationChangedEvent(
        orderId, oldDestination, newDestination, changedAt, Instant.now() // 이벤트 발생 시각
        );
  }
}

