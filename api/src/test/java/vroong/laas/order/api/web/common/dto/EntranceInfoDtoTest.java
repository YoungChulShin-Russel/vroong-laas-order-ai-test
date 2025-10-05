package vroong.laas.order.api.web.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.order.core.domain.order.EntranceInfo;

@DisplayName("EntranceInfoDto 테스트")
class EntranceInfoDtoTest {

  @Test
  @DisplayName("모든 필드가 있는 EntranceInfoDto → EntranceInfo Domain 변환")
  void toDomain_withAllFields() {
    // given
    EntranceInfoDto dto = new EntranceInfoDto("1234", "왼쪽 문으로 들어오세요", "문 앞에 두고 벨 눌러주세요");

    // when
    EntranceInfo domain = dto.toDomain();

    // then
    assertThat(domain).isNotNull();
    assertThat(domain.password()).isEqualTo("1234");
    assertThat(domain.guide()).isEqualTo("왼쪽 문으로 들어오세요");
    assertThat(domain.requestMessage()).isEqualTo("문 앞에 두고 벨 눌러주세요");
  }

  @Test
  @DisplayName("비밀번호만 있어도 변환된다")
  void toDomain_withPasswordOnly() {
    // given
    EntranceInfoDto dto = new EntranceInfoDto("1234", null, null);

    // when
    EntranceInfo domain = dto.toDomain();

    // then
    assertThat(domain.password()).isEqualTo("1234");
    assertThat(domain.guide()).isNull();
    assertThat(domain.requestMessage()).isNull();
  }

  @Test
  @DisplayName("요청사항만 있어도 변환된다")
  void toDomain_withRequestMessageOnly() {
    // given
    EntranceInfoDto dto = new EntranceInfoDto(null, null, "문 앞에 두고 벨 눌러주세요");

    // when
    EntranceInfo domain = dto.toDomain();

    // then
    assertThat(domain.password()).isNull();
    assertThat(domain.guide()).isNull();
    assertThat(domain.requestMessage()).isEqualTo("문 앞에 두고 벨 눌러주세요");
  }

  @Test
  @DisplayName("모든 필드가 null이어도 변환된다")
  void toDomain_withAllFieldsNull() {
    // given
    EntranceInfoDto dto = new EntranceInfoDto(null, null, null);

    // when
    EntranceInfo domain = dto.toDomain();

    // then
    assertThat(domain.password()).isNull();
    assertThat(domain.guide()).isNull();
    assertThat(domain.requestMessage()).isNull();
  }
}
