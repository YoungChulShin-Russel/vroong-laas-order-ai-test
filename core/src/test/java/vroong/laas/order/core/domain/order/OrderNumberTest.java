package vroong.laas.order.core.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderNumberTest {

  @Test
  @DisplayName("유효한 주문번호로 생성할 수 있다")
  void createValidOrderNumber() {
    // given
    String validNumber = "ORD-20251002-001";

    // when
    OrderNumber orderNumber = OrderNumber.of(validNumber);

    // then
    assertThat(orderNumber.value()).isEqualTo(validNumber);
    assertThat(orderNumber.toString()).isEqualTo(validNumber);
  }

  @Test
  @DisplayName("주문번호가 null이면 예외가 발생한다")
  void createWithNull() {
    // when & then
    assertThatThrownBy(() -> OrderNumber.of(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("주문번호는 필수입니다");
  }

  @Test
  @DisplayName("주문번호가 빈 문자열이면 예외가 발생한다")
  void createWithBlank() {
    // when & then
    assertThatThrownBy(() -> OrderNumber.of(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("주문번호는 필수입니다");
  }

  @Test
  @DisplayName("주문번호가 'ORD-'로 시작하지 않으면 예외가 발생한다")
  void createWithInvalidPrefix() {
    // when & then
    assertThatThrownBy(() -> OrderNumber.of("ORDER-001"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("주문번호는 'ORD-'로 시작해야 합니다");

    assertThatThrownBy(() -> OrderNumber.of("ABC-001"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("주문번호는 'ORD-'로 시작해야 합니다");

    assertThatThrownBy(() -> OrderNumber.of("ord-001"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("주문번호는 'ORD-'로 시작해야 합니다");
  }

  @Test
  @DisplayName("같은 값을 가진 OrderNumber는 동등하다")
  void equality() {
    // given
    OrderNumber orderNumber1 = OrderNumber.of("ORD-001");
    OrderNumber orderNumber2 = OrderNumber.of("ORD-001");
    OrderNumber orderNumber3 = OrderNumber.of("ORD-002");

    // when & then
    assertThat(orderNumber1).isEqualTo(orderNumber2);
    assertThat(orderNumber1).isNotEqualTo(orderNumber3);
    assertThat(orderNumber1.hashCode()).isEqualTo(orderNumber2.hashCode());
  }
}

