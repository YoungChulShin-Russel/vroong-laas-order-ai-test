package vroong.laas.order.api.web.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import vroong.laas.order.core.domain.order.OrderItem;

/**
 * 주문 아이템 DTO
 */
public record OrderItemDto(
    @NotBlank(message = "상품명은 필수입니다")
    String itemName,

    @Positive(message = "수량은 1개 이상이어야 합니다")
    int quantity,

    @NotNull(message = "가격은 필수입니다")
    @Positive(message = "가격은 0보다 커야 합니다")
    BigDecimal price,

    @NotBlank(message = "카테고리는 필수입니다")
    String category,

    BigDecimal weightInKg,  // 선택
    BigDecimal widthInCm,   // 선택
    BigDecimal heightInCm,  // 선택
    BigDecimal depthInCm    // 선택
) {

  /** OrderItem Domain → OrderItemDto 변환 */
  public static OrderItemDto from(OrderItem item) {
    return new OrderItemDto(
        item.itemName(),
        item.quantity(),
        item.price().amount(),
        item.category(),
        item.weight() != null ? item.weight().value() : null,
        item.volume() != null ? item.volume().width() : null,
        item.volume() != null ? item.volume().height() : null,
        item.volume() != null ? item.volume().length() : null);
  }
}
