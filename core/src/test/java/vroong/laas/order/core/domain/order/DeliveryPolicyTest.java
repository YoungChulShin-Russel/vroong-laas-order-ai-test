package vroong.laas.order.core.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeliveryPolicyTest {

  @Test
  @DisplayName("즉시 배송 정책을 생성한다")
  void createImmediateDeliveryPolicy() {
    // given
    Instant pickupTime = Instant.now();

    // when
    DeliveryPolicy policy = new DeliveryPolicy(false, true, false, null, pickupTime);

    // then
    assertThat(policy.alcoholDelivery()).isFalse();
    assertThat(policy.contactlessDelivery()).isTrue();
    assertThat(policy.reservedDelivery()).isFalse();
    assertThat(policy.reservedDeliveryStartTime()).isNull();
    assertThat(policy.pickupRequestTime()).isEqualTo(pickupTime);
  }

  @Test
  @DisplayName("예약 배송 정책을 생성한다")
  void createReservedDeliveryPolicy() {
    // given
    Instant pickupTime = Instant.now();
    Instant reservedTime = Instant.now().plusSeconds(3600);

    // when
    DeliveryPolicy policy = new DeliveryPolicy(true, false, true, reservedTime, pickupTime);

    // then
    assertThat(policy.alcoholDelivery()).isTrue();
    assertThat(policy.contactlessDelivery()).isFalse();
    assertThat(policy.reservedDelivery()).isTrue();
    assertThat(policy.reservedDeliveryStartTime()).isEqualTo(reservedTime);
  }

  @Test
  @DisplayName("예약 배송인데 예약 시작 시간이 없으면 예외가 발생한다")
  void createReservedDeliveryPolicyWithoutStartTime() {
    // given
    Instant pickupTime = Instant.now();

    // when & then
    assertThatThrownBy(() -> new DeliveryPolicy(false, true, true, null, pickupTime))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("예약 배송인 경우 예약 시작 시간은 필수입니다");
  }

  @Test
  @DisplayName("픽업 요청 시간이 없으면 예외가 발생한다")
  void createDeliveryPolicyWithoutPickupTime() {
    // when & then
    assertThatThrownBy(() -> new DeliveryPolicy(false, true, false, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("픽업 요청 시간은 필수입니다");
  }
}

