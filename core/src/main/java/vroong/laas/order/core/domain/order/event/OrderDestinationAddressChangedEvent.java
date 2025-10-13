package vroong.laas.order.core.domain.order.event;

import java.time.Instant;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.shared.event.DomainEvent;

/**
 * 주문 도착지 주소 변경 이벤트
 *
 * <p>주문의 도착지 주소가 변경되었을 때 발행되는 Domain Event입니다.
 *
 * <p>변경 범위:
 * - Address (주소)
 * - LatLng (위경도)
 * - EntranceInfo (출입 가이드)
 *
 * <p>유지되는 것:
 * - Contact (연락처) - 변경되지 않음
 *
 * <p>특징:
 * - 불변 객체 (record)
 * - 변경 전/후 도착지 정보 포함 (Fat Event)
 * - 다른 서비스에서 이 이벤트로 비즈니스 로직 처리 가능
 */
public record OrderDestinationAddressChangedEvent(
    Long orderId, Destination oldDestination, Destination newDestination, Instant occurredAt)
    implements DomainEvent {

  /**
   * Order로부터 OrderDestinationAddressChangedEvent 생성
   *
   * @param orderId 주문 ID
   * @param oldDestination 변경 전 도착지
   * @param newDestination 변경 후 도착지
   * @param occurredAt 이벤트 발생 시각
   * @return OrderDestinationAddressChangedEvent
   */
  public static OrderDestinationAddressChangedEvent of(
      Long orderId, Destination oldDestination, Destination newDestination, Instant occurredAt) {
    return new OrderDestinationAddressChangedEvent(
        orderId, oldDestination, newDestination, occurredAt);
  }
}

