package vroong.laas.order.infrastructure.storage.db.order;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * OrderDeliveryPolicy JPA Repository
 *
 * <p>주문 배송 정책 영속성 인터페이스
 */
public interface OrderDeliveryPolicyJpaRepository
    extends JpaRepository<OrderDeliveryPolicyEntity, Long> {

  /**
   * 주문 ID로 배송 정책 조회
   *
   * @param orderId 주문 ID
   * @return 배송 정책 Entity (없으면 Optional.empty())
   */
  @Query(
      "SELECT p FROM OrderDeliveryPolicyEntity p "
          + "WHERE p.orderId = :orderId "
          + "AND p.entityStatus = vroong.laas.order.infrastructure.storage.db.EntityStatus.ACTIVE")
  Optional<OrderDeliveryPolicyEntity> findByOrderId(@Param("orderId") Long orderId);

  /**
   * 주문 ID로 배송 정책 삭제 (soft delete)
   *
   * @param orderId 주문 ID
   */
  @Query(
      "UPDATE OrderDeliveryPolicyEntity p "
          + "SET p.entityStatus = vroong.laas.order.infrastructure.storage.db.EntityStatus.DELETED "
          + "WHERE p.orderId = :orderId")
  void deleteByOrderId(@Param("orderId") Long orderId);
}

