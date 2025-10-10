package vroong.laas.order.core.application.order;

import lombok.RequiredArgsConstructor;
import vroong.laas.order.core.common.annotation.Facade;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderCreator;
import vroong.laas.order.core.domain.order.OrderReader;
import vroong.laas.order.core.domain.order.command.CreateOrderCommand;

/**
 * Order Facade
 *
 * <p>책임:
 * - API 진입점
 * - Domain Service 호출 (OrderCreator, OrderReader)
 * - 외부 서비스 조합 (나중에 추가)
 *
 * <p>트랜잭션:
 * - 트랜잭션 없음 (Domain Service에서 관리)
 */
@Facade
@RequiredArgsConstructor
public class OrderFacade {

  private final OrderCreator orderCreator;
  private final OrderReader orderReader;

  /**
   * 주문 생성
   *
   * @param command 주문 생성 Command
   * @return Order
   */
  public Order createOrder(CreateOrderCommand command) {
    // 1. Order 생성 및 저장 (TX 내부)
    Order savedOrder = orderCreator.createOrder(command);

    // 2. 외부 서비스 호출 (TX 외부) - 나중에 추가
    // - 이벤트 발행
    // - 알림 전송

    return savedOrder;
  }

  /**
   * ID로 Order 조회
   *
   * @param orderId Order ID
   * @return Order
   */
  public Order getOrderById(Long orderId) {
    return orderReader.getOrderById(orderId);
  }

  /**
   * 주문번호로 Order 조회
   *
   * @param orderNumber 주문번호
   * @return Order
   */
  public Order getOrderByNumber(String orderNumber) {
    return orderReader.getOrderByNumber(orderNumber);
  }
}

