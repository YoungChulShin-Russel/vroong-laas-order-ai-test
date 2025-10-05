package vroong.laas.order.api.web.common.dto;

import jakarta.validation.constraints.NotBlank;

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
) {}
