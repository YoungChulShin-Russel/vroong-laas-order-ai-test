package vroong.laas.order.infrastructure.storage.db.order.adapter;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.order.core.domain.order.DeliveryPolicy;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.order.OrderNumber;
import vroong.laas.order.core.domain.order.Origin;
import vroong.laas.order.core.domain.order.required.OrderRepository;
import vroong.laas.order.core.common.annotation.ReadOnlyTransactional;
import vroong.laas.order.infrastructure.storage.db.order.OrderDeliveryPolicyEntity;
import vroong.laas.order.infrastructure.storage.db.order.OrderDeliveryPolicyJpaRepository;
import vroong.laas.order.infrastructure.storage.db.order.OrderEntity;
import vroong.laas.order.infrastructure.storage.db.order.OrderItemEntity;
import vroong.laas.order.infrastructure.storage.db.order.OrderItemJpaRepository;
import vroong.laas.order.infrastructure.storage.db.order.OrderJpaRepository;
import vroong.laas.order.infrastructure.storage.db.order.OrderLocationEntity;
import vroong.laas.order.infrastructure.storage.db.order.OrderLocationJpaRepository;

/**
 * Order Repository Adapter
 *
 * <p>OrderRepository Port의 구현체 (Infrastructure Layer)
 *
 * <p>OrderStore와 OrderReader를 통합한 Adapter입니다.
 *
 * <p>트랜잭션 관리:
 *
 * <ul>
 *   <li>store(), delete(): @Transactional (쓰기)
 *   <li>findById(), findByOrderNumber(), existsByOrderNumber(): @ReadOnlyTransactional (읽기)
 * </ul>
 */
@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

  private final OrderJpaRepository orderJpaRepository;
  private final OrderItemJpaRepository orderItemJpaRepository;
  private final OrderLocationJpaRepository orderLocationJpaRepository;
  private final OrderDeliveryPolicyJpaRepository orderDeliveryPolicyJpaRepository;

  // === 저장 ===

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

  @Transactional
  @Override
  public void delete(Order order) {
    if (order.getId() == null) {
      throw new IllegalArgumentException("삭제할 주문은 ID가 있어야 합니다");
    }

    Long orderId = order.getId();

    // 1. 연관 Entity 삭제
    orderItemJpaRepository.deleteByOrderId(orderId);
    orderLocationJpaRepository.deleteByOrderId(orderId);
    orderDeliveryPolicyJpaRepository.deleteByOrderId(orderId);

    // 2. OrderEntity 삭제
    orderJpaRepository.deleteById(orderId);
  }

  // === 조회 ===

  @ReadOnlyTransactional
  @Override
  public Optional<Order> findById(Long orderId) {
    return orderJpaRepository.findById(orderId).map(this::toDomainWithDetails);
  }

  @ReadOnlyTransactional
  @Override
  public Optional<Order> findByOrderNumber(OrderNumber orderNumber) {
    // OrderNumber → String 변환 후 JPA Repository 호출
    return orderJpaRepository
        .findByOrderNumber(orderNumber.value())
        .map(this::toDomainWithDetails);
  }

  @ReadOnlyTransactional
  @Override
  public boolean existsByOrderNumber(OrderNumber orderNumber) {
    return orderJpaRepository.existsByOrderNumber(orderNumber.value());
  }

  // === Private Helper Methods ===

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

  /**
   * OrderEntity를 연관 데이터와 함께 Domain Order로 변환
   *
   * @param orderEntity OrderEntity
   * @return Domain Order
   */
  private Order toDomainWithDetails(OrderEntity orderEntity) {
    Long orderId = orderEntity.getId();

    // 1. OrderItem 조회 및 변환
    List<OrderItem> items =
        orderItemJpaRepository.findByOrderId(orderId).stream()
            .map(OrderItemEntity::toDomain)
            .toList();

    // 2. OrderLocation 조회 및 변환
    OrderLocationEntity locationEntity =
        orderLocationJpaRepository
            .findByOrderId(orderId)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "OrderLocation이 없습니다. orderId: " + orderId));

    Origin origin = locationEntity.toOriginDomain();
    Destination destination = locationEntity.toDestinationDomain();

    // 3. DeliveryPolicy 조회 및 변환
    OrderDeliveryPolicyEntity policyEntity =
        orderDeliveryPolicyJpaRepository
            .findByOrderId(orderId)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "OrderDeliveryPolicy가 없습니다. orderId: " + orderId));

    DeliveryPolicy policy = policyEntity.toDomain();

    // 4. OrderEntity → Domain Order 변환
    return orderEntity.toDomain(items, origin, destination, policy);
  }
}

