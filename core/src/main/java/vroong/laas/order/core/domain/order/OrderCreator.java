package vroong.laas.order.core.domain.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.order.core.domain.order.command.CreateOrderCommand;
import vroong.laas.order.core.domain.order.event.OrderCreatedEvent;
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
   * @return 생성된 Order (id 할당됨)
   */
  @Transactional
  public Order createOrder(CreateOrderCommand command) {
    // 1. Order 생성
    Order order =
        Order.create(
            orderNumberGenerator,
            command.items(),
            command.origin(),
            command.destination(),
            command.deliveryPolicy());

    // 2. Order 저장
    Order savedOrder = orderRepository.store(order);

    // 3. 이벤트 생성 및 발행
    outboxEventStore.save(OrderCreatedEvent.from(savedOrder));

    return savedOrder;
  }
}
