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
import vroong.laas.order.core.application.order.query.GetOrderByIdQuery;
import vroong.laas.order.core.application.order.query.GetOrderByNumberQuery;
import vroong.laas.order.core.application.order.usecase.CreateOrderUseCase;
import vroong.laas.order.core.application.order.usecase.GetOrderByIdUseCase;
import vroong.laas.order.core.application.order.usecase.GetOrderByNumberUseCase;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderNumber;

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

  private final CreateOrderUseCase createOrderUseCase;
  private final GetOrderByIdUseCase getOrderByIdUseCase;
  private final GetOrderByNumberUseCase getOrderByNumberUseCase;

  public OrderController(
      CreateOrderUseCase createOrderUseCase,
      GetOrderByIdUseCase getOrderByIdUseCase,
      GetOrderByNumberUseCase getOrderByNumberUseCase) {
    this.createOrderUseCase = createOrderUseCase;
    this.getOrderByIdUseCase = getOrderByIdUseCase;
    this.getOrderByNumberUseCase = getOrderByNumberUseCase;
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

    // UseCase 실행
    Order order = createOrderUseCase.execute(request.toCommand());

    // Domain → Response DTO 변환 및 반환
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

    // Query 생성 및 UseCase 실행
    GetOrderByIdQuery query = new GetOrderByIdQuery(orderId);
    Order order = getOrderByIdUseCase.execute(query);

    // Domain → Response DTO 변환 및 반환
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

    // String → OrderNumber (Domain Value Object) 변환
    // Query 생성 및 UseCase 실행
    GetOrderByNumberQuery query = new GetOrderByNumberQuery(OrderNumber.of(orderNumber));
    Order order = getOrderByNumberUseCase.execute(query);

    // Domain → Response DTO 변환 및 반환
    return OrderResponse.from(order);
  }
}
