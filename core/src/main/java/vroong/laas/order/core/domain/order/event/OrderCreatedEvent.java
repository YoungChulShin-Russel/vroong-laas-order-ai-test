package vroong.laas.order.core.domain.order.event;

import java.time.Instant;
import java.util.List;
import vroong.laas.order.core.domain.shared.event.DomainEvent;
import vroong.laas.order.core.domain.order.DeliveryPolicy;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.order.OrderNumber;
import vroong.laas.order.core.domain.order.OrderStatus;
import vroong.laas.order.core.domain.order.Origin;

/**
 * 주문 생성 이벤트
 *
 * <p>주문이 생성되었을 때 발행되는 Domain Event입니다.
 *
 * <p>특징:
 * - 불변 객체 (record)
 * - Order의 모든 정보 포함 (Fat Event)
 * - 다른 서비스에서 이 이벤트로 비즈니스 로직 처리 가능
 */
public record OrderCreatedEvent(
    Long orderId,
    OrderNumber orderNumber,
    OrderStatus status,
    List<OrderItem> items,
    Origin origin,
    Destination destination,
    DeliveryPolicy deliveryPolicy,
    Instant orderedAt,
    Instant occurredAt)
    implements DomainEvent {

  /**
   * Order로부터 OrderCreatedEvent 생성
   *
   * @param order 생성된 주문
   * @return OrderCreatedEvent
   */
  public static OrderCreatedEvent from(Order order) {
    return new OrderCreatedEvent(
        order.getId(),
        order.getOrderNumber(),
        order.getStatus(),
        List.copyOf(order.getItems()), // 불변 리스트
        order.getOrigin(),
        order.getDestination(),
        order.getDeliveryPolicy(),
        order.getOrderedAt(),
        Instant.now() // 이벤트 발생 시각
        );
  }
}
