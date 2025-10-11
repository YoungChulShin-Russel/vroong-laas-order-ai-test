package vroong.laas.order.api.web.shared.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import vroong.laas.order.core.domain.shared.LatLng;

/**
 * 위경도 DTO
 *
 * <p>공통으로 사용되는 위경도 정보
 */
public record LatLngDto(
    @NotNull(message = "위도는 필수입니다")
    BigDecimal latitude,

    @NotNull(message = "경도는 필수입니다")
    BigDecimal longitude
) {

  /** LatLngDto → LatLng Domain 변환 */
  public LatLng toDomain() {
    return new LatLng(latitude, longitude);
  }
}
