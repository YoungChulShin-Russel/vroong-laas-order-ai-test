package vroong.laas.order.api.web.order.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import vroong.laas.order.api.web.shared.dto.AddressDto;
import vroong.laas.order.api.web.shared.dto.EntranceInfoDto;
import vroong.laas.order.api.web.shared.dto.LatLngDto;
import vroong.laas.order.core.domain.order.command.ChangeDestinationAddressCommand;

/**
 * 도착지 주소 변경 Request
 *
 * <p>설계 원칙:
 * - API Layer DTO (Domain Model과 분리)
 * - Bean Validation으로 형식 검증
 * - orderId는 Path Variable로 전달 (필드 없음)
 * - 공통 DTO 재사용 (AddressDto, LatLngDto, EntranceInfoDto)
 *
 * <p>변경 범위:
 * - Address (주소)
 * - LatLng (위경도)
 * - EntranceInfo (출입 가이드)
 *
 * <p>유지되는 것:
 * - Contact (연락처) - 변경되지 않음
 */
public record ChangeDestinationAddressRequest(
    @NotNull(message = "주소는 필수입니다") @Valid AddressDto address,
    @NotNull(message = "위경도는 필수입니다") @Valid LatLngDto latLng,
    @NotNull(message = "출입 정보는 필수입니다") @Valid EntranceInfoDto entranceInfo) {

  /**
   * ChangeDestinationAddressRequest → ChangeDestinationAddressCommand 변환
   *
   * @param orderId Path Variable로 전달받은 주문 ID
   * @return ChangeDestinationAddressCommand
   */
  public ChangeDestinationAddressCommand toCommand(Long orderId) {
    return new ChangeDestinationAddressCommand(
        orderId, address.toDomain(), latLng.toDomain(), entranceInfo.toDomain());
  }
}

