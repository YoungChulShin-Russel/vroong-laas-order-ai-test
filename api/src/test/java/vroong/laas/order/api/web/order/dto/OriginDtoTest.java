package vroong.laas.order.api.web.order.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.order.api.web.common.dto.AddressDto;
import vroong.laas.order.api.web.common.dto.ContactDto;
import vroong.laas.order.api.web.common.dto.EntranceInfoDto;
import vroong.laas.order.api.web.common.dto.LatLngDto;
import vroong.laas.order.core.domain.order.Origin;

@DisplayName("OriginDto 테스트")
class OriginDtoTest {

  @Test
  @DisplayName("모든 필드가 있는 OriginDto → Origin Domain 변환")
  void toDomain_withAllFields() {
    // given
    ContactDto contact = new ContactDto("홍길동", "010-1234-5678");
    AddressDto address = new AddressDto("지번주소", "도로명주소", "상세주소");
    LatLngDto latLng = new LatLngDto(new BigDecimal("37.123"), new BigDecimal("127.123"));
    EntranceInfoDto entranceInfo = new EntranceInfoDto("1234", "안내사항", "요청사항");

    OriginDto dto = new OriginDto(contact, address, latLng, entranceInfo);

    // when
    Origin domain = dto.toDomain();

    // then
    assertThat(domain).isNotNull();
    assertThat(domain.contact().name()).isEqualTo("홍길동");
    assertThat(domain.contact().phoneNumber()).isEqualTo("010-1234-5678");
    assertThat(domain.address().jibnunAddress()).isEqualTo("지번주소");
    assertThat(domain.address().roadAddress()).isEqualTo("도로명주소");
    assertThat(domain.address().detailAddress()).isEqualTo("상세주소");
    assertThat(domain.latLng().latitude()).isEqualByComparingTo(new BigDecimal("37.123"));
    assertThat(domain.latLng().longitude()).isEqualByComparingTo(new BigDecimal("127.123"));
    assertThat(domain.entranceInfo().password()).isEqualTo("1234");
    assertThat(domain.entranceInfo().guide()).isEqualTo("안내사항");
    assertThat(domain.entranceInfo().requestMessage()).isEqualTo("요청사항");
  }

  @Test
  @DisplayName("출입 정보가 없어도 변환된다 (empty로 변환)")
  void toDomain_withoutEntranceInfo() {
    // given
    ContactDto contact = new ContactDto("김철수", "010-9999-8888");
    AddressDto address = new AddressDto(null, "서울시 강남구 테헤란로 123", null);
    LatLngDto latLng = new LatLngDto(new BigDecimal("37.5"), new BigDecimal("127.0"));

    OriginDto dto = new OriginDto(contact, address, latLng, null);

    // when
    Origin domain = dto.toDomain();

    // then
    assertThat(domain.contact().name()).isEqualTo("김철수");
    assertThat(domain.entranceInfo()).isNotNull();
    assertThat(domain.entranceInfo().password()).isNull();
    assertThat(domain.entranceInfo().guide()).isNull();
    assertThat(domain.entranceInfo().requestMessage()).isNull();
  }
}
