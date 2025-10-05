package vroong.laas.order.api.web.order;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vroong.laas.order.api.web.order.dto.request.CreateOrderRequest;
import vroong.laas.order.api.web.order.dto.response.OrderResponse;
import vroong.laas.order.core.application.order.usecase.CreateOrderUseCase;
import vroong.laas.order.core.domain.order.Order;

/**
 * 주문 Controller
 *
 * <p>주문 관련 HTTP API 제공
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
  public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid CreateOrderRequest request) {

    // UseCase 실행
    Order order = createOrderUseCase.execute(request.toCommand());

    // Domain → Response DTO 변환
    OrderResponse response = OrderResponse.from(order);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
