package vroong.laas.order.core.fixture;

import com.navercorp.fixturemonkey.FixtureMonkey;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import net.jqwik.api.Arbitraries;
import vroong.laas.order.core.domain.order.DeliveryPolicy;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.EntranceInfo;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.order.OrderStatus;
import vroong.laas.order.core.domain.order.Origin;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.Contact;
import vroong.laas.order.core.domain.shared.LatLng;
import vroong.laas.order.core.domain.shared.Money;

/**
 * Fixture Monkey 기반 테스트 Fixture
 *
 * <p>Fixture Monkey의 giveMeBuilder()와 Arbitraries를 활용하여 랜덤 테스트 데이터를 생성합니다.
 */
public class OrderFixtures {

  private final FixtureMonkey fixtureMonkey;

  public OrderFixtures(FixtureMonkey fixtureMonkey) {
    this.fixtureMonkey = fixtureMonkey;
  }

  // ===== Order 생성 =====

  /** 기본 주문 (CREATED 상태) */
  public Order order() {
    return Order.reconstitute(
        null, // id
        generateOrderNumber(),
        OrderStatus.CREATED,
        randomOrderItems(),
        randomOrigin(),
        randomDestination(),
        randomDeliveryPolicy(),
        Instant.now(),
        null, // deliveredAt
        null // cancelledAt
        );
  }

  /** 특정 주문번호로 생성 */
  public Order order(String orderNumber) {
    return Order.reconstitute(
        null,
        orderNumber,
        OrderStatus.CREATED,
        randomOrderItems(),
        randomOrigin(),
        randomDestination(),
        randomDeliveryPolicy(),
        Instant.now(),
        null,
        null);
  }

  /** 배송완료 주문 */
  public Order deliveredOrder() {
    return Order.reconstitute(
        fixtureMonkey.giveMeOne(Long.class),
        generateOrderNumber(),
        OrderStatus.DELIVERED,
        randomOrderItems(),
        randomOrigin(),
        randomDestination(),
        randomDeliveryPolicy(),
        Instant.now().minusSeconds(3600),
        Instant.now(), // deliveredAt
        null);
  }

  /** 취소된 주문 */
  public Order cancelledOrder() {
    return Order.reconstitute(
        fixtureMonkey.giveMeOne(Long.class),
        generateOrderNumber(),
        OrderStatus.CANCELLED,
        randomOrderItems(),
        randomOrigin(),
        randomDestination(),
        randomDeliveryPolicy(),
        Instant.now().minusSeconds(3600),
        null,
        Instant.now() // cancelledAt
        );
  }

  /** 특정 ID로 생성 */
  public Order orderWithId(Long id) {
    return Order.reconstitute(
        id,
        generateOrderNumber(),
        OrderStatus.CREATED,
        randomOrderItems(),
        randomOrigin(),
        randomDestination(),
        randomDeliveryPolicy(),
        Instant.now(),
        null,
        null);
  }

  /** 특정 상태로 생성 */
  public Order orderWithStatus(OrderStatus status) {
    Instant deliveredAt = status == OrderStatus.DELIVERED ? Instant.now() : null;
    Instant cancelledAt = status == OrderStatus.CANCELLED ? Instant.now() : null;

    return Order.reconstitute(
        null,
        generateOrderNumber(),
        status,
        randomOrderItems(),
        randomOrigin(),
        randomDestination(),
        randomDeliveryPolicy(),
        Instant.now().minusSeconds(3600),
        deliveredAt,
        cancelledAt);
  }

  // ===== Value Objects 랜덤 생성 (Fixture Monkey 활용) =====

  /** 랜덤 OrderItem 리스트 */
  public List<OrderItem> randomOrderItems() {
    int count = Math.abs(fixtureMonkey.giveMeOne(Integer.class) % 3) + 1; // 1~3개
    return List.of(
        randomOrderItem(),
        randomOrderItem(),
        randomOrderItem()
    ).subList(0, count);
  }

  /** 랜덤 OrderItem (Fixture Monkey 사용) */
  public OrderItem randomOrderItem() {
    String itemName = "상품" + Math.abs(fixtureMonkey.giveMeOne(Integer.class) % 1000);
    int quantity = Arbitraries.integers().between(1, 10).sample();
    Money price = new Money(BigDecimal.valueOf(Arbitraries.longs().between(1000L, 100000L).sample()));
    String category = Arbitraries.of("식품", "생활용품", "의류", "전자제품", "도서").sample();
    
    return new OrderItem(itemName, quantity, price, category, null, null);
  }

  /** 랜덤 Origin */
  public Origin randomOrigin() {
    return new Origin(randomContact(), randomAddress(), randomLatLng(), randomEntranceInfo());
  }

  /** 랜덤 Destination */
  public Destination randomDestination() {
    return new Destination(randomContact(), randomAddress(), randomLatLng(), randomEntranceInfo());
  }

  /** 랜덤 DeliveryPolicy */
  public DeliveryPolicy randomDeliveryPolicy() {
    return new DeliveryPolicy(false, true, false, null, Instant.now());
  }

  /** 랜덤 Contact (Fixture Monkey 사용) */
  public Contact randomContact() {
    return fixtureMonkey
        .giveMeBuilder(Contact.class)
        .set("name", "테스터" + fixtureMonkey.giveMeOne(Integer.class))
        .set(
            "phoneNumber",
            "010-"
                + Arbitraries.strings().numeric().ofLength(4).sample()
                + "-"
                + Arbitraries.strings().numeric().ofLength(4).sample())
        .sample();
  }

  /** 랜덤 Address (Fixture Monkey 사용) */
  public Address randomAddress() {
    return fixtureMonkey
        .giveMeBuilder(Address.class)
        .set(
            "jibnunAddress",
            "서울시 " + Arbitraries.of("강남구", "서초구", "송파구", "마포구", "용산구").sample())
        .set(
            "roadAddress",
            Arbitraries.of("테헤란로", "강남대로", "서초대로", "올림픽로", "한강대로").sample()
                + " "
                + fixtureMonkey.giveMeOne(Integer.class))
        .set(
            "detailAddress",
            Arbitraries.of("A동", "B동", "C동").sample()
                + " "
                + fixtureMonkey.giveMeOne(Integer.class)
                + "호")
        .sample();
  }

  /** 랜덤 LatLng (Fixture Monkey 사용) */
  public LatLng randomLatLng() {
    return fixtureMonkey
        .giveMeBuilder(LatLng.class)
        .set("latitude", Arbitraries.doubles().between(37.0, 38.0).map(BigDecimal::valueOf))
        .set("longitude", Arbitraries.doubles().between(126.0, 127.0).map(BigDecimal::valueOf))
        .sample();
  }

  /** 랜덤 EntranceInfo (Fixture Monkey 사용) */
  public EntranceInfo randomEntranceInfo() {
    return fixtureMonkey
        .giveMeBuilder(EntranceInfo.class)
        .set("password", Arbitraries.strings().numeric().ofLength(4))
        .set("guide", Arbitraries.of("경비실 통과", "정문 이용", "후문 이용", "엘리베이터 이용"))
        .set("requestMessage", Arbitraries.of("빠른 배송 부탁드려요", "문앞에 두세요", "경비실에 맡겨주세요", "벨 눌러주세요"))
        .sample();
  }

  // ===== 헬퍼 메서드 =====

  private String generateOrderNumber() {
    return "ORD-" + System.currentTimeMillis();
  }
}
