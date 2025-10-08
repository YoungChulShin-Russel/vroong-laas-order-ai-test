package vroong.laas.order.core.application.order.query;

/**
 * ID로 주문 조회 Query
 *
 * @param orderId 주문 ID
 */
public record GetOrderByIdQuery(Long orderId) {

  public GetOrderByIdQuery {
    if (orderId == null || orderId <= 0) {
      throw new IllegalArgumentException("주문 ID는 필수이며 양수여야 합니다");
    }
  }
}
