package vroong.laas.order.core.domain.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.order.core.domain.order.command.CreateOrderCommand;
import vroong.laas.order.core.domain.order.required.OrderRepository;
import vroong.laas.order.core.domain.outbox.OutboxEventStore;

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
 */
@Service
@RequiredArgsConstructor
public class OrderCreator {

  private final OrderNumberGenerator orderNumberGenerator;
  private final OrderRepository orderRepository;
  private final OutboxEventStore outboxEventStore;

  /**
   * 주문 생성
   *
   * @param command 주문 생성 Command
   * @return 생성된 Order (id 할당됨, 도메인 이벤트는 발행 후 초기화됨)
   */
  @Transactional
  public Order create(CreateOrderCommand command) {
    // 1. 주문번호 생성
    OrderNumber orderNumber = orderNumberGenerator.generate();

    // 2. Order 저장 (Order.create()가 내부에서 호출되어 도메인 이벤트 자동 추가)
    Order savedOrder =
        orderRepository.store(
            orderNumber,
            command.items(),
            command.origin(),
            command.destination(),
            command.deliveryPolicy());

    // 3. 도메인 이벤트 발행
    savedOrder.getDomainEvents().forEach(outboxEventStore::save);

    return savedOrder;
  }
}
