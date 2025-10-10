package vroong.laas.order.api.web.order;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import vroong.laas.order.api.web.order.request.CreateOrderRequest;
import vroong.laas.order.api.web.order.response.OrderResponse;
import vroong.laas.order.core.application.order.OrderFacade;
import vroong.laas.order.core.domain.order.Order;

/**
 * 주문 Controller
 *
 * <p>주문 관련 HTTP API 제공
 *
 * <p>응답 정책: - 성공: 객체 직접 반환 (OrderResponse 등) - 실패: ProblemDetail 반환 (WebApiControllerAdvice에서
 * 처리)
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

  private final OrderFacade orderFacade;

  public OrderController(OrderFacade orderFacade) {
    this.orderFacade = orderFacade;
  }

  /**
   * 주문 생성 API
   *
   * @param request 주문 생성 요청
   * @return 생성된 주문 정보 (HTTP 201 Created)
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OrderResponse createOrder(@RequestBody @Valid CreateOrderRequest request) {

    // Facade 실행
    Order order = orderFacade.createOrder(request.toCommand());

    // Order → Response DTO 변환 및 반환
    return OrderResponse.from(order);
  }

  /**
   * ID로 주문 조회 API
   *
   * @param orderId 주문 ID
   * @return 주문 정보 (HTTP 200 OK)
   */
  @GetMapping("/{orderId}")
  public OrderResponse getOrderById(@PathVariable Long orderId) {

    // Facade 실행
    Order order = orderFacade.getOrderById(orderId);

    // Order → Response DTO 변환 및 반환
    return OrderResponse.from(order);
  }

  /**
   * 주문번호로 주문 조회 API
   *
   * @param orderNumber 주문번호 (String)
   * @return 주문 정보 (HTTP 200 OK)
   */
  @GetMapping("/number/{orderNumber}")
  public OrderResponse getOrderByNumber(@PathVariable String orderNumber) {

    // Facade 실행
    Order order = orderFacade.getOrderByNumber(orderNumber);

    // Order → Response DTO 변환 및 반환
    return OrderResponse.from(order);
  }
}
