package vroong.laas.order.core.domain.order.required;

import vroong.laas.order.core.domain.order.Order;

/**
 * Order 영속성 Port (쓰기)
 *
 * <p>주문 저장을 담당하는 Port입니다.
 * Infrastructure Layer에서 Adapter로 구현됩니다.
 */
public interface OrderStore {

  /**
   * 주문을 저장합니다.
   *
   * @param order 저장할 주문 (ID가 null이면 신규, 있으면 수정)
   * @return 저장된 주문 (ID가 할당됨)
   */
  Order save(Order order);
}


