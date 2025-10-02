package vroong.laas.order.core.fixture;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import vroong.laas.order.core.domain.order.DeliveryPolicy;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.EntranceInfo;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.order.Origin;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.Contact;
import vroong.laas.order.core.domain.shared.LatLng;
import vroong.laas.order.core.domain.shared.Money;
import vroong.laas.order.core.domain.shared.Volume;
import vroong.laas.order.core.domain.shared.Weight;

public class OrderFixture {

  public static Order createOrder() {
    return Order.create(
        "ORD-20251002-001",
        createOrderItems(),
        createOrigin(),
        createDestination(),
        createDeliveryPolicy());
  }

  public static Order createOrder(String orderNumber) {
    return Order.create(
        orderNumber, createOrderItems(), createOrigin(), createDestination(), createDeliveryPolicy());
  }

  public static Order createOrder(List<OrderItem> items) {
    return Order.create(
        "ORD-20251002-001", items, createOrigin(), createDestination(), createDeliveryPolicy());
  }

  public static List<OrderItem> createOrderItems() {
    return List.of(
        createOrderItem("상품1", 2, 10_000, "식품"),
        createOrderItem("상품2", 3, 5_000, "생활용품"));
  }

  public static OrderItem createOrderItem(
      String itemName, int quantity, long price, String category) {
    return new OrderItem(
        itemName, quantity, new Money(BigDecimal.valueOf(price)), category, null, null);
  }

  public static OrderItem createOrderItemWithWeight(
      String itemName, int quantity, long price, String category, double weight) {
    return new OrderItem(
        itemName,
        quantity,
        new Money(BigDecimal.valueOf(price)),
        category,
        new Weight(BigDecimal.valueOf(weight)),
        null);
  }

  public static OrderItem createOrderItemWithVolume(
      String itemName,
      int quantity,
      long price,
      String category,
      double length,
      double width,
      double height) {
    return new OrderItem(
        itemName,
        quantity,
        new Money(BigDecimal.valueOf(price)),
        category,
        null,
        new Volume(
            BigDecimal.valueOf(length), BigDecimal.valueOf(width), BigDecimal.valueOf(height)));
  }

  public static Origin createOrigin() {
    return new Origin(
        createContact("홍길동", "010-1234-5678"),
        createAddress("서울시 강남구", "테헤란로 123", "A동 101호"),
        createLatLng(37.123456, 127.123456),
        createEntranceInfo("1234", "경비실 통과", "문앞에 두세요"));
  }

  public static Destination createDestination() {
    return new Destination(
        createContact("김철수", "010-9876-5432"),
        createAddress("서울시 서초구", "서초대로 456", "B동 202호"),
        createLatLng(37.654321, 127.654321),
        createEntranceInfo("5678", "정문 이용", "빠른 배송 부탁드려요"));
  }

  public static Contact createContact(String name, String phoneNumber) {
    return new Contact(name, phoneNumber);
  }

  public static Address createAddress(
      String jibnunAddress, String roadAddress, String detailAddress) {
    return new Address(jibnunAddress, roadAddress, detailAddress);
  }

  public static LatLng createLatLng(double latitude, double longitude) {
    return new LatLng(BigDecimal.valueOf(latitude), BigDecimal.valueOf(longitude));
  }

  public static EntranceInfo createEntranceInfo(
      String password, String guide, String requestMessage) {
    return new EntranceInfo(password, guide, requestMessage);
  }

  public static DeliveryPolicy createDeliveryPolicy() {
    return new DeliveryPolicy(false, true, false, null, Instant.now());
  }

  public static DeliveryPolicy createImmediateDeliveryPolicy() {
    return new DeliveryPolicy(false, true, false, null, Instant.now());
  }

  public static DeliveryPolicy createReservedDeliveryPolicy(Instant reservedTime) {
    return new DeliveryPolicy(false, true, true, reservedTime, Instant.now());
  }

  public static DeliveryPolicy createAlcoholDeliveryPolicy() {
    return new DeliveryPolicy(true, false, false, null, Instant.now());
  }
}

