package vroong.laas.order.domain.order.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderStatus;
import vroong.laas.order.core.domain.order.event.OrderCreatedEvent;
import vroong.laas.order.core.fixture.OrderFixtures;

class OrderCreatedEventTest {

  private FixtureMonkey fixtureMonkey;
  private OrderFixtures orderFixtures;

  @BeforeEach
  void setUp() {
    fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .defaultNotNull(true)
        .build();

    orderFixtures = new OrderFixtures(fixtureMonkey);
  }

  @Test
  @DisplayName("Order로부터 OrderCreatedEvent를 생성한다")
  void createEventFromOrder() {
    // given
    Order order = orderFixtures.orderWithId(1L);

    // when
    OrderCreatedEvent event = OrderCreatedEvent.from(order);

    // then - DomainEvent 필드
    assertThat(event.occurredAt()).isNotNull();
    
    // then - Order 기본 정보
    assertThat(event.orderId()).isEqualTo(order.getId());
    assertThat(event.orderNumber()).isEqualTo(order.getOrderNumber().value());
    assertThat(event.status()).isEqualTo(OrderStatus.CREATED);
    assertThat(event.orderedAt()).isEqualTo(order.getOrderedAt());
    
    // then - Order Items (Domain Model 그대로)
    assertThat(event.items()).hasSize(order.getItems().size());
    assertThat(event.items()).isEqualTo(order.getItems());
    assertThat(event.totalAmount()).isEqualTo(order.calculateTotalAmount());
    
    // then - Origin (Domain Model 그대로)
    assertThat(event.origin()).isEqualTo(order.getOrigin());
    
    // then - Destination (Domain Model 그대로)
    assertThat(event.destination()).isEqualTo(order.getDestination());
    
    // then - DeliveryPolicy (Domain Model 그대로)
    assertThat(event.deliveryPolicy()).isEqualTo(order.getDeliveryPolicy());
  }

  @Test
  @DisplayName("ID가 없는 Order로는 Event를 생성할 수 없다")
  void cannotCreateEventFromOrderWithoutId() {
    // given
    Order order = orderFixtures.order();  // ID 없음

    // when & then
    assertThatThrownBy(() -> OrderCreatedEvent.from(order))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("저장되지 않은 Order로는 Event를 생성할 수 없습니다");
  }

  @Test
  @DisplayName("Event는 DomainEvent 인터페이스를 구현한다")
  void implementsDomainEvent() {
    // given
    Order order = orderFixtures.orderWithId(1L);
    OrderCreatedEvent event = OrderCreatedEvent.from(order);

    // when & then
    assertThat(event).isInstanceOf(vroong.laas.order.core.domain.shared.event.DomainEvent.class);
  }
}

