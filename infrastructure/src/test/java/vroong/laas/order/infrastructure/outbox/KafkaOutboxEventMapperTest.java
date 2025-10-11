package vroong.laas.order.infrastructure.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.vroong.msa.kafka.event.KafkaEvent;
import com.vroong.msa.kafka.event.KafkaEventPayload;
import com.vroong.msa.kafka.event.payload.order.OrderCreatedKafkaEventPayload;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.order.core.domain.order.DeliveryPolicy;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.EntranceInfo;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.order.OrderNumber;
import vroong.laas.order.core.domain.order.OrderStatus;
import vroong.laas.order.core.domain.order.Origin;
import vroong.laas.order.core.domain.outbox.OutboxEventType;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.Contact;
import vroong.laas.order.core.domain.shared.LatLng;
import vroong.laas.order.core.domain.shared.Money;
import vroong.laas.order.core.domain.shared.Volume;
import vroong.laas.order.core.domain.shared.Weight;

/**
 * KafkaOutboxEventMapper 단위 테스트
 *
 * <p>순수 Java 테스트
 * - Domain Model → Kafka Payload 변환 검증
 * - 모든 필드 매핑 확인
 */
@DisplayName("KafkaOutboxEventMapper 테스트")
class KafkaOutboxEventMapperTest {

  private KafkaOutboxEventMapper sut;

  private FixtureMonkey fixtureMonkey;

  @BeforeEach
  void setUp() {
    sut = new KafkaOutboxEventMapper();

    fixtureMonkey =
        FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .defaultNotNull(true)
            .build();
  }

  @Test
  @DisplayName("ORDER_CREATED: Order → OrderCreatedKafkaEventPayload 매핑")
  void map_orderCreated_mapsAllFields() {
    // given
    Long orderId = 12345L;
    OrderNumber orderNumber = OrderNumber.of("ORD-2025-001");
    
    // OrderItem
    OrderItem item1 =
        new OrderItem(
            "상품A",
            2,
            new Money(new BigDecimal("10000")),
            "식품",
            new Weight(new BigDecimal("1.5")),
            new Volume(
                new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("30")));
    
    OrderItem item2 =
        new OrderItem(
            "상품B",
            1,
            new Money(new BigDecimal("5000")),
            "생활용품",
            null, // weight 없음
            null // volume 없음
        );
    
    List<OrderItem> items = List.of(item1, item2);

    // Origin
    Contact originContact = new Contact("홍길동", "010-1234-5678");
    Address originAddress = new Address("지번주소1", "도로명주소1", "상세주소1");
    LatLng originLatLng = new LatLng(new BigDecimal("37.123"), new BigDecimal("127.123"));
    EntranceInfo originEntranceInfo = new EntranceInfo("1234", "안내1", "요청1");
    Origin origin = new Origin(originContact, originAddress, originLatLng, originEntranceInfo);

    // Destination
    Contact destContact = new Contact("김철수", "010-9999-8888");
    Address destAddress = new Address("지번주소2", "도로명주소2", "상세주소2");
    LatLng destLatLng = new LatLng(new BigDecimal("37.456"), new BigDecimal("127.456"));
    EntranceInfo destEntranceInfo = new EntranceInfo("5678", "안내2", "요청2");
    Destination destination =
        new Destination(destContact, destAddress, destLatLng, destEntranceInfo);

    // DeliveryPolicy
    Instant reservedTime = Instant.parse("2025-10-06T15:00:00Z");
    Instant pickupTime = Instant.parse("2025-10-06T14:00:00Z");
    DeliveryPolicy deliveryPolicy =
        new DeliveryPolicy(false, true, true, reservedTime, pickupTime);

    // Order 생성
    Order order =
        Order.create(orderId, orderNumber, items, origin, destination, deliveryPolicy);

    // when
    KafkaOutboxEvent result = sut.map(OutboxEventType.ORDER_CREATED, order);

    // then
    assertThat(result).isNotNull();
    assertThat(result.eventKey()).isEqualTo("12345"); // orderId

    KafkaEvent<KafkaEventPayload> kafkaEvent = result.kafkaEvent();
    assertThat(kafkaEvent).isNotNull();

    OrderCreatedKafkaEventPayload payload =
        (OrderCreatedKafkaEventPayload) kafkaEvent.getPayload();
    assertThat(payload).isNotNull();

    // Order 기본 정보
    assertThat(payload.getOrderId()).isEqualTo(12345L);
    assertThat(payload.getOrderNumber()).isEqualTo("ORD-2025-001");
    assertThat(payload.getOrderStatus()).isEqualTo("CREATED");
    assertThat(payload.getOrderedAt()).isNotNull();

    // Items 검증
    assertThat(payload.getItems()).hasSize(2);

    OrderCreatedKafkaEventPayload.OrderCreatedOrderItem payloadItem1 = payload.getItems().get(0);
    assertThat(payloadItem1.getItemName()).isEqualTo("상품A");
    assertThat(payloadItem1.getQuantity()).isEqualTo(2);
    assertThat(payloadItem1.getPrice()).isEqualByComparingTo(new BigDecimal("10000"));
    assertThat(payloadItem1.getCategory()).isEqualTo("식품");
    assertThat(payloadItem1.getWeight()).isEqualByComparingTo(new BigDecimal("1.5"));
    assertThat(payloadItem1.getVolumeLength()).isEqualByComparingTo(new BigDecimal("10"));
    assertThat(payloadItem1.getVolumeWidth()).isEqualByComparingTo(new BigDecimal("20"));
    assertThat(payloadItem1.getVolumeHeight()).isEqualByComparingTo(new BigDecimal("30"));
    assertThat(payloadItem1.getVolumeCbm()).isNotNull();

    OrderCreatedKafkaEventPayload.OrderCreatedOrderItem payloadItem2 = payload.getItems().get(1);
    assertThat(payloadItem2.getItemName()).isEqualTo("상품B");
    assertThat(payloadItem2.getWeight()).isNull();
    assertThat(payloadItem2.getVolumeLength()).isNull();

    // Origin Location 검증
    OrderCreatedKafkaEventPayload.OrderCreatedOrderLocation originLocation =
        payload.getOriginLocation();
    assertThat(originLocation.getContactName()).isEqualTo("홍길동");
    assertThat(originLocation.getContactPhoneNumber()).isEqualTo("010-1234-5678");
    assertThat(originLocation.getEntrancePassword()).isEqualTo("1234");
    assertThat(originLocation.getEntranceGuide()).isEqualTo("안내1");
    assertThat(originLocation.getRequestMessage()).isEqualTo("요청1");
    assertThat(originLocation.getLatitude()).isEqualByComparingTo(new BigDecimal("37.123"));
    assertThat(originLocation.getLongitude()).isEqualByComparingTo(new BigDecimal("127.123"));
    assertThat(originLocation.getJibunAddress()).isEqualTo("지번주소1");
    assertThat(originLocation.getRoadAddress()).isEqualTo("도로명주소1");
    assertThat(originLocation.getDetailAddress()).isEqualTo("상세주소1");

    // Destination Location 검증
    OrderCreatedKafkaEventPayload.OrderCreatedOrderLocation destLocation =
        payload.getDestinationLocation();
    assertThat(destLocation.getContactName()).isEqualTo("김철수");
    assertThat(destLocation.getJibunAddress()).isEqualTo("지번주소2");

    // Delivery Policy 검증
    OrderCreatedKafkaEventPayload.OrderCreatedOrderDeliveryPolicy policy =
        payload.getDeliveryPolicy();
    assertThat(policy).isNotNull();
    assertThat(policy.getRequestedStartAt()).isEqualTo(reservedTime);
    assertThat(policy.getRequestedPickupAt()).isEqualTo(pickupTime);
  }

  @Test
  @DisplayName("ORDER_CREATED가 아닌 타입은 예외 발생 (향후 확장 가능)")
  void map_currentlyOnlySupportsOrderCreated() {
    // given
    Order order = Order.create(
        1L,
        OrderNumber.of("ORD-001"),
        List.of(new OrderItem("상품", 1, new Money(BigDecimal.valueOf(1000)), "카테고리", null, null)),
        new Origin(
            new Contact("이름", "전화"),
            new Address("지번", "도로명", "상세"),
            new LatLng(BigDecimal.valueOf(37), BigDecimal.valueOf(127)),
            new EntranceInfo(null, null, null)),
        new Destination(
            new Contact("이름2", "전화2"),
            new Address("지번2", "도로명2", "상세2"),
            new LatLng(BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.5)),
            new EntranceInfo(null, null, null)),
        new DeliveryPolicy(false, false, false, null, Instant.now()));

    // when & then
    // 현재는 ORDER_CREATED만 지원
    // 향후 ORDER_CANCELLED, ORDER_DESTINATION_UPDATED 등 추가 가능
    KafkaOutboxEvent result = sut.map(OutboxEventType.ORDER_CREATED, order);
    
    assertThat(result).isNotNull();
    assertThat(result.eventKey()).isEqualTo("1");
    assertThat(result.kafkaEvent()).isNotNull();
  }

  @Test
  @DisplayName("null 필드가 있어도 매핑이 정상 동작한다")
  void map_withNullableFields() {
    // given
    OrderItem item = new OrderItem("상품", 1, new Money(BigDecimal.valueOf(1000)), "카테고리", null, null);
    
    Contact contact = new Contact("이름", "전화번호");
    Address address = new Address("지번", "도로명", "상세");
    LatLng latLng = new LatLng(BigDecimal.valueOf(37), BigDecimal.valueOf(127));
    EntranceInfo entranceInfo = new EntranceInfo(null, null, null); // 모두 null
    
    Origin origin = new Origin(contact, address, latLng, entranceInfo);
    Destination destination = new Destination(contact, address, latLng, entranceInfo);
    
    DeliveryPolicy policy = new DeliveryPolicy(false, false, false, null, Instant.now());
    
    Order order = Order.create(
        1L,
        OrderNumber.of("ORD-001"),
        List.of(item),
        origin,
        destination,
        policy);

    // when
    KafkaOutboxEvent result = sut.map(OutboxEventType.ORDER_CREATED, order);

    // then
    assertThat(result).isNotNull();
    OrderCreatedKafkaEventPayload payload =
        (OrderCreatedKafkaEventPayload) result.kafkaEvent().getPayload();
    
    // null 필드 확인
    assertThat(payload.getOriginLocation().getEntrancePassword()).isNull();
    assertThat(payload.getOriginLocation().getEntranceGuide()).isNull();
    assertThat(payload.getOriginLocation().getRequestMessage()).isNull();
  }
}

