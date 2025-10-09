package vroong.laas.order.core.domain.order.event;

import java.time.Instant;
import java.util.List;
import vroong.laas.order.core.domain.order.DeliveryPolicy;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.order.OrderStatus;
import vroong.laas.order.core.domain.order.Origin;
import vroong.laas.order.core.domain.shared.Money;
import vroong.laas.order.core.domain.shared.event.DomainEvent;

/**
 * 주문 생성 Domain Event
 *
 * <p>Order가 생성되었을 때 발행되는 이벤트입니다.
 *
 * <p>설계 원칙:
 * - Domain Model을 그대로 사용 (DTO 변환은 Infrastructure에서)
 * - Order의 모든 정보를 포함하여 다른 서비스가 독립적으로 처리 가능
 *
 * <p>Event 발행 시점:
 * - Order.create() 후 저장이 완료된 시점
 * - CreateOrderUseCase에서 발행
 *
 * <p>Event 소비자:
 * - 배송 서비스 (배송 생성)
 * - 알림 서비스 (주문 확인 알림)
 * - 분석 서비스 (주문 통계)
 */
public record OrderCreatedEvent(
    Instant occurredAt,
    
    // Order 기본 정보
    Long orderId,
    String orderNumber,
    OrderStatus status,
    Instant orderedAt,
    
    // Order Items
    List<OrderItem> items,
    Money totalAmount,
    
    // Origin (출발지)
    Origin origin,
    
    // Destination (도착지)
    Destination destination,
    
    // Delivery Policy
    DeliveryPolicy deliveryPolicy
) implements DomainEvent {

  /**
   * Order로부터 OrderCreatedEvent 생성
   *
   * @param order 생성된 Order
   * @return OrderCreatedEvent
   */
  public static OrderCreatedEvent from(Order order) {
    if (order.getId() == null) {
      throw new IllegalArgumentException("저장되지 않은 Order로는 Event를 생성할 수 없습니다");
    }

    return new OrderCreatedEvent(
        Instant.now(),
        order.getId(),
        order.getOrderNumber().value(),
        order.getStatus(),
        order.getOrderedAt(),
        order.getItems(),
        order.calculateTotalAmount(),
        order.getOrigin(),
        order.getDestination(),
        order.getDeliveryPolicy()
    );
  }
}

