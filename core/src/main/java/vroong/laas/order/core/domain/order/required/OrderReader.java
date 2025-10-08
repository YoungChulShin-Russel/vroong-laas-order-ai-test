package vroong.laas.order.core.domain.order.required;

import java.util.Optional;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderNumber;

/**
 * Order 영속성 Port (읽기)
 *
 * <p>주문 조회 및 검색을 담당하는 Port입니다. Infrastructure Layer에서 Adapter로 구현됩니다.
 *
 * <p>필요한 조회 메서드는 점진적으로 추가합니다.
 */
public interface OrderReader {

  /**
   * ID로 주문 조회
   *
   * @param orderId 주문 ID
   * @return 주문 (없으면 Optional.empty())
   */
  Optional<Order> findById(Long orderId);

  /**
   * 주문번호로 주문 조회
   *
   * @param orderNumber 주문번호 (Domain Value Object)
   * @return 주문 (없으면 Optional.empty())
   */
  Optional<Order> findByOrderNumber(OrderNumber orderNumber);
}


