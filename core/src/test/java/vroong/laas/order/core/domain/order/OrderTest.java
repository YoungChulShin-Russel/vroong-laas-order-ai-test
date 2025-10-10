package vroong.laas.order.core.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
                new Order(
                    1L,
                    orderFixtures.orderNumberGenerator().generate(),
                    OrderStatus.CREATED,
                    orderFixtures.randomOrderItems(),
                    orderFixtures.randomOrigin(),
                    orderFixtures.randomDestination(),
                    null,
                    java.time.Instant.now(),
                    null,
                    null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("배송 정책은 필수입니다");
  }

}
