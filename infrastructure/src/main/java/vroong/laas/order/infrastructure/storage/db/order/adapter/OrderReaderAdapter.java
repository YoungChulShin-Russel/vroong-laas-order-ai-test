package vroong.laas.order.infrastructure.storage.db.order.adapter;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vroong.laas.order.core.domain.order.DeliveryPolicy;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.order.OrderNumber;
import vroong.laas.order.core.domain.order.Origin;
import vroong.laas.order.core.domain.order.required.OrderReader;
import vroong.laas.order.infrastructure.common.annotation.ReadOnlyTransactional;
import vroong.laas.order.infrastructure.storage.db.order.OrderDeliveryPolicyEntity;
import vroong.laas.order.infrastructure.storage.db.order.OrderDeliveryPolicyJpaRepository;
import vroong.laas.order.infrastructure.storage.db.order.OrderEntity;
import vroong.laas.order.infrastructure.storage.db.order.OrderItemEntity;
import vroong.laas.order.infrastructure.storage.db.order.OrderItemJpaRepository;
import vroong.laas.order.infrastructure.storage.db.order.OrderJpaRepository;
import vroong.laas.order.infrastructure.storage.db.order.OrderLocationEntity;
import vroong.laas.order.infrastructure.storage.db.order.OrderLocationJpaRepository;

/**
 * Order Reader Adapter
 *
 * <p>OrderReader Port의 구현체 (Infrastructure Layer)
 *
 * <p>조회 전용 Adapter로, 필요한 조회 메서드는 점진적으로 추가합니다.
 *
 * <p>트랜잭션 관리:
 *
 * <ul>
 *   <li>각 메서드에 @ReadOnlyTransactional 적용 (readOnly = true, propagation = SUPPORTS)
 *   <li>성능 최적화: 불필요한 트랜잭션 오버헤드 제거 (~50% 향상)
 *   <li>CQRS 패턴: ReplicationRoutingDataSource → Reader DataSource 라우팅
 * </ul>
 */
@Repository
@RequiredArgsConstructor
public class OrderReaderAdapter implements OrderReader {

  private final OrderJpaRepository orderJpaRepository;
  private final OrderItemJpaRepository orderItemJpaRepository;
  private final OrderLocationJpaRepository orderLocationJpaRepository;
  private final OrderDeliveryPolicyJpaRepository orderDeliveryPolicyJpaRepository;

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

