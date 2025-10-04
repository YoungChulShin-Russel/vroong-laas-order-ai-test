package vroong.laas.order.infrastructure.storage.db.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.order.core.domain.order.DeliveryPolicy;

@DisplayName("OrderDeliveryPolicyEntity 변환 테스트")
class OrderDeliveryPolicyEntityTest {

  @Test
  @DisplayName("Domain → Entity 변환 (JSON 직렬화)")
  void from_domain_to_entity() {
    // given
    Instant pickupTime = Instant.parse("2025-01-15T10:00:00Z");
    Instant reservedTime = Instant.parse("2025-01-15T15:00:00Z");

    DeliveryPolicy domain =
        new DeliveryPolicy(true, false, true, reservedTime, pickupTime);

    // when
    OrderDeliveryPolicyEntity entity = OrderDeliveryPolicyEntity.from(domain, 1L);

    // then
    assertThat(entity.getOrderId()).isEqualTo(1L);
    assertThat(entity.getDeliveryPolicyJson()).isNotNull();
    // JsonUtil은 snake_case를 사용
    assertThat(entity.getDeliveryPolicyJson()).contains("alcohol_delivery");
    assertThat(entity.getDeliveryPolicyJson()).contains("pickup_request_time");
  }

  @Test
  @DisplayName("Entity → Domain 변환 (JSON 역직렬화)")
  void from_entity_to_domain() {
    // given
    Instant pickupTime = Instant.parse("2025-01-15T10:00:00Z");
    Instant reservedTime = Instant.parse("2025-01-15T15:00:00Z");

    DeliveryPolicy original =
        new DeliveryPolicy(true, false, true, reservedTime, pickupTime);

    OrderDeliveryPolicyEntity entity = OrderDeliveryPolicyEntity.from(original, 1L);

    // when
    DeliveryPolicy domain = entity.toDomain();

    // then
    assertThat(domain.alcoholDelivery()).isTrue();
    assertThat(domain.contactlessDelivery()).isFalse();
    assertThat(domain.reservedDelivery()).isTrue();
    assertThat(domain.reservedDeliveryStartTime()).isEqualTo(reservedTime);
    assertThat(domain.pickupRequestTime()).isEqualTo(pickupTime);
  }

  @Test
  @DisplayName("Domain → Entity → Domain 왕복 변환")
  void round_trip_conversion() {
    // given
    Instant pickupTime = Instant.parse("2025-01-20T09:30:00Z");

    DeliveryPolicy original =
        new DeliveryPolicy(false, true, false, null, pickupTime);

    // when
    OrderDeliveryPolicyEntity entity = OrderDeliveryPolicyEntity.from(original, 123L);
    DeliveryPolicy converted = entity.toDomain();

    // then
    assertThat(converted.alcoholDelivery()).isEqualTo(original.alcoholDelivery());
    assertThat(converted.contactlessDelivery()).isEqualTo(original.contactlessDelivery());
    assertThat(converted.reservedDelivery()).isEqualTo(original.reservedDelivery());
    assertThat(converted.reservedDeliveryStartTime())
        .isEqualTo(original.reservedDeliveryStartTime());
    assertThat(converted.pickupRequestTime()).isEqualTo(original.pickupRequestTime());
  }
}

