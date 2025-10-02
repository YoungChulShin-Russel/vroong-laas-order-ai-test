package vroong.laas.order.core.domain.order;

/**
 * 주문번호 Value Object
 *
 * <p>주문을 고유하게 식별하는 번호
 *
 * <p>포맷 규칙: "ORD-"로 시작해야 함
 */
public record OrderNumber(String value) {

  private static final String PREFIX = "ORD-";

  public OrderNumber {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("주문번호는 필수입니다");
    }
    if (!value.startsWith(PREFIX)) {
      throw new IllegalArgumentException("주문번호는 'ORD-'로 시작해야 합니다");
    }
  }

  public static OrderNumber of(String value) {
    return new OrderNumber(value);
  }

  @Override
  public String toString() {
    return value;
  }
}

