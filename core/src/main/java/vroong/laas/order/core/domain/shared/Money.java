package vroong.laas.order.core.domain.shared;

import java.math.BigDecimal;

public record Money(BigDecimal amount) {

  public Money {
    if (amount == null) {
      throw new IllegalArgumentException("금액은 null일 수 없습니다");
    }
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("금액은 음수일 수 없습니다");
    }
  }

  public static Money zero() {
    return new Money(BigDecimal.ZERO);
  }

  public Money add(Money other) {
    return new Money(this.amount.add(other.amount));
  }

  public Money multiply(int quantity) {
    return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)));
  }
}

