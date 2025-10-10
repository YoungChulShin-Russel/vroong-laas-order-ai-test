package vroong.laas.order.core.domain.order;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.ToString;
import vroong.laas.order.core.domain.shared.Money;

@Getter
@ToString
public class Order {

  private final Long id;
  private final OrderNumber orderNumber;
  private OrderStatus status;
  private final List<OrderItem> items;
  private final Origin origin;
  private final Destination destination;
  private final DeliveryPolicy deliveryPolicy;
  private final Instant orderedAt;
  private Instant deliveredAt;
  private Instant cancelledAt;

  public Order(
      Long id,
      OrderNumber orderNumber,
      OrderStatus status,
      List<OrderItem> items,
      Origin origin,
      Destination destination,
      DeliveryPolicy deliveryPolicy,
      Instant orderedAt,
      Instant deliveredAt,
      Instant cancelledAt) {
    // 필수 값 체크
    if (id == null) {
      throw new IllegalArgumentException("ID는 필수입니다");
    }
    if (orderNumber == null) {
      throw new IllegalArgumentException("주문번호는 필수입니다");
    }
    if (origin == null) {
      throw new IllegalArgumentException("출발지는 필수입니다");
    }
    if (destination == null) {
      throw new IllegalArgumentException("도착지는 필수입니다");
    }
    if (deliveryPolicy == null) {
      throw new IllegalArgumentException("배송 정책은 필수입니다");
    }

    this.id = id;
    this.orderNumber = orderNumber;
    this.status = status;
    this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    this.origin = origin;
    this.destination = destination;
    this.deliveryPolicy = deliveryPolicy;
    this.orderedAt = orderedAt;
    this.deliveredAt = deliveredAt;
    this.cancelledAt = cancelledAt;
  }

  // 배송 완료 처리 (배송 서비스 이벤트 수신)
  public void markAsDelivered() {
    if (status == OrderStatus.CANCELLED) {
      throw new IllegalStateException("취소된 주문은 배송 완료 처리할 수 없습니다");
    }
    if (status == OrderStatus.DELIVERED) {
      throw new IllegalStateException("이미 배송 완료된 주문입니다");
    }
    this.status = OrderStatus.DELIVERED;
    this.deliveredAt = Instant.now();
  }

  // 주문 취소
  public void cancel() {
    if (status == OrderStatus.DELIVERED) {
      throw new IllegalStateException("배송 완료된 주문은 취소할 수 없습니다");
    }
    if (status == OrderStatus.CANCELLED) {
      throw new IllegalStateException("이미 취소된 주문입니다");
    }
    this.status = OrderStatus.CANCELLED;
    this.cancelledAt = Instant.now();
  }

  // 총 금액 계산
  public Money calculateTotalAmount() {
    return items.stream()
        .map(OrderItem::getTotalPrice)
        .reduce(Money.zero(), Money::add);
  }

  // 불변 리스트 반환
  public List<OrderItem> getItems() {
    return Collections.unmodifiableList(items);
  }
}
