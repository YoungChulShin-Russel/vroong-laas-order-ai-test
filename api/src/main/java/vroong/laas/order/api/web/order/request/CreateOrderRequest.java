package vroong.laas.order.api.web.order.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import vroong.laas.order.api.web.order.dto.DeliveryPolicyDto;
import vroong.laas.order.api.web.order.dto.DestinationDto;
import vroong.laas.order.api.web.order.dto.OrderItemDto;
import vroong.laas.order.api.web.order.dto.OriginDto;
import vroong.laas.order.core.application.order.command.CreateOrderCommand;
import vroong.laas.order.core.domain.order.OrderItem;

/**
 * 주문 생성 Request
 *
 * <p>설계 원칙:
 * - API Layer DTO (Domain Model과 분리)
 * - Bean Validation으로 형식 검증
 * - 주문번호는 시스템이 자동 생성 (필드 없음)
 * - items는 선택 사항 (null 가능)
 * - 공통 DTO 재사용 (ContactDto, AddressDto, LatLngDto, EntranceInfoDto)
 */
public record CreateOrderRequest(
    @Valid
    List<OrderItemDto> items,  // 선택 사항

    @NotNull(message = "출발지는 필수입니다")
    @Valid
    OriginDto origin,

    @NotNull(message = "도착지는 필수입니다")
    @Valid
    DestinationDto destination,

    @NotNull(message = "배송 정책은 필수입니다")
    @Valid
    DeliveryPolicyDto deliveryPolicy
) {

  /** CreateOrderRequest → CreateOrderCommand 변환 */
  public CreateOrderCommand toCommand() {
    List<OrderItem> orderItems =
        items != null
            ? items.stream().map(OrderItemDto::toDomain).toList()
            : List.of();

    return new CreateOrderCommand(
        orderItems,
        origin.toDomain(),
        destination.toDomain(),
        deliveryPolicy.toDomain());
  }
}
