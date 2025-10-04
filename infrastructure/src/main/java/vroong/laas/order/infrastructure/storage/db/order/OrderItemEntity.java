package vroong.laas.order.infrastructure.storage.db.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.shared.Money;
import vroong.laas.order.core.domain.shared.Volume;
import vroong.laas.order.core.domain.shared.Weight;
import vroong.laas.order.infrastructure.storage.db.BaseEntity;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemEntity extends BaseEntity {

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "item_name", nullable = false, length = 200)
  private String itemName;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "price", nullable = false, precision = 19, scale = 2)
  private BigDecimal price;

  @Column(name = "category", length = 100)
  private String category;

  @Column(name = "weight", precision = 10, scale = 3)
  private BigDecimal weight;

  @Column(name = "volume_length", precision = 10, scale = 2)
  private BigDecimal volumeLength;

  @Column(name = "volume_width", precision = 10, scale = 2)
  private BigDecimal volumeWidth;

  @Column(name = "volume_height", precision = 10, scale = 2)
  private BigDecimal volumeHeight;

  @Column(name = "volume_cbm", precision = 10, scale = 4)
  private BigDecimal volumeCbm;

  @Builder
  public OrderItemEntity(
      Long orderId,
      String itemName,
      Integer quantity,
      BigDecimal price,
      String category,
      BigDecimal weight,
      BigDecimal volumeLength,
      BigDecimal volumeWidth,
      BigDecimal volumeHeight,
      BigDecimal volumeCbm) {
    this.orderId = orderId;
    this.itemName = itemName;
    this.quantity = quantity;
    this.price = price;
    this.category = category;
    this.weight = weight;
    this.volumeLength = volumeLength;
    this.volumeWidth = volumeWidth;
    this.volumeHeight = volumeHeight;
    this.volumeCbm = volumeCbm;
  }

  // Domain → Entity
  public static OrderItemEntity from(OrderItem item, Long orderId) {
    return OrderItemEntity.builder()
        .orderId(orderId)
        .itemName(item.itemName())
        .quantity(item.quantity())
        .price(item.price().amount())
        .category(item.category())
        .weight(item.weight() != null ? item.weight().value() : null)
        .volumeLength(item.volume() != null ? item.volume().length() : null)
        .volumeWidth(item.volume() != null ? item.volume().width() : null)
        .volumeHeight(item.volume() != null ? item.volume().height() : null)
        .volumeCbm(item.volume() != null ? item.volume().cbm() : null)
        .build();
  }

  // Entity → Domain
  public OrderItem toDomain() {
    Weight weight = this.weight != null ? new Weight(this.weight) : null;

    Volume volume =
        (volumeLength != null && volumeWidth != null && volumeHeight != null)
            ? new Volume(volumeLength, volumeWidth, volumeHeight)
            : null;

    return new OrderItem(itemName, quantity, new Money(price), category, weight, volume);
  }
}

