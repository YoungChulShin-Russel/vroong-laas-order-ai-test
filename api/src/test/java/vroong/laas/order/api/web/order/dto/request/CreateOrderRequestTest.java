package vroong.laas.order.api.web.order.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.order.api.web.common.dto.AddressDto;
import vroong.laas.order.api.web.common.dto.ContactDto;
import vroong.laas.order.api.web.common.dto.EntranceInfoDto;
import vroong.laas.order.api.web.common.dto.LatLngDto;
import vroong.laas.order.api.web.order.dto.DeliveryPolicyDto;
import vroong.laas.order.api.web.order.dto.DestinationDto;
import vroong.laas.order.api.web.order.dto.OrderItemDto;
import vroong.laas.order.api.web.order.dto.OriginDto;
import vroong.laas.order.core.application.order.command.CreateOrderCommand;

@DisplayName("CreateOrderRequest 테스트")
class CreateOrderRequestTest {

  @Test
  @DisplayName("모든 필드가 있는 CreateOrderRequest → CreateOrderCommand 변환")
  void toCommand_withAllFields() {
    // given
    List<OrderItemDto> items =
        List.of(
            new OrderItemDto(
                "상품A",
                2,
                new BigDecimal("10000"),
                "식품",
                new BigDecimal("1.5"),
                new BigDecimal("10"),
                new BigDecimal("20"),
                new BigDecimal("30")),
            new OrderItemDto(
                "상품B", 1, new BigDecimal("5000"), "생활용품", null, null, null, null));

    OriginDto origin =
        new OriginDto(
            new ContactDto("홍길동", "010-1234-5678"),
            new AddressDto("지번주소1", "도로명주소1", "상세주소1"),
            new LatLngDto(new BigDecimal("37.123"), new BigDecimal("127.123")),
            new EntranceInfoDto("1234", "안내1", "요청1"));

    DestinationDto destination =
        new DestinationDto(
            new ContactDto("김철수", "010-9999-8888"),
            new AddressDto("지번주소2", "도로명주소2", "상세주소2"),
            new LatLngDto(new BigDecimal("37.456"), new BigDecimal("127.456")),
            new EntranceInfoDto("5678", "안내2", "요청2"));

    DeliveryPolicyDto deliveryPolicy =
        new DeliveryPolicyDto(
            true,
            false,
            true,
            Instant.parse("2025-10-06T15:00:00Z"),
            Instant.parse("2025-10-06T14:00:00Z"));

    CreateOrderRequest request = new CreateOrderRequest(items, origin, destination, deliveryPolicy);

    // when
    CreateOrderCommand command = request.toCommand();

    // then
    assertThat(command).isNotNull();

    // items 검증
    assertThat(command.items()).hasSize(2);
    assertThat(command.items().get(0).itemName()).isEqualTo("상품A");
    assertThat(command.items().get(0).quantity()).isEqualTo(2);
    assertThat(command.items().get(0).price().amount())
        .isEqualByComparingTo(new BigDecimal("10000"));
    assertThat(command.items().get(1).itemName()).isEqualTo("상품B");

    // origin 검증
    assertThat(command.origin().contact().name()).isEqualTo("홍길동");
    assertThat(command.origin().address().roadAddress()).isEqualTo("도로명주소1");
    assertThat(command.origin().latLng().latitude())
        .isEqualByComparingTo(new BigDecimal("37.123"));

    // destination 검증
    assertThat(command.destination().contact().name()).isEqualTo("김철수");
    assertThat(command.destination().address().roadAddress()).isEqualTo("도로명주소2");
    assertThat(command.destination().latLng().longitude())
        .isEqualByComparingTo(new BigDecimal("127.456"));

    // deliveryPolicy 검증
    assertThat(command.deliveryPolicy().alcoholDelivery()).isTrue();
    assertThat(command.deliveryPolicy().contactlessDelivery()).isFalse();
    assertThat(command.deliveryPolicy().reservedDelivery()).isTrue();
  }

  @Test
  @DisplayName("items가 null인 경우 빈 리스트로 변환된다")
  void toCommand_withNullItems() {
    // given
    OriginDto origin =
        new OriginDto(
            new ContactDto("홍길동", "010-1234-5678"),
            new AddressDto(null, "도로명주소1", null),
            new LatLngDto(new BigDecimal("37.0"), new BigDecimal("127.0")),
            null);

    DestinationDto destination =
        new DestinationDto(
            new ContactDto("김철수", "010-9999-8888"),
            new AddressDto(null, "도로명주소2", null),
            new LatLngDto(new BigDecimal("37.5"), new BigDecimal("127.5")),
            null);

    DeliveryPolicyDto deliveryPolicy =
        new DeliveryPolicyDto(false, false, false, null, Instant.parse("2025-10-06T10:00:00Z"));

    CreateOrderRequest request = new CreateOrderRequest(null, origin, destination, deliveryPolicy);

    // when
    CreateOrderCommand command = request.toCommand();

    // then
    assertThat(command.items()).isEmpty();
    assertThat(command.origin().contact().name()).isEqualTo("홍길동");
    assertThat(command.destination().contact().name()).isEqualTo("김철수");
  }

  @Test
  @DisplayName("items가 빈 리스트인 경우도 정상 변환된다")
  void toCommand_withEmptyItems() {
    // given
    OriginDto origin =
        new OriginDto(
            new ContactDto("이영희", "010-1111-2222"),
            new AddressDto(null, "서울시 강남구", null),
            new LatLngDto(new BigDecimal("37.5"), new BigDecimal("127.0")),
            null);

    DestinationDto destination =
        new DestinationDto(
            new ContactDto("박민수", "010-3333-4444"),
            new AddressDto(null, "부산시 해운대구", null),
            new LatLngDto(new BigDecimal("35.0"), new BigDecimal("129.0")),
            null);

    DeliveryPolicyDto deliveryPolicy =
        new DeliveryPolicyDto(false, true, false, null, Instant.parse("2025-10-06T11:00:00Z"));

    CreateOrderRequest request =
        new CreateOrderRequest(List.of(), origin, destination, deliveryPolicy);

    // when
    CreateOrderCommand command = request.toCommand();

    // then
    assertThat(command.items()).isEmpty();
    assertThat(command.deliveryPolicy().contactlessDelivery()).isTrue();
  }

  @Test
  @DisplayName("단일 아이템도 정상 변환된다")
  void toCommand_withSingleItem() {
    // given
    List<OrderItemDto> items =
        List.of(new OrderItemDto("상품Z", 1, new BigDecimal("5000"), "전자제품", null, null, null, null));

    OriginDto origin =
        new OriginDto(
            new ContactDto("홍길동", "010-1234-5678"),
            new AddressDto(null, "도로명주소1", null),
            new LatLngDto(new BigDecimal("37.0"), new BigDecimal("127.0")),
            null);

    DestinationDto destination =
        new DestinationDto(
            new ContactDto("김철수", "010-9999-8888"),
            new AddressDto(null, "도로명주소2", null),
            new LatLngDto(new BigDecimal("37.5"), new BigDecimal("127.5")),
            null);

    DeliveryPolicyDto deliveryPolicy =
        new DeliveryPolicyDto(false, false, false, null, Instant.parse("2025-10-06T10:00:00Z"));

    CreateOrderRequest request = new CreateOrderRequest(items, origin, destination, deliveryPolicy);

    // when
    CreateOrderCommand command = request.toCommand();

    // then
    assertThat(command.items()).hasSize(1);
    assertThat(command.items().get(0).itemName()).isEqualTo("상품Z");
    assertThat(command.origin().contact().name()).isEqualTo("홍길동");
    assertThat(command.destination().contact().name()).isEqualTo("김철수");
  }

  @Test
  @DisplayName("여러 아이템이 모두 정상 변환된다")
  void toCommand_withMultipleItems() {
    // given
    List<OrderItemDto> items =
        List.of(
            new OrderItemDto("상품1", 1, new BigDecimal("1000"), "식품", null, null, null, null),
            new OrderItemDto("상품2", 2, new BigDecimal("2000"), "생활용품", null, null, null, null),
            new OrderItemDto("상품3", 3, new BigDecimal("3000"), "의류", null, null, null, null));

    OriginDto origin =
        new OriginDto(
            new ContactDto("이영희", "010-1111-2222"),
            new AddressDto(null, "서울시 강남구", null),
            new LatLngDto(new BigDecimal("37.5"), new BigDecimal("127.0")),
            null);

    DestinationDto destination =
        new DestinationDto(
            new ContactDto("박민수", "010-3333-4444"),
            new AddressDto(null, "부산시 해운대구", null),
            new LatLngDto(new BigDecimal("35.0"), new BigDecimal("129.0")),
            null);

    DeliveryPolicyDto deliveryPolicy =
        new DeliveryPolicyDto(false, true, false, null, Instant.parse("2025-10-06T11:00:00Z"));

    CreateOrderRequest request =
        new CreateOrderRequest(items, origin, destination, deliveryPolicy);

    // when
    CreateOrderCommand command = request.toCommand();

    // then
    assertThat(command.items()).hasSize(3);
    assertThat(command.items().get(0).itemName()).isEqualTo("상품1");
    assertThat(command.items().get(1).itemName()).isEqualTo("상품2");
    assertThat(command.items().get(2).itemName()).isEqualTo("상품3");
    assertThat(command.deliveryPolicy().contactlessDelivery()).isTrue();
  }

  @Test
  @DisplayName("Optional 필드들이 최소한으로만 있어도 변환된다")
  void toCommand_withMinimalFields() {
    // given
    List<OrderItemDto> items =
        List.of(new OrderItemDto("상품X", 1, new BigDecimal("1000"), "카테고리", null, null, null, null));

    OriginDto origin =
        new OriginDto(
            new ContactDto("A", "010-0000-0000"),
            new AddressDto(null, "주소A", null),
            new LatLngDto(new BigDecimal("37"), new BigDecimal("127")),
            null);

    DestinationDto destination =
        new DestinationDto(
            new ContactDto("B", "010-1111-1111"),
            new AddressDto(null, "주소B", null),
            new LatLngDto(new BigDecimal("36"), new BigDecimal("126")),
            null);

    DeliveryPolicyDto deliveryPolicy =
        new DeliveryPolicyDto(false, false, false, null, Instant.parse("2025-10-06T12:00:00Z"));

    CreateOrderRequest request = new CreateOrderRequest(items, origin, destination, deliveryPolicy);

    // when
    CreateOrderCommand command = request.toCommand();

    // then
    assertThat(command).isNotNull();
    assertThat(command.items()).hasSize(1);
    assertThat(command.origin()).isNotNull();
    assertThat(command.destination()).isNotNull();
    assertThat(command.deliveryPolicy()).isNotNull();
  }
}
