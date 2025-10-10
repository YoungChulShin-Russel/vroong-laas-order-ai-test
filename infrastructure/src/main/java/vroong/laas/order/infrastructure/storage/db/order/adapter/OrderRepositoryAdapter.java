package vroong.laas.order.infrastructure.storage.db.order.adapter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.order.core.common.annotation.ReadOnlyTransactional;
import vroong.laas.order.core.domain.order.DeliveryPolicy;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.order.OrderNumber;
import vroong.laas.order.core.domain.order.Origin;
import vroong.laas.order.core.domain.order.required.OrderRepository;
import vroong.laas.order.infrastructure.storage.db.order.OrderDeliveryPolicyEntity;
import vroong.laas.order.infrastructure.storage.db.order.OrderDeliveryPolicyJpaRepository;
import vroong.laas.order.infrastructure.storage.db.order.OrderEntity;
import vroong.laas.order.infrastructure.storage.db.order.OrderItemEntity;
import vroong.laas.order.infrastructure.storage.db.order.OrderItemJpaRepository;
import vroong.laas.order.infrastructure.storage.db.order.OrderJpaRepository;
import vroong.laas.order.infrastructure.storage.db.order.OrderLocationEntity;
import vroong.laas.order.infrastructure.storage.db.order.OrderLocationJpaRepository;
import vroong.laas.order.infrastructure.storage.db.order.OrderStatus;

/**
 * Order Repository Adapter
 *
 * <p>OrderRepository Port의 구현체 (Infrastructure Layer)
 *
 * <p>책임:
 * - Order Entity 생성 및 저장
 * - Order 조회
 *
 * <p>트랜잭션 관리:
 *
 * <ul>
 *   <li>store(): @Transactional (쓰기)
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

  /**
   * Order 생성 및 저장
   *
   * @param orderNumber 주문번호
   * @param items 주문 아이템 목록
   * @param origin 출발지
   * @param destination 도착지
   * @param deliveryPolicy 배송 정책
   * @return 저장된 Order (id 할당됨, 도메인 이벤트 포함)
   */
  @Transactional
  @Override
  public Order store(
      OrderNumber orderNumber,
      List<OrderItem> items,
      Origin origin,
      Destination destination,
      DeliveryPolicy deliveryPolicy) {

    // 1. OrderEntity 생성 및 저장
    OrderEntity orderEntity =
        OrderEntity.builder()
            .orderNumber(orderNumber.value())
            .status(OrderStatus.CREATED)
            .orderedAt(Instant.now())
            .build();
    OrderEntity savedOrderEntity = orderJpaRepository.save(orderEntity);
    Long orderId = savedOrderEntity.getId();

    // 2. 연관 Entity 저장
    saveOrderItems(items, orderId);
    saveOrderLocation(origin, destination, orderId);
    saveOrderDeliveryPolicy(deliveryPolicy, orderId);

    // 3. Order.create() 호출 (도메인 이벤트 자동 추가)
    return Order.create(orderId, orderNumber, items, origin, destination, deliveryPolicy);
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

  private void saveOrderItems(List<OrderItem> items, Long orderId) {
    List<OrderItemEntity> itemEntities =
        items.stream().map(item -> OrderItemEntity.from(item, orderId)).toList();

    orderItemJpaRepository.saveAll(itemEntities);
  }

  private void saveOrderLocation(Origin origin, Destination destination, Long orderId) {
    OrderLocationEntity locationEntity = OrderLocationEntity.from(origin, destination, orderId);
    orderLocationJpaRepository.save(locationEntity);
  }

  private void saveOrderDeliveryPolicy(DeliveryPolicy deliveryPolicy, Long orderId) {
    OrderDeliveryPolicyEntity policyEntity =
        OrderDeliveryPolicyEntity.from(deliveryPolicy, orderId);
    orderDeliveryPolicyJpaRepository.save(policyEntity);
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
                    new IllegalStateException("OrderLocation이 없습니다. orderId: " + orderId));

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
