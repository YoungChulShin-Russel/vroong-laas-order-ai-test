package vroong.laas.order.api.web.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import vroong.laas.order.api.web.common.dto.AddressDto;
import vroong.laas.order.api.web.common.dto.ContactDto;
import vroong.laas.order.api.web.common.dto.EntranceInfoDto;
import vroong.laas.order.api.web.common.dto.LatLngDto;
import vroong.laas.order.core.domain.order.Origin;

/**
 * 출발지 DTO
 */
public record OriginDto(
    @NotNull(message = "연락처는 필수입니다")
    @Valid
    ContactDto contact,

    @NotNull(message = "주소는 필수입니다")
    @Valid
    AddressDto address,

    @NotNull(message = "위경도는 필수입니다")
    @Valid
    LatLngDto latLng,

    @Valid
    EntranceInfoDto entranceInfo  // 선택
) {

  /** Origin Domain → OriginDto 변환 */
  public static OriginDto from(Origin origin) {
    return new OriginDto(
        new ContactDto(origin.contact().name(), origin.contact().phoneNumber()),
        new AddressDto(
            origin.address().jibnunAddress(),
            origin.address().roadAddress(),
            origin.address().detailAddress()),
        new LatLngDto(origin.latLng().latitude(), origin.latLng().longitude()),
        origin.entranceInfo() != null
            ? new EntranceInfoDto(
                origin.entranceInfo().password(),
                origin.entranceInfo().guide(),
                origin.entranceInfo().requestMessage())
            : null);
  }
}
