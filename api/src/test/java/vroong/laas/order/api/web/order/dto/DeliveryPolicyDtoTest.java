package vroong.laas.order.api.web.order.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.order.core.domain.order.DeliveryPolicy;

@DisplayName("DeliveryPolicyDto 테스트")
class DeliveryPolicyDtoTest {

  @Test
  @DisplayName("모든 필드가 있는 DeliveryPolicyDto → DeliveryPolicy Domain 변환")
  void toDomain_withAllFields() {
    // given
    Instant reservedTime = Instant.parse("2025-10-06T15:00:00Z");
    Instant pickupTime = Instant.parse("2025-10-06T14:00:00Z");

    DeliveryPolicyDto dto = new DeliveryPolicyDto(true, false, true, reservedTime, pickupTime);

    // when
    DeliveryPolicy domain = dto.toDomain();

    // then
    assertThat(domain).isNotNull();
    assertThat(domain.alcoholDelivery()).isTrue();
    assertThat(domain.contactlessDelivery()).isFalse();
    assertThat(domain.reservedDelivery()).isTrue();
    assertThat(domain.reservedDeliveryStartTime()).isEqualTo(reservedTime);
    assertThat(domain.pickupRequestTime()).isEqualTo(pickupTime);
  }

  @Test
  @DisplayName("예약 배송이 아닌 경우 예약 시작 시간이 null이어도 변환된다")
  void toDomain_withoutReservedTime() {
    // given
    Instant pickupTime = Instant.parse("2025-10-06T14:00:00Z");
    DeliveryPolicyDto dto = new DeliveryPolicyDto(false, true, false, null, pickupTime);

    // when
    DeliveryPolicy domain = dto.toDomain();

    // then
    assertThat(domain.alcoholDelivery()).isFalse();
    assertThat(domain.contactlessDelivery()).isTrue();
    assertThat(domain.reservedDelivery()).isFalse();
    assertThat(domain.reservedDeliveryStartTime()).isNull();
    assertThat(domain.pickupRequestTime()).isEqualTo(pickupTime);
  }

  @Test
  @DisplayName("모든 옵션이 false인 일반 배송도 변환된다")
  void toDomain_allOptionsDisabled() {
    // given
    Instant pickupTime = Instant.parse("2025-10-06T13:00:00Z");
    DeliveryPolicyDto dto = new DeliveryPolicyDto(false, false, false, null, pickupTime);

    // when
    DeliveryPolicy domain = dto.toDomain();

    // then
    assertThat(domain.alcoholDelivery()).isFalse();
    assertThat(domain.contactlessDelivery()).isFalse();
    assertThat(domain.reservedDelivery()).isFalse();
    assertThat(domain.pickupRequestTime()).isEqualTo(pickupTime);
  }

  @Test
  @DisplayName("모든 옵션이 true인 경우도 변환된다")
  void toDomain_allOptionsEnabled() {
    // given
    Instant reservedTime = Instant.parse("2025-10-07T10:00:00Z");
    Instant pickupTime = Instant.parse("2025-10-07T09:00:00Z");

    DeliveryPolicyDto dto = new DeliveryPolicyDto(true, true, true, reservedTime, pickupTime);

    // when
    DeliveryPolicy domain = dto.toDomain();

    // then
    assertThat(domain.alcoholDelivery()).isTrue();
    assertThat(domain.contactlessDelivery()).isTrue();
    assertThat(domain.reservedDelivery()).isTrue();
    assertThat(domain.reservedDeliveryStartTime()).isEqualTo(reservedTime);
    assertThat(domain.pickupRequestTime()).isEqualTo(pickupTime);
  }
}
