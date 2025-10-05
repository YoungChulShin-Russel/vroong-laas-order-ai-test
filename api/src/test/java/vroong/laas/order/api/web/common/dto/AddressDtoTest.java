package vroong.laas.order.api.web.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.order.core.domain.shared.Address;

@DisplayName("AddressDto 테스트")
class AddressDtoTest {

  @Test
  @DisplayName("모든 필드가 있는 AddressDto → Address Domain 변환")
  void toDomain_withAllFields() {
    // given
    AddressDto dto =
        new AddressDto("서울시 강남구 역삼동 123-45", "서울시 강남구 테헤란로 123", "3층 301호");

    // when
    Address domain = dto.toDomain();

    // then
    assertThat(domain).isNotNull();
    assertThat(domain.jibnunAddress()).isEqualTo("서울시 강남구 역삼동 123-45");
    assertThat(domain.roadAddress()).isEqualTo("서울시 강남구 테헤란로 123");
    assertThat(domain.detailAddress()).isEqualTo("3층 301호");
  }

  @Test
  @DisplayName("지번 주소가 없어도 변환된다")
  void toDomain_withoutJibnunAddress() {
    // given
    AddressDto dto = new AddressDto(null, "서울시 강남구 테헤란로 123", "3층 301호");

    // when
    Address domain = dto.toDomain();

    // then
    assertThat(domain.jibnunAddress()).isNull();
    assertThat(domain.roadAddress()).isEqualTo("서울시 강남구 테헤란로 123");
    assertThat(domain.detailAddress()).isEqualTo("3층 301호");
  }

  @Test
  @DisplayName("상세 주소가 없어도 변환된다")
  void toDomain_withoutDetailAddress() {
    // given
    AddressDto dto = new AddressDto("서울시 강남구 역삼동 123-45", "서울시 강남구 테헤란로 123", null);

    // when
    Address domain = dto.toDomain();

    // then
    assertThat(domain.jibnunAddress()).isEqualTo("서울시 강남구 역삼동 123-45");
    assertThat(domain.roadAddress()).isEqualTo("서울시 강남구 테헤란로 123");
    assertThat(domain.detailAddress()).isNull();
  }
}
