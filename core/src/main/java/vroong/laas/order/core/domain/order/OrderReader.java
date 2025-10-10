package vroong.laas.order.core.domain.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.order.core.domain.order.exception.OrderNotFoundException;
import vroong.laas.order.core.domain.order.required.OrderRepository;

/**
 * Order 조회 Domain Service
 *
 * <p>책임:
 * - Order 조회 비즈니스 로직
 * - 조회 결과 검증
 *
 * <p>트랜잭션:
 * - readOnly = true (조회 최적화)
 */
@Service
@RequiredArgsConstructor
public class OrderReader {

  private final OrderRepository orderRepository;

  /**
   * ID로 Order 조회
   *
   * @param orderId Order ID
   * @return Order
   * @throws OrderNotFoundException Order를 찾을 수 없는 경우
   */
  @Transactional(readOnly = true)
  public Order getOrderById(Long orderId) {
    return orderRepository
        .findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
  }

  /**
   * 주문번호로 Order 조회
   *
   * @param orderNumber 주문번호 (String)
   * @return Order
   * @throws OrderNotFoundException Order를 찾을 수 없는 경우
   */
  @Transactional(readOnly = true)
  public Order getOrderByNumber(String orderNumber) {
    OrderNumber orderNumberVO = new OrderNumber(orderNumber);
    return orderRepository
        .findByOrderNumber(orderNumberVO)
        .orElseThrow(() -> new OrderNotFoundException(orderNumberVO));
  }
}

