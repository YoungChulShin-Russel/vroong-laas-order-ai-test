package vroong.laas.order.core.application.order.usecase;

import vroong.laas.order.core.application.order.command.CreateOrderCommand;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.required.OrderStore;

/**
 * 주문 생성 UseCase
 *
 * <p>새로운 주문을 생성하고 저장합니다.
 *
 * <p>흐름:
 * 1. Order.create() 호출 (비즈니스 로직)
 * 2. OrderStore.store() 호출 (영속화)
 *
 * <p>Command는 Domain Model을 그대로 사용하므로 변환 불필요
 */
public class CreateOrderUseCase {

  private final OrderStore orderStore;

  public CreateOrderUseCase(OrderStore orderStore) {
    this.orderStore = orderStore;
  }

  /**
   * 주문을 생성합니다.
   *
   * @param command 주문 생성 Command
   * @return 생성된 주문 (ID 할당됨)
   */
  public Order execute(CreateOrderCommand command) {
    // Domain Model 그대로 사용 (변환 불필요)
    Order order =
        Order.create(
            command.orderNumber(),
            command.items(),
            command.origin(),
            command.destination(),
            command.deliveryPolicy());

    // 저장 (트랜잭션은 OrderStore Adapter에서 관리)
    return orderStore.store(order);
  }
}
