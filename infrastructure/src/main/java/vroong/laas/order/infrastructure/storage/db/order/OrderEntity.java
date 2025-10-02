package vroong.laas.order.infrastructure.storage.db.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vroong.laas.order.infrastructure.storage.db.ConcurrentEntity;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity extends ConcurrentEntity {

  @Column(name = "order_number", nullable = false, unique = true, length = 50)
  private String orderNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private OrderStatus status;

  @Column(name = "ordered_at", nullable = false)
  private Instant orderedAt;

  @Column(name = "delivered_at")
  private Instant deliveredAt;

  @Column(name = "cancelled_at")
  private Instant cancelledAt;

  @Builder
  public OrderEntity(
      String orderNumber,
      OrderStatus status,
      Instant orderedAt,
      Instant deliveredAt,
      Instant cancelledAt) {
    this.orderNumber = orderNumber;
    this.status = status;
    this.orderedAt = orderedAt;
    this.deliveredAt = deliveredAt;
    this.cancelledAt = cancelledAt;
  }

  // TODO: Domain 생성 후 구현
  // public static OrderEntity from(Order order) { }
  // public Order toDomain() { }
}

