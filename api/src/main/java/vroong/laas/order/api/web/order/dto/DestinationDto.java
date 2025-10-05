package vroong.laas.order.api.web.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import vroong.laas.order.api.web.common.dto.AddressDto;
import vroong.laas.order.api.web.common.dto.ContactDto;
import vroong.laas.order.api.web.common.dto.EntranceInfoDto;
import vroong.laas.order.api.web.common.dto.LatLngDto;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.EntranceInfo;

/**
 * 도착지 DTO
 */
public record DestinationDto(
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

  /** Destination Domain → DestinationDto 변환 */
  public static DestinationDto from(Destination destination) {
    return new DestinationDto(
        new ContactDto(
            destination.contact().name(), destination.contact().phoneNumber()),
        new AddressDto(
            destination.address().jibnunAddress(),
            destination.address().roadAddress(),
            destination.address().detailAddress()),
        new LatLngDto(
            destination.latLng().latitude(), destination.latLng().longitude()),
        destination.entranceInfo() != null
            ? new EntranceInfoDto(
                destination.entranceInfo().password(),
                destination.entranceInfo().guide(),
                destination.entranceInfo().requestMessage())
            : null);
  }

  /** DestinationDto → Destination Domain 변환 */
  public Destination toDomain() {
    return new Destination(
        contact.toDomain(),
        address.toDomain(),
        latLng.toDomain(),
        entranceInfo != null ? entranceInfo.toDomain() : EntranceInfo.empty());
  }
}
