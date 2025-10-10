package vroong.laas.order.core.domain.order.required;

import java.util.List;
import java.util.Optional;
import vroong.laas.order.core.domain.order.DeliveryPolicy;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.order.OrderNumber;
import vroong.laas.order.core.domain.order.Origin;

/**
 * Order Repository Port
 *
 * <p>Order 영속화를 담당하는 Port입니다.
 *
 * <p>책임:
 * - Order 저장 (생성 및 수정)
 * - Order 조회
 * - Order 삭제
 *
 * <p>Infrastructure에서 Adapter로 구현됩니다.
 */
public interface OrderRepository {

  /**
   * Order 생성 및 저장
   *
   * <p>Order Entity를 생성하고 저장한 후, 완전한 Order 모델(id 포함)을 반환합니다.
   *
   * @param orderNumber 주문번호
   * @param items 주문 아이템 목록
   * @param origin 출발지
   * @param destination 도착지
   * @param deliveryPolicy 배송 정책
   * @return 저장된 Order (id 할당됨)
   */
  Order store(
      OrderNumber orderNumber,
      List<OrderItem> items,
      Origin origin,
      Destination destination,
      DeliveryPolicy deliveryPolicy);

  /**
   * ID로 Order 조회
   *
   * @param id Order ID
   * @return Order (Optional)
   */
  Optional<Order> findById(Long id);

  /**
   * 주문번호로 Order 조회
   *
   * @param orderNumber 주문번호
   * @return Order (Optional)
   */
  Optional<Order> findByOrderNumber(OrderNumber orderNumber);

  /**
   * 주문번호 존재 여부 확인
   *
   * @param orderNumber 주문번호
   * @return 존재 여부
   */
  boolean existsByOrderNumber(OrderNumber orderNumber);
}
