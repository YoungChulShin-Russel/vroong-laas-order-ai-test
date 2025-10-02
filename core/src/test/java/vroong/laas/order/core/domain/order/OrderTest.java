package vroong.laas.order.core.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static vroong.laas.order.core.fixture.OrderFixture.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.order.core.domain.shared.Money;

class OrderTest {

  @Test
  @DisplayName("주문을 생성하면 CREATED 상태로 시작한다")
  void create() {
    // given & when
    Order order = createOrder();

    // then
    assertThat(order.getOrderNumber()).isEqualTo("ORD-20251002-001");
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    assertThat(order.getOrderedAt()).isNotNull();
    assertThat(order.getItems()).hasSize(2);
  }

  @Test
  @DisplayName("주문번호가 없으면 예외가 발생한다")
  void createWithoutOrderNumber() {
    // when & then
    assertThatThrownBy(
            () ->
                Order.create(
                    null,
                    createOrderItems(),
                    createOrigin(),
                    createDestination(),
                    createDeliveryPolicy()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("주문번호는 필수입니다");
  }

  @Test
  @DisplayName("주문 아이템이 없으면 예외가 발생한다")
  void createWithoutItems() {
    // when & then
    assertThatThrownBy(
            () ->
                Order.create(
                    "ORD-20251002-001",
                    List.of(),
                    createOrigin(),
                    createDestination(),
                    createDeliveryPolicy()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("주문 아이템은 최소 1개 이상이어야 합니다");
  }

  @Test
  @DisplayName("CREATED 상태에서 배송 완료 처리할 수 있다")
  void markAsDelivered() {
    // given
    Order order = createOrder();

    // when
    order.markAsDelivered();

    // then
    assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    assertThat(order.getDeliveredAt()).isNotNull();
  }

  @Test
  @DisplayName("취소된 주문은 배송 완료 처리할 수 없다")
  void markAsDeliveredWhenCancelled() {
    // given
    Order order = createOrder();
    order.cancel();

    // when & then
    assertThatThrownBy(order::markAsDelivered)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("취소된 주문은 배송 완료 처리할 수 없습니다");
  }

  @Test
  @DisplayName("이미 배송 완료된 주문은 다시 배송 완료 처리할 수 없다")
  void markAsDeliveredWhenAlreadyDelivered() {
    // given
    Order order = createOrder();
    order.markAsDelivered();

    // when & then
    assertThatThrownBy(order::markAsDelivered)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("이미 배송 완료된 주문입니다");
  }

  @Test
  @DisplayName("CREATED 상태에서 주문을 취소할 수 있다")
  void cancel() {
    // given
    Order order = createOrder();

    // when
    order.cancel();

    // then
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    assertThat(order.getCancelledAt()).isNotNull();
  }

  @Test
  @DisplayName("DELIVERED 상태에서는 주문을 취소할 수 없다")
  void cancelDeliveredOrder() {
    // given
    Order order = createOrder();
    order.markAsDelivered();

    // when & then
    assertThatThrownBy(order::cancel)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("배송 완료된 주문은 취소할 수 없습니다");
  }

  @Test
  @DisplayName("이미 취소된 주문은 다시 취소할 수 없다")
  void cancelAlreadyCancelledOrder() {
    // given
    Order order = createOrder();
    order.cancel();

    // when & then
    assertThatThrownBy(order::cancel)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("이미 취소된 주문입니다");
  }

  @Test
  @DisplayName("총 금액을 계산한다")
  void calculateTotalAmount() {
    // given
    Order order = createOrder();

    // when
    Money totalAmount = order.calculateTotalAmount();

    // then
    // Item1: 10,000 * 2 = 20,000
    // Item2: 5,000 * 3 = 15,000
    // Total: 35,000
    assertThat(totalAmount.amount()).isEqualByComparingTo(BigDecimal.valueOf(35_000));
  }

  @Test
  @DisplayName("ID를 할당할 수 있다")
  void assignId() {
    // given
    Order order = createOrder();

    // when
    order.assignId(1L);

    // then
    assertThat(order.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("이미 ID가 할당된 주문에는 ID를 다시 할당할 수 없다")
  void assignIdTwice() {
    // given
    Order order = createOrder();
    order.assignId(1L);

    // when & then
    assertThatThrownBy(() -> order.assignId(2L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("이미 ID가 할당된 주문입니다");
  }
}
