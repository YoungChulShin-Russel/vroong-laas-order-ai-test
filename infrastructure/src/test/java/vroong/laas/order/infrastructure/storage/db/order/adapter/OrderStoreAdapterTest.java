package vroong.laas.order.infrastructure.storage.db.order.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import vroong.laas.order.core.domain.order.DeliveryPolicy;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.EntranceInfo;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.order.OrderNumber;
import vroong.laas.order.core.domain.order.OrderStatus;
import vroong.laas.order.core.domain.order.Origin;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.Contact;
import vroong.laas.order.core.domain.shared.LatLng;
import vroong.laas.order.core.domain.shared.Money;
import vroong.laas.order.infrastructure.storage.db.order.OrderDeliveryPolicyJpaRepository;
import vroong.laas.order.infrastructure.storage.db.order.OrderItemJpaRepository;
import vroong.laas.order.infrastructure.storage.db.order.OrderJpaRepository;
import vroong.laas.order.infrastructure.storage.db.order.OrderLocationJpaRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(OrderStoreAdapter.class)
@DisplayName("OrderStoreAdapter 통합 테스트")
class OrderStoreAdapterTest {

  @Autowired private OrderStoreAdapter orderStoreAdapter;

  @Autowired private OrderJpaRepository orderJpaRepository;

  @Autowired private OrderItemJpaRepository orderItemJpaRepository;

  @Autowired private OrderLocationJpaRepository orderLocationJpaRepository;

  @Autowired private OrderDeliveryPolicyJpaRepository orderDeliveryPolicyJpaRepository;

  @Test
  @DisplayName("새로운 주문 저장")
  void store_new_order() {
    // given
    Order order = createOrder(null, "ORD-NEW-001", OrderStatus.CREATED);

    // when
    Order saved = orderStoreAdapter.store(order);

    // then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getOrderNumber().value()).isEqualTo("ORD-NEW-001");
    assertThat(saved.getStatus()).isEqualTo(OrderStatus.CREATED);
    assertThat(saved.getItems()).hasSize(2);
    assertThat(saved.getOrigin()).isNotNull();
    assertThat(saved.getDestination()).isNotNull();
    assertThat(saved.getDeliveryPolicy()).isNotNull();

    // DB 확인
    assertThat(orderJpaRepository.findById(saved.getId())).isPresent();
    assertThat(orderItemJpaRepository.findByOrderId(saved.getId())).hasSize(2);
    assertThat(orderLocationJpaRepository.findByOrderId(saved.getId())).isPresent();
    assertThat(orderDeliveryPolicyJpaRepository.findByOrderId(saved.getId())).isPresent();
  }

  @Test
  @DisplayName("ID가 있는 주문은 저장할 수 없다")
  void cannot_store_order_with_id() {
    // given
    Order order = createOrder(999L, "ORD-WITH-ID-001", OrderStatus.CREATED);

    // when & then
    assertThatThrownBy(() -> orderStoreAdapter.store(order))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("신규 주문은 ID가 없어야 합니다");
  }

  @Test
  @DisplayName("주문 아이템이 많은 주문 저장")
  void store_order_with_many_items() {
    // given
    List<OrderItem> manyItems =
        List.of(
            createOrderItem("상품1"),
            createOrderItem("상품2"),
            createOrderItem("상품3"),
            createOrderItem("상품4"),
            createOrderItem("상품5"));

    Order order =
        new Order(
            null,
            OrderNumber.of("ORD-MANY-ITEMS-001"),
            OrderStatus.CREATED,
            manyItems,
            createOrigin(),
            createDestination(),
            createDeliveryPolicy(),
            Instant.now(),
            null,
            null);

    // when
    Order saved = orderStoreAdapter.store(order);

    // then
    assertThat(saved.getItems()).hasSize(5);
    assertThat(orderItemJpaRepository.findByOrderId(saved.getId())).hasSize(5);
  }

  @Test
  @DisplayName("왕복 변환 테스트 (Domain → Entity → Domain)")
  void round_trip_conversion() {
    // given
    Order original = createOrder(null, "ORD-ROUNDTRIP-001", OrderStatus.CREATED);

    // when
    Order saved = orderStoreAdapter.store(original);

    // then - 모든 필드가 동일하게 유지되는지 확인
    assertThat(saved.getOrderNumber().value()).isEqualTo(original.getOrderNumber().value());
    assertThat(saved.getStatus()).isEqualTo(original.getStatus());
    assertThat(saved.getItems()).hasSize(original.getItems().size());

    // Origin 확인
    assertThat(saved.getOrigin().contact().name())
        .isEqualTo(original.getOrigin().contact().name());
    assertThat(saved.getOrigin().address().jibnunAddress())
        .isEqualTo(original.getOrigin().address().jibnunAddress());

    // Destination 확인
    assertThat(saved.getDestination().contact().name())
        .isEqualTo(original.getDestination().contact().name());

    // DeliveryPolicy 확인
    assertThat(saved.getDeliveryPolicy().alcoholDelivery())
        .isEqualTo(original.getDeliveryPolicy().alcoholDelivery());
  }

  // Helper methods
  private Order createOrder(Long id, String orderNumber, OrderStatus status) {
    return new Order(
        id,
        OrderNumber.of(orderNumber),
        status,
        List.of(createOrderItem("테스트상품1"), createOrderItem("테스트상품2")),
        createOrigin(),
        createDestination(),
        createDeliveryPolicy(),
        Instant.now(),
        null,
        null);
  }

  private OrderItem createOrderItem(String name) {
    return new OrderItem(name, 2, new Money(new BigDecimal("10000")), "식품", null, null);
  }

  private Origin createOrigin() {
    return new Origin(
        new Contact("홍길동", "010-1234-5678"),
        new Address("역삼동 123-45", "서울시 강남구", "1층"),
        new LatLng(new BigDecimal("37.5665"), new BigDecimal("126.9780")),
        new EntranceInfo("1234", "정문", "빠른배송"));
  }

  private Destination createDestination() {
    return new Destination(
        new Contact("김철수", "010-9876-5432"),
        new Address("서초동 567-89", "서울시 서초구", "3층"),
        new LatLng(new BigDecimal("37.4833"), new BigDecimal("127.0324")),
        new EntranceInfo("5678", "후문", "문앞"));
  }

  private DeliveryPolicy createDeliveryPolicy() {
    return new DeliveryPolicy(false, false, false, null, Instant.now());
  }
}

