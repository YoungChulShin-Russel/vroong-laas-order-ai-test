package vroong.laas.order.infrastructure.storage.db.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.order.core.domain.order.DeliveryPolicy;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.EntranceInfo;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.order.OrderNumber;
import vroong.laas.order.core.domain.order.Origin;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.Contact;
import vroong.laas.order.core.domain.shared.LatLng;
import vroong.laas.order.core.domain.shared.Money;

@DisplayName("OrderEntity 변환 테스트")
class OrderEntityTest {

  @Test
  @DisplayName("Domain → Entity 변환 (OrderStatus 변환 포함)")
  void from_domain_to_entity() {
    // given
    Instant now = Instant.now();

    Order domain =
        new Order(
            null,
            OrderNumber.of("ORD-20250115-001"),
            vroong.laas.order.core.domain.order.OrderStatus.CREATED,
            List.of(),
            createOrigin(),
            createDestination(),
            createDeliveryPolicy(),
            now,
            null,
            null);

    // when
    OrderEntity entity = OrderEntity.from(domain);

    // then
    assertThat(entity.getOrderNumber()).isEqualTo("ORD-20250115-001");
    assertThat(entity.getStatus()).isEqualTo(OrderStatus.CREATED);
    assertThat(entity.getOrderedAt()).isEqualTo(now);
    assertThat(entity.getDeliveredAt()).isNull();
    assertThat(entity.getCancelledAt()).isNull();
  }

  @Test
  @DisplayName("Entity → Domain 변환 (연관 Entity 포함)")
  void from_entity_to_domain() {
    // given
    Instant now = Instant.now();

    OrderEntity orderEntity =
        OrderEntity.builder()
            .orderNumber("ORD-20250115-002")
            .status(OrderStatus.DELIVERED)
            .orderedAt(now.minusSeconds(3600))
            .deliveredAt(now)
            .cancelledAt(null)
            .build();

    OrderLocationEntity locationEntity = createLocationEntity(1L);
    OrderDeliveryPolicyEntity policyEntity = createPolicyEntity(1L);
    List<OrderItemEntity> itemEntities = createItemEntities(1L);

    // when
    Order domain = orderEntity.toDomain(locationEntity, policyEntity, itemEntities);

    // then
    assertThat(domain.getOrderNumber().value()).isEqualTo("ORD-20250115-002");
    assertThat(domain.getStatus())
        .isEqualTo(vroong.laas.order.core.domain.order.OrderStatus.DELIVERED);
    assertThat(domain.getOrderedAt()).isEqualTo(now.minusSeconds(3600));
    assertThat(domain.getDeliveredAt()).isEqualTo(now);
    assertThat(domain.getCancelledAt()).isNull();
    assertThat(domain.getItems()).hasSize(2);
    assertThat(domain.getOrigin()).isNotNull();
    assertThat(domain.getDestination()).isNotNull();
    assertThat(domain.getDeliveryPolicy()).isNotNull();
  }

  @Test
  @DisplayName("OrderStatus 변환 (CREATED)")
  void convert_order_status_created() {
    // given
    Order domain =
        new Order(
            null,
            OrderNumber.of("ORD-TEST-001"),
            vroong.laas.order.core.domain.order.OrderStatus.CREATED,
            List.of(),
            createOrigin(),
            createDestination(),
            createDeliveryPolicy(),
            Instant.now(),
            null,
            null);

    // when
    OrderEntity entity = OrderEntity.from(domain);

    // then
    assertThat(entity.getStatus()).isEqualTo(OrderStatus.CREATED);
  }

  @Test
  @DisplayName("OrderStatus 변환 (DELIVERED)")
  void convert_order_status_delivered() {
    // given
    Order domain =
        new Order(
            1L,
            OrderNumber.of("ORD-TEST-002"),
            vroong.laas.order.core.domain.order.OrderStatus.DELIVERED,
            List.of(),
            createOrigin(),
            createDestination(),
            createDeliveryPolicy(),
            Instant.now().minusSeconds(3600),
            Instant.now(),
            null);

    // when
    OrderEntity entity = OrderEntity.from(domain);

    // then
    assertThat(entity.getStatus()).isEqualTo(OrderStatus.DELIVERED);
  }

  @Test
  @DisplayName("OrderStatus 변환 (CANCELLED)")
  void convert_order_status_cancelled() {
    // given
    Order domain =
        new Order(
            1L,
            OrderNumber.of("ORD-TEST-003"),
            vroong.laas.order.core.domain.order.OrderStatus.CANCELLED,
            List.of(),
            createOrigin(),
            createDestination(),
            createDeliveryPolicy(),
            Instant.now().minusSeconds(3600),
            null,
            Instant.now());

    // when
    OrderEntity entity = OrderEntity.from(domain);

    // then
    assertThat(entity.getStatus()).isEqualTo(OrderStatus.CANCELLED);
  }

  // Helper methods
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

  private OrderLocationEntity createLocationEntity(Long orderId) {
    return OrderLocationEntity.builder()
        .orderId(orderId)
        .originContactName("홍길동")
        .originContactPhoneNumber("010-1234-5678")
        .originJibnunAddress("역삼동 123-45")
        .originRoadAddress("서울시 강남구")
        .originDetailAddress("1층")
        .originLatitude(new BigDecimal("37.5665"))
        .originLongitude(new BigDecimal("126.9780"))
        .originEntrancePassword("1234")
        .originEntranceGuide("정문")
        .originRequestMessage("빠른배송")
        .destinationContactName("김철수")
        .destinationContactPhoneNumber("010-9876-5432")
        .destinationJibnunAddress("서초동 567-89")
        .destinationRoadAddress("서울시 서초구")
        .destinationDetailAddress("3층")
        .destinationLatitude(new BigDecimal("37.4833"))
        .destinationLongitude(new BigDecimal("127.0324"))
        .destinationEntrancePassword("5678")
        .destinationEntranceGuide("후문")
        .destinationRequestMessage("문앞")
        .build();
  }

  private OrderDeliveryPolicyEntity createPolicyEntity(Long orderId) {
    DeliveryPolicy policy = new DeliveryPolicy(false, false, false, null, Instant.now());
    return OrderDeliveryPolicyEntity.from(policy, orderId);
  }

  private List<OrderItemEntity> createItemEntities(Long orderId) {
    OrderItem item1 =
        new OrderItem(
            "상품1", 2, new Money(new BigDecimal("10000")), "식품", null, null);

    OrderItem item2 =
        new OrderItem(
            "상품2", 3, new Money(new BigDecimal("20000")), "생활용품", null, null);

    return List.of(
        OrderItemEntity.from(item1, orderId), OrderItemEntity.from(item2, orderId));
  }
}

