package vroong.laas.order.infrastructure.storage.db.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.EntranceInfo;
import vroong.laas.order.core.domain.order.Origin;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.Contact;
import vroong.laas.order.core.domain.shared.LatLng;

@DisplayName("OrderLocationEntity 변환 테스트")
class OrderLocationEntityTest {

  @Test
  @DisplayName("Domain → Entity 변환 (Origin + Destination)")
  void from_domain_to_entity() {
    // given
    Origin origin =
        new Origin(
            new Contact("홍길동", "010-1234-5678"),
            new Address("역삼동 123-45", "서울시 강남구 테헤란로", "1층"),
            new LatLng(new BigDecimal("37.5665"), new BigDecimal("126.9780")),
            new EntranceInfo("1234", "정문 이용", "빠른 배송 부탁드립니다"));

    Destination destination =
        new Destination(
            new Contact("김철수", "010-9876-5432"),
            new Address("서초동 567-89", "서울시 서초구 강남대로", "3층 301호"),
            new LatLng(new BigDecimal("37.4833"), new BigDecimal("127.0324")),
            new EntranceInfo("5678", "후문 이용", "문 앞에 놔주세요"));

    // when
    OrderLocationEntity entity = OrderLocationEntity.from(origin, destination, 1L);

    // then
    assertThat(entity.getOrderId()).isEqualTo(1L);

    // Origin 검증
    assertThat(entity.getOriginContactName()).isEqualTo("홍길동");
    assertThat(entity.getOriginContactPhoneNumber()).isEqualTo("010-1234-5678");
    assertThat(entity.getOriginJibnunAddress()).isEqualTo("역삼동 123-45");
    assertThat(entity.getOriginRoadAddress()).isEqualTo("서울시 강남구 테헤란로");
    assertThat(entity.getOriginDetailAddress()).isEqualTo("1층");
    assertThat(entity.getOriginLatitude()).isEqualByComparingTo("37.5665");
    assertThat(entity.getOriginLongitude()).isEqualByComparingTo("126.9780");
    assertThat(entity.getOriginEntrancePassword()).isEqualTo("1234");
    assertThat(entity.getOriginEntranceGuide()).isEqualTo("정문 이용");
    assertThat(entity.getOriginRequestMessage()).isEqualTo("빠른 배송 부탁드립니다");

    // Destination 검증
    assertThat(entity.getDestinationContactName()).isEqualTo("김철수");
    assertThat(entity.getDestinationContactPhoneNumber()).isEqualTo("010-9876-5432");
    assertThat(entity.getDestinationJibnunAddress()).isEqualTo("서초동 567-89");
    assertThat(entity.getDestinationRoadAddress()).isEqualTo("서울시 서초구 강남대로");
    assertThat(entity.getDestinationDetailAddress()).isEqualTo("3층 301호");
  }

  @Test
  @DisplayName("Entity → Domain 변환 (Origin)")
  void from_entity_to_origin_domain() {
    // given
    OrderLocationEntity entity =
        OrderLocationEntity.builder()
            .orderId(1L)
            .originContactName("홍길동")
            .originContactPhoneNumber("010-1234-5678")
            .originJibnunAddress("역삼동 123-45")
            .originRoadAddress("서울시 강남구 테헤란로")
            .originDetailAddress("1층")
            .originLatitude(new BigDecimal("37.5665"))
            .originLongitude(new BigDecimal("126.9780"))
            .originEntrancePassword("1234")
            .originEntranceGuide("정문 이용")
            .originRequestMessage("빠른 배송 부탁드립니다")
            .destinationContactName("김철수")
            .destinationContactPhoneNumber("010-9876-5432")
            .destinationJibnunAddress("서초동 567-89")
            .destinationRoadAddress("서울시 서초구 강남대로")
            .destinationDetailAddress("3층 301호")
            .destinationLatitude(new BigDecimal("37.4833"))
            .destinationLongitude(new BigDecimal("127.0324"))
            .destinationEntrancePassword("5678")
            .destinationEntranceGuide("후문 이용")
            .destinationRequestMessage("문 앞에 놔주세요")
            .build();

    // when
    Origin origin = entity.toOriginDomain();

    // then
    assertThat(origin.contact().name()).isEqualTo("홍길동");
    assertThat(origin.contact().phoneNumber()).isEqualTo("010-1234-5678");
    assertThat(origin.address().jibnunAddress()).isEqualTo("역삼동 123-45");
    assertThat(origin.address().roadAddress()).isEqualTo("서울시 강남구 테헤란로");
    assertThat(origin.address().detailAddress()).isEqualTo("1층");
    assertThat(origin.latLng().latitude()).isEqualByComparingTo("37.5665");
    assertThat(origin.latLng().longitude()).isEqualByComparingTo("126.9780");
    assertThat(origin.entranceInfo().password()).isEqualTo("1234");
    assertThat(origin.entranceInfo().guide()).isEqualTo("정문 이용");
    assertThat(origin.entranceInfo().requestMessage()).isEqualTo("빠른 배송 부탁드립니다");
  }

  @Test
  @DisplayName("Entity → Domain 변환 (Destination)")
  void from_entity_to_destination_domain() {
    // given
    OrderLocationEntity entity =
        OrderLocationEntity.builder()
            .orderId(1L)
            .originContactName("홍길동")
            .originContactPhoneNumber("010-1234-5678")
            .originJibnunAddress("역삼동 123-45")
            .originRoadAddress("서울시 강남구 테헤란로")
            .originDetailAddress("1층")
            .originLatitude(new BigDecimal("37.5665"))
            .originLongitude(new BigDecimal("126.9780"))
            .originEntrancePassword("1234")
            .originEntranceGuide("정문 이용")
            .originRequestMessage("빠른 배송 부탁드립니다")
            .destinationContactName("김철수")
            .destinationContactPhoneNumber("010-9876-5432")
            .destinationJibnunAddress("서초동 567-89")
            .destinationRoadAddress("서울시 서초구 강남대로")
            .destinationDetailAddress("3층 301호")
            .destinationLatitude(new BigDecimal("37.4833"))
            .destinationLongitude(new BigDecimal("127.0324"))
            .destinationEntrancePassword("5678")
            .destinationEntranceGuide("후문 이용")
            .destinationRequestMessage("문 앞에 놔주세요")
            .build();

    // when
    Destination destination = entity.toDestinationDomain();

    // then
    assertThat(destination.contact().name()).isEqualTo("김철수");
    assertThat(destination.contact().phoneNumber()).isEqualTo("010-9876-5432");
    assertThat(destination.address().jibnunAddress()).isEqualTo("서초동 567-89");
    assertThat(destination.address().roadAddress()).isEqualTo("서울시 서초구 강남대로");
    assertThat(destination.address().detailAddress()).isEqualTo("3층 301호");
    assertThat(destination.latLng().latitude()).isEqualByComparingTo("37.4833");
    assertThat(destination.latLng().longitude()).isEqualByComparingTo("127.0324");
    assertThat(destination.entranceInfo().password()).isEqualTo("5678");
  }

  @Test
  @DisplayName("Domain → Entity → Domain 왕복 변환")
  void round_trip_conversion() {
    // given
    Origin originalOrigin =
        new Origin(
            new Contact("왕복테스트", "010-0000-0000"),
            new Address("테스트동 1-1", "테스트시 테스트구", "테스트빌딩"),
            new LatLng(new BigDecimal("37.1234"), new BigDecimal("127.5678")),
            new EntranceInfo("0000", "테스트 가이드", "테스트 메시지"));

    Destination originalDestination =
        new Destination(
            new Contact("도착지테스트", "010-1111-1111"),
            new Address("도착동 2-2", "도착시 도착구", "도착빌딩"),
            new LatLng(new BigDecimal("37.9876"), new BigDecimal("127.1234")),
            new EntranceInfo("1111", "도착 가이드", "도착 메시지"));

    // when
    OrderLocationEntity entity =
        OrderLocationEntity.from(originalOrigin, originalDestination, 999L);
    Origin convertedOrigin = entity.toOriginDomain();
    Destination convertedDestination = entity.toDestinationDomain();

    // then
    assertThat(convertedOrigin.contact().name())
        .isEqualTo(originalOrigin.contact().name());
    assertThat(convertedOrigin.address().roadAddress())
        .isEqualTo(originalOrigin.address().roadAddress());
    assertThat(convertedDestination.contact().name())
        .isEqualTo(originalDestination.contact().name());
    assertThat(convertedDestination.address().roadAddress())
        .isEqualTo(originalDestination.address().roadAddress());
  }
}

