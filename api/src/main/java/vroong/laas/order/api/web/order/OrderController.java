package vroong.laas.order.api.web.order;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import vroong.laas.order.api.web.order.request.CreateOrderRequest;
import vroong.laas.order.api.web.order.response.OrderResponse;
import vroong.laas.order.core.application.order.usecase.CreateOrderUseCase;
import vroong.laas.order.core.domain.order.Order;

/**
 * 주문 Controller
 *
 * <p>주문 관련 HTTP API 제공
 *
 * <p>응답 정책:
 * - 성공: 객체 직접 반환 (OrderResponse 등)
 * - 실패: ProblemDetail 반환 (WebApiControllerAdvice에서 처리)
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

  private final CreateOrderUseCase createOrderUseCase;

  public OrderController(CreateOrderUseCase createOrderUseCase) {
    this.createOrderUseCase = createOrderUseCase;
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
}
