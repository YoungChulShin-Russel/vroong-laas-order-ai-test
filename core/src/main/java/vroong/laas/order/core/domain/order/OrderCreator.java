package vroong.laas.order.core.domain.order;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.order.core.domain.order.required.OrderRepository;
import vroong.laas.order.core.domain.outbox.OutboxEventAppender;
import vroong.laas.order.core.domain.outbox.OutboxEventType;

/**
 * Order 생성 Domain Service
 *
 * <p>책임:
 * - Order 생성 비즈니스 로직
 * - Order 저장
 * - Domain Event 발행
 *
 * <p>트랜잭션:
 * - Order 저장 + Outbox 저장까지 하나의 트랜잭션 (원자성 보장)
 *
 * <p>주소 정제:
 * - OrderCreator는 이미 정제된 주소를 받아서 생성에만 집중
 * - 주소 정제는 OrderFacade에서 AddressRefiner를 통해 수행
 */
@Service
@RequiredArgsConstructor
public class OrderCreator {

  private final OrderNumberGenerator orderNumberGenerator;
  private final OrderRepository orderRepository;
  private final OutboxEventAppender outboxEventAppender;

  /**
   * 주문 생성 (이미 정제된 주소로)
   *
   * <p>OrderFacade에서 주소 정제 후 호출됩니다.
   *
   * @param items 주문 아이템 목록
   * @param origin 정제된 출발지
   * @param destination 정제된 도착지
   * @param deliveryPolicy 배송 정책
   * @return 생성된 Order (id 할당됨, 도메인 이벤트는 발행 후 초기화됨)
   */
  @Transactional
  public Order create(
      List<OrderItem> items,
      Origin origin,
      Destination destination,
      DeliveryPolicy deliveryPolicy) {

    // 1. 주문번호 생성
    OrderNumber orderNumber = orderNumberGenerator.generate();

    // 2. Order 저장 (Order.create()가 내부에서 호출되어 도메인 이벤트 자동 추가)
    Order order =
        orderRepository.store(orderNumber, items, origin, destination, deliveryPolicy);

    // 3. 도메인 이벤트 발행
    outboxEventAppender.append(OutboxEventType.ORDER_CREATED, order);

    return order;
  }
}
