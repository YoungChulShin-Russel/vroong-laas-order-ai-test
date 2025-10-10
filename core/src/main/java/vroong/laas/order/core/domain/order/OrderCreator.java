package vroong.laas.order.core.domain.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.order.core.domain.order.command.CreateOrderCommand;
import vroong.laas.order.core.domain.order.required.OrderRepository;

/**
 * Order 생성 Domain Service
 *
 * <p>책임:
 * - Order 생성 비즈니스 로직
 * - Order 저장
 *
 * <p>트랜잭션:
 * - Order 저장까지만 트랜잭션 (짧은 TX)
 */
@Service
@RequiredArgsConstructor
public class OrderCreator {

  private final OrderNumberGenerator orderNumberGenerator;
  private final OrderRepository orderRepository;

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

    return savedOrder;
  }
}

