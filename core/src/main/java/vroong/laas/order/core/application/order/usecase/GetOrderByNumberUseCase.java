package vroong.laas.order.core.application.order.usecase;

import lombok.RequiredArgsConstructor;
import vroong.laas.order.core.application.order.query.GetOrderByNumberQuery;
import vroong.laas.order.core.common.annotation.UseCase;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.exception.OrderNotFoundException;
import vroong.laas.order.core.domain.order.required.OrderReader;

/**
 * 주문번호로 주문 조회 UseCase
 *
 * <p>주문번호로 주문을 조회합니다.
 *
 * <p>흐름: 1. OrderReader.findByOrderNumber() 호출 2. 주문이 없으면 OrderNotFoundException 발생
 */
@UseCase
@RequiredArgsConstructor
public class GetOrderByNumberUseCase {

  private final OrderReader orderReader;

  /**
   * 주문을 조회합니다.
   *
   * @param query 조회 Query
   * @return 조회된 주문
   * @throws OrderNotFoundException 주문이 없는 경우
   */
  public Order execute(GetOrderByNumberQuery query) {
    return orderReader
        .findByOrderNumber(query.orderNumber())
        .orElseThrow(() -> new OrderNotFoundException(query.orderNumber()));
  }
}
