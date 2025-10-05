package vroong.laas.order.api.web.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.order.core.domain.shared.LatLng;

@DisplayName("LatLngDto 테스트")
class LatLngDtoTest {

  @Test
  @DisplayName("LatLngDto → LatLng Domain 변환")
  void toDomain() {
    // given
    LatLngDto dto = new LatLngDto(new BigDecimal("37.123456"), new BigDecimal("127.123456"));

    // when
    LatLng domain = dto.toDomain();

    // then
    assertThat(domain).isNotNull();
    assertThat(domain.latitude()).isEqualByComparingTo(new BigDecimal("37.123456"));
    assertThat(domain.longitude()).isEqualByComparingTo(new BigDecimal("127.123456"));
  }

  @Test
  @DisplayName("소수점 자리수가 많은 좌표도 정확하게 변환된다")
  void toDomain_withHighPrecision() {
    // given
    LatLngDto dto =
        new LatLngDto(
            new BigDecimal("37.123456789012345"), new BigDecimal("127.987654321098765"));

    // when
    LatLng domain = dto.toDomain();

    // then
    assertThat(domain.latitude()).isEqualByComparingTo(new BigDecimal("37.123456789012345"));
    assertThat(domain.longitude()).isEqualByComparingTo(new BigDecimal("127.987654321098765"));
  }

  @Test
  @DisplayName("경계값 위도 90도도 변환된다")
  void toDomain_withBoundaryLatitude() {
    // given
    LatLngDto dto = new LatLngDto(new BigDecimal("90.0"), new BigDecimal("127.0"));

    // when
    LatLng domain = dto.toDomain();

    // then
    assertThat(domain.latitude()).isEqualByComparingTo(new BigDecimal("90.0"));
  }

  @Test
  @DisplayName("경계값 경도 180도도 변환된다")
  void toDomain_withBoundaryLongitude() {
    // given
    LatLngDto dto = new LatLngDto(new BigDecimal("37.0"), new BigDecimal("180.0"));

    // when
    LatLng domain = dto.toDomain();

    // then
    assertThat(domain.longitude()).isEqualByComparingTo(new BigDecimal("180.0"));
  }
}
