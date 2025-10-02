package vroong.laas.order.core.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoneyTest {

  @Test
  @DisplayName("금액을 생성한다")
  void createMoney() {
    // given & when
    Money money = new Money(BigDecimal.valueOf(10_000));

    // then
    assertThat(money.amount()).isEqualByComparingTo(BigDecimal.valueOf(10_000));
  }

  @Test
  @DisplayName("0원을 생성한다")
  void createZero() {
    // given & when
    Money money = Money.zero();

    // then
    assertThat(money.amount()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  @DisplayName("금액이 null이면 예외가 발생한다")
  void createMoneyWithNull() {
    // when & then
    assertThatThrownBy(() -> new Money(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("금액은 null일 수 없습니다");
  }

  @Test
  @DisplayName("금액이 음수이면 예외가 발생한다")
  void createMoneyWithNegative() {
    // when & then
    assertThatThrownBy(() -> new Money(BigDecimal.valueOf(-1000)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("금액은 음수일 수 없습니다");
  }

  @Test
  @DisplayName("금액을 더한다")
  void addMoney() {
    // given
    Money money1 = new Money(BigDecimal.valueOf(10_000));
    Money money2 = new Money(BigDecimal.valueOf(5_000));

    // when
    Money result = money1.add(money2);

    // then
    assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(15_000));
  }

  @Test
  @DisplayName("금액에 수량을 곱한다")
  void multiplyMoney() {
    // given
    Money money = new Money(BigDecimal.valueOf(10_000));

    // when
    Money result = money.multiply(3);

    // then
    assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(30_000));
  }

  @Test
  @DisplayName("금액 객체는 불변이다")
  void moneyIsImmutable() {
    // given
    Money original = new Money(BigDecimal.valueOf(10_000));

    // when
    Money added = original.add(new Money(BigDecimal.valueOf(5_000)));
    Money multiplied = original.multiply(2);

    // then
    assertThat(original.amount()).isEqualByComparingTo(BigDecimal.valueOf(10_000));
    assertThat(added.amount()).isEqualByComparingTo(BigDecimal.valueOf(15_000));
    assertThat(multiplied.amount()).isEqualByComparingTo(BigDecimal.valueOf(20_000));
  }
}

