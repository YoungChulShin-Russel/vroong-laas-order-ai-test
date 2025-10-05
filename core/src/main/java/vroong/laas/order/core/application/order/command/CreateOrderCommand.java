package vroong.laas.order.core.application.order.command;

import java.util.List;
import vroong.laas.order.core.domain.order.DeliveryPolicy;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.order.Origin;

/**
 * 주문 생성 Command
 *
 * <p>API Layer에서 받은 요청을 Domain Layer로 전달하기 위한 Command입니다.
 *
 * <p>설계 원칙:
 * - 가능하면 Domain Model 사용 (OrderItem, Origin, Destination, DeliveryPolicy 등)
 * - Domain으로 대응이 어려운 경우에만 Command용 DTO 추가
 * - 주문번호는 OrderNumberGenerator가 자동 생성 (사용자 입력 아님)
 */
public record CreateOrderCommand(
    List<OrderItem> items,
    Origin origin,
    Destination destination,
    DeliveryPolicy deliveryPolicy) {

  public CreateOrderCommand {
    // 필수 값 검증
    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException("주문 아이템은 최소 1개 이상이어야 합니다");
    }
    if (origin == null) {
      throw new IllegalArgumentException("출발지는 필수입니다");
    }
    if (destination == null) {
      throw new IllegalArgumentException("도착지는 필수입니다");
    }
    if (deliveryPolicy == null) {
      throw new IllegalArgumentException("배송 정책은 필수입니다");
    }
  }
}
