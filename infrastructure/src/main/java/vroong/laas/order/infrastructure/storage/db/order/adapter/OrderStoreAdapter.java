package vroong.laas.order.infrastructure.storage.db.order.adapter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.order.core.domain.order.DeliveryPolicy;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.order.Origin;
import vroong.laas.order.core.domain.order.required.OrderStore;
import vroong.laas.order.infrastructure.storage.db.order.OrderDeliveryPolicyEntity;
import vroong.laas.order.infrastructure.storage.db.order.OrderDeliveryPolicyJpaRepository;
import vroong.laas.order.infrastructure.storage.db.order.OrderEntity;
import vroong.laas.order.infrastructure.storage.db.order.OrderItemEntity;
import vroong.laas.order.infrastructure.storage.db.order.OrderItemJpaRepository;
import vroong.laas.order.infrastructure.storage.db.order.OrderJpaRepository;
import vroong.laas.order.infrastructure.storage.db.order.OrderLocationEntity;
import vroong.laas.order.infrastructure.storage.db.order.OrderLocationJpaRepository;

/**
 * Order Store Adapter
 *
 * <p>OrderStore Port의 구현체 (Infrastructure Layer)
 *
 * <p>트랜잭션 관리:
 *
 * <ul>
 *   <li>store() 메서드에 @Transactional 적용
 *   <li>신규 Order와 연관 Entity들을 하나의 트랜잭션으로 저장
 * </ul>
 */
@Repository
@RequiredArgsConstructor
public class OrderStoreAdapter implements OrderStore {

  private final OrderJpaRepository orderJpaRepository;
  private final OrderItemJpaRepository orderItemJpaRepository;
  private final OrderLocationJpaRepository orderLocationJpaRepository;
  private final OrderDeliveryPolicyJpaRepository orderDeliveryPolicyJpaRepository;

  @Transactional
  @Override
  public Order store(Order order) {
    // 1. 신규 주문만 저장 (ID 없어야 함)
    if (order.getId() != null) {
      throw new IllegalArgumentException("신규 주문은 ID가 없어야 합니다: " + order.getId());
    }

    // 2. OrderEntity 저장
    OrderEntity orderEntity = OrderEntity.from(order);
    OrderEntity savedOrderEntity = orderJpaRepository.save(orderEntity);
    Long orderId = savedOrderEntity.getId();

    // 3. 연관 Entity 저장
    saveOrderItems(order, orderId);
    OrderLocationEntity locationEntity = saveOrderLocation(order, orderId);
    OrderDeliveryPolicyEntity policyEntity = saveOrderDeliveryPolicy(order, orderId);

    // 4. 연관 Entity를 Domain으로 변환
    List<OrderItemEntity> itemEntities = orderItemJpaRepository.findByOrderId(orderId);
    List<OrderItem> items = itemEntities.stream().map(OrderItemEntity::toDomain).toList();

    Origin origin = locationEntity.toOriginDomain();
    Destination destination = locationEntity.toDestinationDomain();
    DeliveryPolicy policy = policyEntity.toDomain();

    // 5. OrderEntity를 Domain으로 변환
    return savedOrderEntity.toDomain(items, origin, destination, policy);
  }

  private void saveOrderItems(Order order, Long orderId) {
    List<OrderItemEntity> itemEntities =
        order.getItems().stream().map(item -> OrderItemEntity.from(item, orderId)).toList();

    orderItemJpaRepository.saveAll(itemEntities);
  }

  private OrderLocationEntity saveOrderLocation(Order order, Long orderId) {
    OrderLocationEntity locationEntity =
        OrderLocationEntity.from(order.getOrigin(), order.getDestination(), orderId);

    return orderLocationJpaRepository.save(locationEntity);
  }

  private OrderDeliveryPolicyEntity saveOrderDeliveryPolicy(Order order, Long orderId) {
    OrderDeliveryPolicyEntity policyEntity =
        OrderDeliveryPolicyEntity.from(order.getDeliveryPolicy(), orderId);

    return orderDeliveryPolicyJpaRepository.save(policyEntity);
  }
}

