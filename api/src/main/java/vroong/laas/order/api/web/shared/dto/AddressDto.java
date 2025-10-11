package vroong.laas.order.api.web.shared.dto;

import jakarta.validation.constraints.NotBlank;
import vroong.laas.order.core.domain.shared.Address;

/**
 * 주소 DTO
 *
 * <p>공통으로 사용되는 주소 정보
 */
public record AddressDto(
    String jibnunAddress,  // 선택

    @NotBlank(message = "도로명 주소는 필수입니다")
    String roadAddress,

    String detailAddress  // 선택
) {

  /** AddressDto → Address Domain 변환 */
  public Address toDomain() {
    return new Address(jibnunAddress, roadAddress, detailAddress);
  }
}
