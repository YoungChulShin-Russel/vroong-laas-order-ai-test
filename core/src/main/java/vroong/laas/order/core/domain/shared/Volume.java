package vroong.laas.order.core.domain.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Volume(BigDecimal length, BigDecimal width, BigDecimal height, BigDecimal cbm) {

  public Volume(BigDecimal length, BigDecimal width, BigDecimal height) {
    this(length, width, height, calculateCBM(length, width, height));
    validatePositive(length, "길이");
    validatePositive(width, "너비");
    validatePositive(height, "높이");
  }

  private static BigDecimal calculateCBM(BigDecimal length, BigDecimal width, BigDecimal height) {
    if (length == null || width == null || height == null) {
      return null;
    }
    // cm³ → m³ 변환 (1,000,000으로 나누기)
    return length
        .multiply(width)
        .multiply(height)
        .divide(BigDecimal.valueOf(1_000_000), 4, RoundingMode.HALF_UP);
  }

  private static void validatePositive(BigDecimal value, String fieldName) {
    if (value == null) {
      throw new IllegalArgumentException(fieldName + "는 null일 수 없습니다");
    }
    if (value.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException(fieldName + "는 0보다 커야 합니다");
    }
  }
}

