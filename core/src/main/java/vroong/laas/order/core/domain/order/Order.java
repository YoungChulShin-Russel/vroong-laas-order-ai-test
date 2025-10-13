package vroong.laas.order.core.domain.order;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.ToString;
import vroong.laas.order.core.domain.order.event.OrderCreatedEvent;
import vroong.laas.order.core.domain.order.event.OrderDestinationAddressChangedEvent;
import vroong.laas.order.core.domain.order.exception.OrderLocationChangeNotAllowedException;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.AggregateRoot;
import vroong.laas.order.core.domain.shared.LatLng;

@Getter
@ToString
public class Order extends AggregateRoot {

  private final Long id;
  private final OrderNumber orderNumber;
  private OrderStatus status;
  private final List<OrderItem> items;
  private final Origin origin;
  private Destination destination;
  private final DeliveryPolicy deliveryPolicy;
  private final Instant orderedAt;
  private Instant deliveredAt;
  private Instant cancelledAt;

  /**
   * 생성자 (순수 객체 생성)
   *
   * <p>Infrastructure에서 DB 데이터 복원 시 사용
   * <p>테스트에서 다양한 상태의 Order 생성 시 사용
   * <p>도메인 이벤트를 추가하지 않음
   */
  public Order(
      Long id,
      OrderNumber orderNumber,
      OrderStatus status,
      List<OrderItem> items,
      Origin origin,
      Destination destination,
      DeliveryPolicy deliveryPolicy,
      Instant orderedAt,
      Instant deliveredAt,
      Instant cancelledAt) {
    // 필수 값 체크
    if (id == null) {
      throw new IllegalArgumentException("ID는 필수입니다");
    }
    if (orderNumber == null) {
      throw new IllegalArgumentException("주문번호는 필수입니다");
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

    this.id = id;
    this.orderNumber = orderNumber;
    this.status = status;
    this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    this.origin = origin;
    this.destination = destination;
    this.deliveryPolicy = deliveryPolicy;
    this.orderedAt = orderedAt;
    this.deliveredAt = deliveredAt;
    this.cancelledAt = cancelledAt;
  }

  /**
   * 주문 생성 (팩토리 메서드)
   *
   * <p>프로덕션 코드에서 신규 주문 생성 시 사용
   * <p>비즈니스 규칙 검증 및 도메인 이벤트 자동 추가
   *
   * @param id 주문 ID (DB에서 생성된 ID)
   * @param orderNumber 주문번호
   * @param items 주문 아이템 목록
   * @param origin 출발지
   * @param destination 도착지
   * @param deliveryPolicy 배송 정책
   * @return 생성된 Order (OrderCreatedEvent 포함)
   */
  public static Order create(
      Long id,
      OrderNumber orderNumber,
      List<OrderItem> items,
      Origin origin,
      Destination destination,
      DeliveryPolicy deliveryPolicy) {

    Order order =
        new Order(
            id,
            orderNumber,
            OrderStatus.CREATED,
            items,
            origin,
            destination,
            deliveryPolicy,
            Instant.now(),
            null,
            null);

    // 도메인 이벤트 추가
    order.addDomainEvent(OrderCreatedEvent.from(order));

    return order;
  }

  // 불변 리스트 반환
  public List<OrderItem> getItems() {
    return Collections.unmodifiableList(items);
  }

  /**
   * 도착지 주소 변경
   *
   * <p>비즈니스 규칙:
   * - CREATED 상태에서만 도착지 주소 변경 가능
   * - 변경 시 OrderDestinationAddressChangedEvent 발행
   *
   * <p>변경 범위:
   * - Address (주소)
   * - LatLng (위경도)
   * - EntranceInfo (출입 가이드)
   *
   * <p>유지되는 것:
   * - Contact (연락처) - 변경되지 않음
   *
   * @param newAddress 새로운 주소
   * @param newLatLng 새로운 위경도
   * @param newEntranceInfo 새로운 출입 정보
   * @throws OrderLocationChangeNotAllowedException CREATED 상태가 아닐 때
   */
  public void changeDestinationAddress(
      Address newAddress, LatLng newLatLng, EntranceInfo newEntranceInfo) {
    // 상태 검증: CREATED 상태에서만 변경 가능
    if (this.status != OrderStatus.CREATED) {
      throw new OrderLocationChangeNotAllowedException(
          "CREATED 상태에서만 도착지 주소를 변경할 수 있습니다. 현재 상태: " + this.status);
    }

    // 기존 도착지 백업 (이벤트용)
    Destination oldDestination = this.destination;

    // 기존 Contact 유지하면서 새로운 Destination 생성
    Destination newDestination =
        new Destination(
            this.destination.contact(), // 기존 Contact 유지
            newAddress,
            newLatLng,
            newEntranceInfo);

    // 도착지 변경
    this.destination = newDestination;

    // 도메인 이벤트 추가
    this.addDomainEvent(
        OrderDestinationAddressChangedEvent.of(
            this.id, oldDestination, newDestination, Instant.now()));
  }
}
