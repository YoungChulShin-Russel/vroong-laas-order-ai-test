package vroong.laas.order.api.web.shared.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.order.core.domain.shared.Contact;

@DisplayName("ContactDto 테스트")
class ContactDtoTest {

  @Test
  @DisplayName("ContactDto → Contact Domain 변환")
  void toDomain() {
    // given
    ContactDto dto = new ContactDto("홍길동", "010-1234-5678");

    // when
    Contact domain = dto.toDomain();

    // then
    assertThat(domain).isNotNull();
    assertThat(domain.name()).isEqualTo("홍길동");
    assertThat(domain.phoneNumber()).isEqualTo("010-1234-5678");
  }

  @Test
  @DisplayName("특수문자가 포함된 이름도 정상 변환된다")
  void toDomain_withSpecialCharacters() {
    // given
    ContactDto dto = new ContactDto("Kim, John (김존)", "010-9999-8888");

    // when
    Contact domain = dto.toDomain();

    // then
    assertThat(domain.name()).isEqualTo("Kim, John (김존)");
    assertThat(domain.phoneNumber()).isEqualTo("010-9999-8888");
  }
}
