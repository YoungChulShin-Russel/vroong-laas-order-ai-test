package vroong.laas.order.api.web.order.response;

import java.time.Instant;
import java.util.List;
import vroong.laas.order.api.web.order.dto.DeliveryPolicyDto;
import vroong.laas.order.api.web.order.dto.DestinationDto;
import vroong.laas.order.api.web.order.dto.OrderItemDto;
import vroong.laas.order.api.web.order.dto.OriginDto;
import vroong.laas.order.core.domain.order.Order;

/**
 * 주문 생성 Response
 *
 * <p>Order Domain → Response DTO 변환
 */
public record OrderResponse(
    Long id,
    String orderNumber,
    String status,
    List<OrderItemDto> items,
    OriginDto origin,
    DestinationDto destination,
    DeliveryPolicyDto deliveryPolicy,
    Instant orderedAt,
    Instant deliveredAt,
    Instant cancelledAt
) {

  /**
   * Order Domain → OrderResponse 변환
   */
  public static OrderResponse from(Order order) {
    return new OrderResponse(
        order.getId(),
        order.getOrderNumber().value(),
        order.getStatus().name(),
        order.getItems() != null
            ? order.getItems().stream().map(OrderItemDto::from).toList()
            : List.of(),
        OriginDto.from(order.getOrigin()),
        DestinationDto.from(order.getDestination()),
        DeliveryPolicyDto.from(order.getDeliveryPolicy()),
        order.getOrderedAt(),
        order.getDeliveredAt(),
        order.getCancelledAt());
  }
}
