package vroong.laas.order.api.web.shared.dto;

import jakarta.validation.constraints.NotBlank;
import vroong.laas.order.core.domain.shared.Contact;

/**
 * 연락처 DTO
 *
 * <p>공통으로 사용되는 연락처 정보
 */
public record ContactDto(
    @NotBlank(message = "이름은 필수입니다")
    String name,

    @NotBlank(message = "전화번호는 필수입니다")
    String phoneNumber
) {

  /** ContactDto → Contact Domain 변환 */
  public Contact toDomain() {
    return new Contact(name, phoneNumber);
  }
}
