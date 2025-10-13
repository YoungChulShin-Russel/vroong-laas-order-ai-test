package vroong.laas.order.core.domain.order.required;

import java.util.List;
import java.util.Optional;
import vroong.laas.order.core.domain.order.DeliveryPolicy;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.EntranceInfo;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.order.OrderNumber;
import vroong.laas.order.core.domain.order.Origin;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.LatLng;

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

  /**
   * 도착지 주소 업데이트
   *
   * <p>주문의 도착지 주소만 업데이트합니다.
   *
   * <p>변경 범위:
   * - Address (주소)
   * - LatLng (위경도)
   * - EntranceInfo (출입 가이드)
   *
   * <p>유지되는 것:
   * - Contact (연락처) - 변경되지 않음
   *
   * @param orderId 주문 ID
   * @param newAddress 새로운 주소
   * @param newLatLng 새로운 위경도
   * @param newEntranceInfo 새로운 출입 정보
   */
  void updateDestinationAddress(
      Long orderId, Address newAddress, LatLng newLatLng, EntranceInfo newEntranceInfo);
}
