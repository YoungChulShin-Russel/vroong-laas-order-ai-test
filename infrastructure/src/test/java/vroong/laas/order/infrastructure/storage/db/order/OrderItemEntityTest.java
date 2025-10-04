package vroong.laas.order.infrastructure.storage.db.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.shared.Money;
import vroong.laas.order.core.domain.shared.Volume;
import vroong.laas.order.core.domain.shared.Weight;

@DisplayName("OrderItemEntity 변환 테스트")
class OrderItemEntityTest {

  @Test
  @DisplayName("Domain → Entity 변환 (Weight, Volume 포함)")
  void from_domain_to_entity_with_weight_and_volume() {
    // given
    OrderItem domain =
        new OrderItem(
            "테스트 상품",
            5,
            new Money(new BigDecimal("10000")),
            "식품",
            new Weight(new BigDecimal("1.5")),
            new Volume(
                new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("30")));

    // when
    OrderItemEntity entity = OrderItemEntity.from(domain, 1L);

    // then
    assertThat(entity.getOrderId()).isEqualTo(1L);
    assertThat(entity.getItemName()).isEqualTo("테스트 상품");
    assertThat(entity.getQuantity()).isEqualTo(5);
    assertThat(entity.getPrice()).isEqualByComparingTo("10000");
    assertThat(entity.getCategory()).isEqualTo("식품");
    assertThat(entity.getWeight()).isEqualByComparingTo("1.5");
    assertThat(entity.getVolumeLength()).isEqualByComparingTo("10");
    assertThat(entity.getVolumeWidth()).isEqualByComparingTo("20");
    assertThat(entity.getVolumeHeight()).isEqualByComparingTo("30");
    assertThat(entity.getVolumeCbm()).isNotNull();
  }

  @Test
  @DisplayName("Domain → Entity 변환 (Weight, Volume null)")
  void from_domain_to_entity_without_weight_and_volume() {
    // given
    OrderItem domain =
        new OrderItem(
            "테스트 상품", 5, new Money(new BigDecimal("10000")), "식품", null, null);

    // when
    OrderItemEntity entity = OrderItemEntity.from(domain, 1L);

    // then
    assertThat(entity.getOrderId()).isEqualTo(1L);
    assertThat(entity.getItemName()).isEqualTo("테스트 상품");
    assertThat(entity.getWeight()).isNull();
    assertThat(entity.getVolumeLength()).isNull();
    assertThat(entity.getVolumeWidth()).isNull();
    assertThat(entity.getVolumeHeight()).isNull();
    assertThat(entity.getVolumeCbm()).isNull();
  }

  @Test
  @DisplayName("Entity → Domain 변환 (Weight, Volume 포함)")
  void from_entity_to_domain_with_weight_and_volume() {
    // given
    OrderItemEntity entity =
        OrderItemEntity.builder()
            .orderId(1L)
            .itemName("테스트 상품")
            .quantity(5)
            .price(new BigDecimal("10000"))
            .category("식품")
            .weight(new BigDecimal("1.5"))
            .volumeLength(new BigDecimal("10"))
            .volumeWidth(new BigDecimal("20"))
            .volumeHeight(new BigDecimal("30"))
            .volumeCbm(new BigDecimal("0.0060"))
            .build();

    // when
    OrderItem domain = entity.toDomain();

    // then
    assertThat(domain.itemName()).isEqualTo("테스트 상품");
    assertThat(domain.quantity()).isEqualTo(5);
    assertThat(domain.price().amount()).isEqualByComparingTo("10000");
    assertThat(domain.category()).isEqualTo("식품");
    assertThat(domain.weight()).isNotNull();
    assertThat(domain.weight().value()).isEqualByComparingTo("1.5");
    assertThat(domain.volume()).isNotNull();
    assertThat(domain.volume().length()).isEqualByComparingTo("10");
  }

  @Test
  @DisplayName("Entity → Domain 변환 (Weight, Volume null)")
  void from_entity_to_domain_without_weight_and_volume() {
    // given
    OrderItemEntity entity =
        OrderItemEntity.builder()
            .orderId(1L)
            .itemName("테스트 상품")
            .quantity(5)
            .price(new BigDecimal("10000"))
            .category("식품")
            .weight(null)
            .volumeLength(null)
            .volumeWidth(null)
            .volumeHeight(null)
            .volumeCbm(null)
            .build();

    // when
    OrderItem domain = entity.toDomain();

    // then
    assertThat(domain.itemName()).isEqualTo("테스트 상품");
    assertThat(domain.weight()).isNull();
    assertThat(domain.volume()).isNull();
  }

  @Test
  @DisplayName("Domain → Entity → Domain 왕복 변환")
  void round_trip_conversion() {
    // given
    OrderItem original =
        new OrderItem(
            "왕복 테스트",
            10,
            new Money(new BigDecimal("50000")),
            "전자제품",
            new Weight(new BigDecimal("2.5")),
            new Volume(
                new BigDecimal("15"), new BigDecimal("25"), new BigDecimal("35")));

    // when
    OrderItemEntity entity = OrderItemEntity.from(original, 999L);
    OrderItem converted = entity.toDomain();

    // then
    assertThat(converted.itemName()).isEqualTo(original.itemName());
    assertThat(converted.quantity()).isEqualTo(original.quantity());
    assertThat(converted.price().amount()).isEqualByComparingTo(original.price().amount());
    assertThat(converted.category()).isEqualTo(original.category());
    assertThat(converted.weight().value())
        .isEqualByComparingTo(original.weight().value());
  }
}

