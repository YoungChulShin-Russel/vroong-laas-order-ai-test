package vroong.laas.order.core.domain.order.required;

import java.util.Optional;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderNumber;

/**
 * Order 영속화 Port
 *
 * <p>주문의 저장, 조회, 삭제를 담당합니다.
 * <p>실제 구현은 Infrastructure Layer의 Adapter가 담당합니다.
 *
 * <p>기존 OrderStore와 OrderReader를 통합한 Port입니다.
 */
public interface OrderRepository {

  // === 저장 ===

  /**
   * Order 저장 (생성 또는 수정)
   *
   * @param order 저장할 Order
   * @return 저장된 Order (id 할당됨)
   */
  Order store(Order order);

  /**
   * Order 삭제
   *
   * @param order 삭제할 Order
   */
  void delete(Order order);

  // === 조회 ===

  /**
   * ID로 Order 조회
   *
   * @param orderId Order ID
   * @return Order (Optional)
   */
  Optional<Order> findById(Long orderId);

  /**
   * 주문번호로 Order 조회
   *
   * @param orderNumber 주문번호 (Domain Value Object)
   * @return Order (Optional)
   */
  Optional<Order> findByOrderNumber(OrderNumber orderNumber);

  /**
   * 주문번호 존재 여부 확인
   *
   * @param orderNumber 주문번호 (Domain Value Object)
   * @return 존재 여부
   */
  boolean existsByOrderNumber(OrderNumber orderNumber);
}

