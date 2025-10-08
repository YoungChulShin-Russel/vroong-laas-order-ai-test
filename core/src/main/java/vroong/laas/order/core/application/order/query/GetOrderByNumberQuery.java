package vroong.laas.order.core.application.order.query;

import vroong.laas.order.core.domain.order.OrderNumber;

/**
 * 주문번호로 주문 조회 Query
 *
 * @param orderNumber 주문번호 (Domain Value Object)
 */
public record GetOrderByNumberQuery(OrderNumber orderNumber) {

  public GetOrderByNumberQuery {
    if (orderNumber == null) {
      throw new IllegalArgumentException("주문번호는 필수입니다");
    }
  }
}
