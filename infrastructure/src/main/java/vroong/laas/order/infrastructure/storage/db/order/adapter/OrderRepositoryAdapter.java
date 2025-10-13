package vroong.laas.order.infrastructure.storage.db.order.adapter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vroong.laas.order.core.domain.order.DeliveryPolicy;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.EntranceInfo;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.order.OrderNumber;
import vroong.laas.order.core.domain.order.Origin;
import vroong.laas.order.core.domain.order.required.OrderRepository;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.LatLng;
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
 * - Domain Service에서 관리 (OrderCreator, OrderLocationChanger, OrderReader)
 * - Adapter는 단순히 영속성 작업만 수행
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

  @Override
  public Optional<Order> findById(Long orderId) {
    return orderJpaRepository.findById(orderId).map(this::toDomainWithDetails);
  }

  @Override
  public Optional<Order> findByOrderNumber(OrderNumber orderNumber) {
    return orderJpaRepository
        .findByOrderNumber(orderNumber.value())
        .map(this::toDomainWithDetails);
  }

  @Override
  public boolean existsByOrderNumber(OrderNumber orderNumber) {
    return orderJpaRepository.existsByOrderNumber(orderNumber.value());
  }

  // === 업데이트 ===

  /**
   * 도착지 주소 업데이트
   *
   * <p>OrderLocationEntity의 Destination 주소 필드만 업데이트 (Contact는 유지)
   *
   * <p>변경 범위:
   * - Address (주소)
   * - LatLng (위경도)
   * - EntranceInfo (출입 가이드)
   *
   * <p>유지되는 것:
   * - Contact (연락처) - 변경되지 않음
   *
   * @param orderId 주문 ID
   * @param newAddress 새로운 주소
   * @param newLatLng 새로운 위경도
   * @param newEntranceInfo 새로운 출입 정보
   */
  @Override
  public void updateDestinationAddress(
      Long orderId, Address newAddress, LatLng newLatLng, EntranceInfo newEntranceInfo) {
    // 1. OrderLocationEntity 조회
    OrderLocationEntity locationEntity =
        orderLocationJpaRepository
            .findByOrderId(orderId)
            .orElseThrow(
                () ->
                    new IllegalStateException("OrderLocation이 없습니다. orderId: " + orderId));

    // 2. Destination 주소 필드만 업데이트 (Contact 유지)
    locationEntity.updateDestinationAddress(newAddress, newLatLng, newEntranceInfo);

    // 3. 명시적으로 저장
    orderLocationJpaRepository.save(locationEntity);
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
