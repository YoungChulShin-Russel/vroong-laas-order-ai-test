package vroong.laas.order.core.application.order.usecase;

import lombok.RequiredArgsConstructor;
import vroong.laas.order.core.application.order.query.GetOrderByIdQuery;
import vroong.laas.order.core.common.annotation.UseCase;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.exception.OrderNotFoundException;
import vroong.laas.order.core.domain.order.required.OrderReader;

/**
 * ID로 주문 조회 UseCase
 *
 * <p>주문 ID로 주문을 조회합니다.
 *
 * <p>흐름: 1. OrderReader.findById() 호출 2. 주문이 없으면 OrderNotFoundException 발생
 */
@UseCase
@RequiredArgsConstructor
public class GetOrderByIdUseCase {

  private final OrderReader orderReader;

  /**
   * 주문을 조회합니다.
   *
   * @param query 조회 Query
   * @return 조회된 주문
   * @throws OrderNotFoundException 주문이 없는 경우
   */
  public Order execute(GetOrderByIdQuery query) {
    return orderReader
        .findById(query.orderId())
        .orElseThrow(() -> new OrderNotFoundException(query.orderId()));
  }
}
