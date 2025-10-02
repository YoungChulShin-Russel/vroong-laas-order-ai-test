package vroong.laas.order.core.domain.shared;

import java.math.BigDecimal;

public record LatLng(BigDecimal latitude, BigDecimal longitude) {

  public LatLng {
    validateLatitude(latitude);
    validateLongitude(longitude);
  }

  private static void validateLatitude(BigDecimal latitude) {
    if (latitude == null) {
      throw new IllegalArgumentException("위도는 필수입니다");
    }
    if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0
        || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
      throw new IllegalArgumentException("위도는 -90 ~ 90 사이여야 합니다");
    }
  }

  private static void validateLongitude(BigDecimal longitude) {
    if (longitude == null) {
      throw new IllegalArgumentException("경도는 필수입니다");
    }
    if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0
        || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
      throw new IllegalArgumentException("경도는 -180 ~ 180 사이여야 합니다");
    }
  }
}

