package vroong.laas.order.core.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LatLngTest {

  @Test
  @DisplayName("위도와 경도를 생성한다")
  void createLatLng() {
    // given & when
    LatLng latLng = new LatLng(BigDecimal.valueOf(37.123456), BigDecimal.valueOf(127.123456));

    // then
    assertThat(latLng.latitude()).isEqualByComparingTo(BigDecimal.valueOf(37.123456));
    assertThat(latLng.longitude()).isEqualByComparingTo(BigDecimal.valueOf(127.123456));
  }

  @Test
  @DisplayName("위도가 null이면 예외가 발생한다")
  void createLatLngWithNullLatitude() {
    // when & then
    assertThatThrownBy(() -> new LatLng(null, BigDecimal.valueOf(127.123456)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("위도는 필수입니다");
  }

  @Test
  @DisplayName("위도가 -90보다 작으면 예외가 발생한다")
  void createLatLngWithInvalidMinLatitude() {
    // when & then
    assertThatThrownBy(() -> new LatLng(BigDecimal.valueOf(-91), BigDecimal.valueOf(127.123456)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("위도는 -90 ~ 90 사이여야 합니다");
  }

  @Test
  @DisplayName("위도가 90보다 크면 예외가 발생한다")
  void createLatLngWithInvalidMaxLatitude() {
    // when & then
    assertThatThrownBy(() -> new LatLng(BigDecimal.valueOf(91), BigDecimal.valueOf(127.123456)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("위도는 -90 ~ 90 사이여야 합니다");
  }

  @Test
  @DisplayName("경도가 null이면 예외가 발생한다")
  void createLatLngWithNullLongitude() {
    // when & then
    assertThatThrownBy(() -> new LatLng(BigDecimal.valueOf(37.123456), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("경도는 필수입니다");
  }

  @Test
  @DisplayName("경도가 -180보다 작으면 예외가 발생한다")
  void createLatLngWithInvalidMinLongitude() {
    // when & then
    assertThatThrownBy(() -> new LatLng(BigDecimal.valueOf(37.123456), BigDecimal.valueOf(-181)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("경도는 -180 ~ 180 사이여야 합니다");
  }

  @Test
  @DisplayName("경도가 180보다 크면 예외가 발생한다")
  void createLatLngWithInvalidMaxLongitude() {
    // when & then
    assertThatThrownBy(() -> new LatLng(BigDecimal.valueOf(37.123456), BigDecimal.valueOf(181)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("경도는 -180 ~ 180 사이여야 합니다");
  }
}

