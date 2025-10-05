package vroong.laas.order.core.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.order.core.domain.shared.Money;
import vroong.laas.order.core.fixture.OrderFixtures;

class OrderTest {

  private OrderFixtures orderFixtures;

  @BeforeEach
  void setUp() {
    FixtureMonkey fixtureMonkey =
        FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .defaultNotNull(true)
            .build();

    orderFixtures = new OrderFixtures(fixtureMonkey);
  }

  @Test
  @DisplayName("주문을 생성하면 CREATED 상태로 시작한다")
  void create() {
    // given & when
    Order order = orderFixtures.order();

    // then
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    assertThat(order.getOrderedAt()).isNotNull();
    assertThat(order.getItems()).isNotEmpty();
  }

  @Test
  @DisplayName("배송 정책이 없으면 예외가 발생한다")
  void createWithoutDeliveryPolicy() {
    // when & then
    assertThatThrownBy(
            () ->
                Order.create(
                    orderFixtures.orderNumberGenerator(),
                    orderFixtures.randomOrderItems(),
                    orderFixtures.randomOrigin(),
                    orderFixtures.randomDestination(),
                    null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("배송 정책은 필수입니다");
  }

  @Test
  @DisplayName("CREATED 상태에서 배송 완료 처리할 수 있다")
  void markAsDelivered() {
    // given
    Order order = orderFixtures.order();

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
    Order order = orderFixtures.cancelledOrder();

    // when & then
    assertThatThrownBy(order::markAsDelivered)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("취소된 주문은 배송 완료 처리할 수 없습니다");
  }

  @Test
  @DisplayName("이미 배송 완료된 주문은 다시 배송 완료 처리할 수 없다")
  void markAsDeliveredWhenAlreadyDelivered() {
    // given
    Order order = orderFixtures.deliveredOrder();

    // when & then
    assertThatThrownBy(order::markAsDelivered)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("이미 배송 완료된 주문입니다");
  }

  @Test
  @DisplayName("CREATED 상태에서 주문을 취소할 수 있다")
  void cancel() {
    // given
    Order order = orderFixtures.order();

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
    Order order = orderFixtures.deliveredOrder();

    // when & then
    assertThatThrownBy(order::cancel)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("배송 완료된 주문은 취소할 수 없습니다");
  }

  @Test
  @DisplayName("이미 취소된 주문은 다시 취소할 수 없다")
  void cancelAlreadyCancelledOrder() {
    // given
    Order order = orderFixtures.cancelledOrder();

    // when & then
    assertThatThrownBy(order::cancel)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("이미 취소된 주문입니다");
  }

  @Test
  @DisplayName("총 금액을 계산한다")
  void calculateTotalAmount() {
    // given
    Order order = orderFixtures.order();

    // when
    Money totalAmount = order.calculateTotalAmount();

    // then
    assertThat(totalAmount).isNotNull();
    assertThat(totalAmount.amount()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
  }

  @Test
  @DisplayName("ID를 할당할 수 있다")
  void assignId() {
    // given
    Order order = orderFixtures.order();

    // when
    order.assignId(1L);

    // then
    assertThat(order.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("이미 ID가 할당된 주문에는 ID를 다시 할당할 수 없다")
  void assignIdTwice() {
    // given
    Order order = orderFixtures.orderWithId(1L);

    // when & then
    assertThatThrownBy(() -> order.assignId(2L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("이미 ID가 할당된 주문입니다");
  }
}
