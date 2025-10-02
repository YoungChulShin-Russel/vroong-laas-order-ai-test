package vroong.laas.order.core.domain.shared;

import java.math.BigDecimal;

public record Weight(BigDecimal value) {

  public Weight {
    if (value == null) {
      throw new IllegalArgumentException("무게는 null일 수 없습니다");
    }
    if (value.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("무게는 음수일 수 없습니다");
    }
  }
}

