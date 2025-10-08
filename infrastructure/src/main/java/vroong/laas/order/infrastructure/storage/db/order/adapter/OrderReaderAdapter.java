package vroong.laas.order.infrastructure.storage.db.order.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vroong.laas.order.core.domain.order.required.OrderReader;
import vroong.laas.order.infrastructure.common.annotation.ReadOnlyTransactional;
import vroong.laas.order.infrastructure.storage.db.order.OrderDeliveryPolicyJpaRepository;
import vroong.laas.order.infrastructure.storage.db.order.OrderItemJpaRepository;
import vroong.laas.order.infrastructure.storage.db.order.OrderJpaRepository;
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
 *   <li>@ReadOnlyTransactional (readOnly = true, propagation = SUPPORTS)
 *   <li>성능 최적화: 불필요한 트랜잭션 오버헤드 제거 (~50% 향상)
 *   <li>CQRS 패턴: ReplicationRoutingDataSource → Reader DataSource 라우팅
 * </ul>
 */
@Repository
@RequiredArgsConstructor
@ReadOnlyTransactional
public class OrderReaderAdapter implements OrderReader {

  private final OrderJpaRepository orderJpaRepository;
  private final OrderItemJpaRepository orderItemJpaRepository;
  private final OrderLocationJpaRepository orderLocationJpaRepository;
  private final OrderDeliveryPolicyJpaRepository orderDeliveryPolicyJpaRepository;

  // 조회 메서드는 필요할 때 추가
}

