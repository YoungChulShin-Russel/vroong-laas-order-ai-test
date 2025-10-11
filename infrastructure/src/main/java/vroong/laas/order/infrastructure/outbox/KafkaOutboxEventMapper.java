package vroong.laas.order.infrastructure.outbox;

import com.vroong.msa.kafka.event.KafkaEvent;
import com.vroong.msa.kafka.event.KafkaEventPayload;
import com.vroong.msa.kafka.event.KafkaEventSource;
import com.vroong.msa.kafka.event.KafkaEventType;
import com.vroong.msa.kafka.event.payload.order.OrderCreatedKafkaEventPayload;
import java.util.List;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.outbox.OutboxEventType;
import vroong.laas.order.core.domain.shared.AggregateRoot;

class KafkaOutboxEventMapper {

  private final KafkaEventSource ORDER_EVENT_SOURCE = KafkaEventSource.ORDER;

  public KafkaOutboxEvent map(OutboxEventType eventType, AggregateRoot aggregateRoot) {
    if (eventType == OutboxEventType.ORDER_CREATED) {
      Order order = (Order) aggregateRoot;
      return mapToOrderCreatedEvent(order);
    }

    throw new IllegalArgumentException("지원하지 않는 이벤트 타입입니다: " + eventType);
  }

  private KafkaOutboxEvent mapToOrderCreatedEvent(Order order) {
    // 1. Items 매핑
    List<OrderCreatedKafkaEventPayload.OrderCreatedOrderItem> items =
        order.getItems().stream()
            .map(
                item ->
                    OrderCreatedKafkaEventPayload.OrderCreatedOrderItem.builder()
                        .itemName(item.itemName())
                        .quantity(item.quantity())
                        .price(item.price().amount())
                        .category(item.category())
                        .weight(item.weight() != null ? item.weight().value() : null)
                        .volumeLength(item.volume() != null ? item.volume().length() : null)
                        .volumeWidth(item.volume() != null ? item.volume().width() : null)
                        .volumeHeight(item.volume() != null ? item.volume().height() : null)
                        .volumeCbm(item.volume() != null ? item.volume().cbm() : null)
                        .build())
            .toList();

    // 2. Origin Location 매핑
    OrderCreatedKafkaEventPayload.OrderCreatedOrderLocation originLocation =
        OrderCreatedKafkaEventPayload.OrderCreatedOrderLocation.builder()
            .contactName(order.getOrigin().contact().name())
            .contactPhoneNumber(order.getOrigin().contact().phoneNumber())
            .entrancePassword(order.getOrigin().entranceInfo().password())
            .entranceGuide(order.getOrigin().entranceInfo().guide())
            .requestMessage(order.getOrigin().entranceInfo().requestMessage())
            .latitude(order.getOrigin().latLng().latitude())
            .longitude(order.getOrigin().latLng().longitude())
            .jibunAddress(order.getOrigin().address().jibnunAddress())
            .roadAddress(order.getOrigin().address().roadAddress())
            .detailAddress(order.getOrigin().address().detailAddress())
            .build();

    // 3. Destination Location 매핑
    OrderCreatedKafkaEventPayload.OrderCreatedOrderLocation destinationLocation =
        OrderCreatedKafkaEventPayload.OrderCreatedOrderLocation.builder()
            .contactName(order.getDestination().contact().name())
            .contactPhoneNumber(order.getDestination().contact().phoneNumber())
            .entrancePassword(order.getDestination().entranceInfo().password())
            .entranceGuide(order.getDestination().entranceInfo().guide())
            .requestMessage(order.getDestination().entranceInfo().requestMessage())
            .latitude(order.getDestination().latLng().latitude())
            .longitude(order.getDestination().latLng().longitude())
            .jibunAddress(order.getDestination().address().jibnunAddress())
            .roadAddress(order.getDestination().address().roadAddress())
            .detailAddress(order.getDestination().address().detailAddress())
            .build();

    // 4. Delivery Policy 매핑
    OrderCreatedKafkaEventPayload.OrderCreatedOrderDeliveryPolicy deliveryPolicy =
        OrderCreatedKafkaEventPayload.OrderCreatedOrderDeliveryPolicy.builder()
            .isContactless(order.getDeliveryPolicy().contactlessDelivery())
            .hasLiquor(order.getDeliveryPolicy().alcoholDelivery())
            .requestedStartAt(order.getDeliveryPolicy().reservedDeliveryStartTime())
            .requestedPickupAt(order.getDeliveryPolicy().pickupRequestTime())
            .build();

    // 5. Payload 생성
    OrderCreatedKafkaEventPayload payload =
        OrderCreatedKafkaEventPayload.builder()
            .orderId(order.getId())
            .orderNumber(order.getOrderNumber().value())
            .orderStatus(order.getStatus().name())
            .items(items)
            .originLocation(originLocation)
            .destinationLocation(destinationLocation)
            .deliveryPolicy(deliveryPolicy)
            .orderedAt(order.getOrderedAt())
            .build();

    // 6. KafkaEvent 생성
    KafkaEvent<KafkaEventPayload> kafkaEvent = KafkaEvent.of(
        KafkaEventType.ORDER_ORDER_CREATED,
        ORDER_EVENT_SOURCE,
        payload);

    // 7. KafkaOutboxEvent 반환
    return new KafkaOutboxEvent(String.valueOf(order.getId()), kafkaEvent);
  }
}
