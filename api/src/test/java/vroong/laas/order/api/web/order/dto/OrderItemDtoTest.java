package vroong.laas.order.api.web.order.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.order.core.domain.order.OrderItem;

@DisplayName("OrderItemDto 테스트")
class OrderItemDtoTest {

  @Test
  @DisplayName("모든 필드가 있는 OrderItemDto → OrderItem Domain 변환")
  void toDomain_withAllFields() {
    // given
    OrderItemDto dto =
        new OrderItemDto(
            "상품A",
            2,
            new BigDecimal("10000"),
            "식품",
            new BigDecimal("1.5"),
            new BigDecimal("10.0"),
            new BigDecimal("20.0"),
            new BigDecimal("30.0"));

    // when
    OrderItem domain = dto.toDomain();

    // then
    assertThat(domain).isNotNull();
    assertThat(domain.itemName()).isEqualTo("상품A");
    assertThat(domain.quantity()).isEqualTo(2);
    assertThat(domain.price().amount()).isEqualByComparingTo(new BigDecimal("10000"));
    assertThat(domain.category()).isEqualTo("식품");
    assertThat(domain.weight()).isNotNull();
    assertThat(domain.weight().value()).isEqualByComparingTo(new BigDecimal("1.5"));
    assertThat(domain.volume()).isNotNull();
    assertThat(domain.volume().length()).isEqualByComparingTo(new BigDecimal("30.0"));  // depthInCm → length
    assertThat(domain.volume().width()).isEqualByComparingTo(new BigDecimal("10.0"));
    assertThat(domain.volume().height()).isEqualByComparingTo(new BigDecimal("20.0"));
  }

  @Test
  @DisplayName("무게와 부피가 없어도 변환된다")
  void toDomain_withoutWeightAndVolume() {
    // given
    OrderItemDto dto =
        new OrderItemDto("상품B", 1, new BigDecimal("5000"), "생활용품", null, null, null, null);

    // when
    OrderItem domain = dto.toDomain();

    // then
    assertThat(domain.itemName()).isEqualTo("상품B");
    assertThat(domain.quantity()).isEqualTo(1);
    assertThat(domain.price().amount()).isEqualByComparingTo(new BigDecimal("5000"));
    assertThat(domain.category()).isEqualTo("생활용품");
    assertThat(domain.weight()).isNull();
    assertThat(domain.volume()).isNull();
  }

  @Test
  @DisplayName("부피의 일부 필드만 있으면 부피는 null이다")
  void toDomain_withPartialVolume() {
    // given - width만 있고 height, depth는 null
    OrderItemDto dto =
        new OrderItemDto(
            "상품C",
            1,
            new BigDecimal("3000"),
            "전자제품",
            null,
            new BigDecimal("10.0"),
            null,
            null);

    // when
    OrderItem domain = dto.toDomain();

    // then
    assertThat(domain.volume()).isNull();
  }

  @Test
  @DisplayName("수량이 여러 개여도 정상 변환된다")
  void toDomain_withMultipleQuantity() {
    // given
    OrderItemDto dto =
        new OrderItemDto("상품D", 100, new BigDecimal("1000"), "식품", null, null, null, null);

    // when
    OrderItem domain = dto.toDomain();

    // then
    assertThat(domain.quantity()).isEqualTo(100);
  }
}
