package vroong.laas.order.core.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VolumeTest {

  @Test
  @DisplayName("길이, 너비, 높이로 부피를 생성하면 CBM이 자동 계산된다")
  void createVolumeWithDimensions() {
    // given
    BigDecimal length = BigDecimal.valueOf(100); // 100cm
    BigDecimal width = BigDecimal.valueOf(50); // 50cm
    BigDecimal height = BigDecimal.valueOf(30); // 30cm

    // when
    Volume volume = new Volume(length, width, height);

    // then
    assertThat(volume.length()).isEqualByComparingTo(length);
    assertThat(volume.width()).isEqualByComparingTo(width);
    assertThat(volume.height()).isEqualByComparingTo(height);

    // 100 * 50 * 30 = 150,000 cm³ = 0.15 m³
    assertThat(volume.cbm())
        .isCloseTo(BigDecimal.valueOf(0.15), within(BigDecimal.valueOf(0.0001)));
  }

  @Test
  @DisplayName("길이가 null이면 예외가 발생한다")
  void createVolumeWithNullLength() {
    // when & then
    assertThatThrownBy(
            () -> new Volume(null, BigDecimal.valueOf(50), BigDecimal.valueOf(30)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("길이는 null일 수 없습니다");
  }

  @Test
  @DisplayName("길이가 0 이하이면 예외가 발생한다")
  void createVolumeWithZeroLength() {
    // when & then
    assertThatThrownBy(
            () -> new Volume(BigDecimal.ZERO, BigDecimal.valueOf(50), BigDecimal.valueOf(30)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("길이는 0보다 커야 합니다");
  }

  @Test
  @DisplayName("너비가 0 이하이면 예외가 발생한다")
  void createVolumeWithZeroWidth() {
    // when & then
    assertThatThrownBy(
            () -> new Volume(BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.valueOf(30)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("너비는 0보다 커야 합니다");
  }

  @Test
  @DisplayName("높이가 0 이하이면 예외가 발생한다")
  void createVolumeWithZeroHeight() {
    // when & then
    assertThatThrownBy(
            () -> new Volume(BigDecimal.valueOf(100), BigDecimal.valueOf(50), BigDecimal.ZERO))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("높이는 0보다 커야 합니다");
  }

  @Test
  @DisplayName("CBM 계산이 정확하다")
  void calculateCBMAccuracy() {
    // given
    Volume volume1 = new Volume(BigDecimal.valueOf(100), BigDecimal.valueOf(100), BigDecimal.valueOf(100));
    Volume volume2 = new Volume(BigDecimal.valueOf(200), BigDecimal.valueOf(50), BigDecimal.valueOf(40));

    // then
    // 100 * 100 * 100 = 1,000,000 cm³ = 1.0 m³
    assertThat(volume1.cbm())
        .isCloseTo(BigDecimal.valueOf(1.0), within(BigDecimal.valueOf(0.0001)));

    // 200 * 50 * 40 = 400,000 cm³ = 0.4 m³
    assertThat(volume2.cbm())
        .isCloseTo(BigDecimal.valueOf(0.4), within(BigDecimal.valueOf(0.0001)));
  }
}

