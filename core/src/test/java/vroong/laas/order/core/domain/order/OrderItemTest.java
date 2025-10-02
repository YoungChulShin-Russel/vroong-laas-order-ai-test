package vroong.laas.order.core.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.order.core.domain.shared.Money;

class OrderItemTest {

  @Test
  @DisplayName("주문 아이템을 생성한다")
  void createOrderItem() {
    // given & when
    OrderItem item =
        new OrderItem(
            "테스트 상품",
            2,
            new Money(BigDecimal.valueOf(10_000)),
            "식품",
            null,
            null);

    // then
    assertThat(item.itemName()).isEqualTo("테스트 상품");
    assertThat(item.quantity()).isEqualTo(2);
    assertThat(item.price().amount()).isEqualByComparingTo(BigDecimal.valueOf(10_000));
  }

  @Test
  @DisplayName("상품명이 없으면 예외가 발생한다")
  void createOrderItemWithoutName() {
    // when & then
    assertThatThrownBy(
            () ->
                new OrderItem(
                    null, 2, new Money(BigDecimal.valueOf(10_000)), "식품", null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("상품명은 필수입니다");
  }

  @Test
  @DisplayName("수량이 0 이하이면 예외가 발생한다")
  void createOrderItemWithZeroQuantity() {
    // when & then
    assertThatThrownBy(
            () ->
                new OrderItem(
                    "테스트 상품", 0, new Money(BigDecimal.valueOf(10_000)), "식품", null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("수량은 1개 이상이어야 합니다");
  }

  @Test
  @DisplayName("가격이 없으면 예외가 발생한다")
  void createOrderItemWithoutPrice() {
    // when & then
    assertThatThrownBy(() -> new OrderItem("테스트 상품", 2, null, "식품", null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("가격은 필수입니다");
  }

  @Test
  @DisplayName("총 가격을 계산한다")
  void getTotalPrice() {
    // given
    OrderItem item =
        new OrderItem(
            "테스트 상품",
            3,
            new Money(BigDecimal.valueOf(10_000)),
            "식품",
            null,
            null);

    // when
    Money totalPrice = item.getTotalPrice();

    // then
    assertThat(totalPrice.amount()).isEqualByComparingTo(BigDecimal.valueOf(30_000));
  }
}

