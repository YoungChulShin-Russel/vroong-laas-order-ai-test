package vroong.laas.order.core.application.order;

import lombok.RequiredArgsConstructor;
import vroong.laas.order.core.common.annotation.Facade;
import vroong.laas.order.core.domain.address.AddressRefiner;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderCreator;
import vroong.laas.order.core.domain.order.OrderLocationChanger;
import vroong.laas.order.core.domain.order.OrderReader;
import vroong.laas.order.core.domain.order.Origin;
import vroong.laas.order.core.domain.order.command.ChangeDestinationCommand;
import vroong.laas.order.core.domain.order.command.CreateOrderCommand;

/**
 * Order Facade
 *
 * <p>책임:
 * - API 진입점
 * - 주소 정제 (AddressRefiner)
 * - Domain Service 호출 (OrderCreator, OrderReader)
 * - 외부 서비스 조합
 *
 * <p>흐름:
 * 1. 주소 정제 (Origin/Destination) - AddressRefiner
 * 2. 주문 생성 - OrderCreator
 *
 * <p>트랜잭션:
 * - 트랜잭션 없음 (Domain Service에서 관리)
 */
@Facade
@RequiredArgsConstructor
public class OrderFacade {

  private final OrderCreator orderCreator;
  private final OrderReader orderReader;
  private final OrderLocationChanger orderLocationChanger;
  private final AddressRefiner addressRefiner;

  /**
   * 주문 생성
   *
   * <p>흐름:
   * 1. Origin 주소 정제 (역지오코딩)
   * 2. Destination 주소 정제 (역지오코딩)
   * 3. 정제된 주소로 Order 생성
   *
   * @param command 주문 생성 Command
   * @return 생성된 Order
   * @throws vroong.laas.order.core.domain.address.exception.AddressRefineFailedException 주소 정제 실패 시
   */
  public Order createOrder(CreateOrderCommand command) {
    // 1. 주소 정제 (역지오코딩)
    Origin refinedOrigin = addressRefiner.refineOrigin(command.origin());
    Destination refinedDestination = addressRefiner.refineDestination(command.destination());

    // 2. Order 생성 및 저장 (정제된 주소로)
    return orderCreator.create(
        command.items(), refinedOrigin, refinedDestination, command.deliveryPolicy());
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

  /**
   * 주문 도착지 변경
   *
   * <p>흐름:
   * 1. Order 조회 (빠른 실패 - 주문이 없으면 즉시 실패)
   * 2. Destination 주소 정제 (역지오코딩 - 주문이 있을 때만 실행)
   * 3. 정제된 주소로 도착지 변경
   *
   * @param command 도착지 변경 Command
   * @return 변경된 Order
   * @throws vroong.laas.order.core.domain.order.exception.OrderNotFoundException 주문을 찾을 수 없음
   * @throws vroong.laas.order.core.domain.order.exception.OrderLocationChangeNotAllowedException CREATED 상태가 아님
   * @throws vroong.laas.order.core.domain.address.exception.AddressRefineFailedException 주소 정제 실패 시
   */
  public Order changeDestination(ChangeDestinationCommand command) {
    // 1. Order 조회 먼저 (빠른 실패)
    Order order = orderReader.getOrderById(command.orderId());

    // 2. 주소 정제 (역지오코딩 - Order가 있을 때만)
    Destination refinedDestination = addressRefiner.refineDestination(command.newDestination());

    // 3. 도착지 변경 (정제된 주소로)
    return orderLocationChanger.changeDestination(order, refinedDestination);
  }
}

