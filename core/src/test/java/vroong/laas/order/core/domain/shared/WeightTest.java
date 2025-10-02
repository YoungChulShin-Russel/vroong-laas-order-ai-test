package vroong.laas.order.core.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WeightTest {

  @Test
  @DisplayName("무게를 생성한다")
  void createWeight() {
    // given & when
    Weight weight = new Weight(BigDecimal.valueOf(5.5));

    // then
    assertThat(weight.value()).isEqualByComparingTo(BigDecimal.valueOf(5.5));
  }

  @Test
  @DisplayName("무게가 null이면 예외가 발생한다")
  void createWeightWithNull() {
    // when & then
    assertThatThrownBy(() -> new Weight(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("무게는 null일 수 없습니다");
  }

  @Test
  @DisplayName("무게가 음수이면 예외가 발생한다")
  void createWeightWithNegative() {
    // when & then
    assertThatThrownBy(() -> new Weight(BigDecimal.valueOf(-1)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("무게는 음수일 수 없습니다");
  }
}

