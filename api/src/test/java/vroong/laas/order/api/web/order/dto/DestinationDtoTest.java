package vroong.laas.order.api.web.order.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.order.api.web.common.dto.AddressDto;
import vroong.laas.order.api.web.common.dto.ContactDto;
import vroong.laas.order.api.web.common.dto.EntranceInfoDto;
import vroong.laas.order.api.web.common.dto.LatLngDto;
import vroong.laas.order.core.domain.order.Destination;

@DisplayName("DestinationDto 테스트")
class DestinationDtoTest {

  @Test
  @DisplayName("모든 필드가 있는 DestinationDto → Destination Domain 변환")
  void toDomain_withAllFields() {
    // given
    ContactDto contact = new ContactDto("이영희", "010-5555-6666");
    AddressDto address = new AddressDto("부산시 해운대구 우동 123-45", "부산시 해운대구 해운대로 789", "101동 1001호");
    LatLngDto latLng = new LatLngDto(new BigDecimal("35.123"), new BigDecimal("129.123"));
    EntranceInfoDto entranceInfo = new EntranceInfoDto("9999", "엘리베이터 이용", "문 앞에 놔주세요");

    DestinationDto dto = new DestinationDto(contact, address, latLng, entranceInfo);

    // when
    Destination domain = dto.toDomain();

    // then
    assertThat(domain).isNotNull();
    assertThat(domain.contact().name()).isEqualTo("이영희");
    assertThat(domain.contact().phoneNumber()).isEqualTo("010-5555-6666");
    assertThat(domain.address().jibnunAddress()).isEqualTo("부산시 해운대구 우동 123-45");
    assertThat(domain.address().roadAddress()).isEqualTo("부산시 해운대구 해운대로 789");
    assertThat(domain.address().detailAddress()).isEqualTo("101동 1001호");
    assertThat(domain.latLng().latitude()).isEqualByComparingTo(new BigDecimal("35.123"));
    assertThat(domain.latLng().longitude()).isEqualByComparingTo(new BigDecimal("129.123"));
    assertThat(domain.entranceInfo().password()).isEqualTo("9999");
    assertThat(domain.entranceInfo().guide()).isEqualTo("엘리베이터 이용");
    assertThat(domain.entranceInfo().requestMessage()).isEqualTo("문 앞에 놔주세요");
  }

  @Test
  @DisplayName("출입 정보가 없어도 변환된다 (empty로 변환)")
  void toDomain_withoutEntranceInfo() {
    // given
    ContactDto contact = new ContactDto("박민수", "010-7777-3333");
    AddressDto address = new AddressDto(null, "대전시 유성구 대학로 99", "B동 301호");
    LatLngDto latLng = new LatLngDto(new BigDecimal("36.5"), new BigDecimal("127.5"));

    DestinationDto dto = new DestinationDto(contact, address, latLng, null);

    // when
    Destination domain = dto.toDomain();

    // then
    assertThat(domain.contact().name()).isEqualTo("박민수");
    assertThat(domain.entranceInfo()).isNotNull();
    assertThat(domain.entranceInfo().password()).isNull();
    assertThat(domain.entranceInfo().guide()).isNull();
    assertThat(domain.entranceInfo().requestMessage()).isNull();
  }
}
