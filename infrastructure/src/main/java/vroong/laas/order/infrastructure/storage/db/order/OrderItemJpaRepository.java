package vroong.laas.order.infrastructure.storage.db.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * OrderItem JPA Repository
 *
 * <p>주문 아이템 영속성 인터페이스
 */
public interface OrderItemJpaRepository extends JpaRepository<OrderItemEntity, Long> {

  /**
   * 주문 ID로 주문 아이템 목록 조회
   *
   * @param orderId 주문 ID
   * @return 주문 아이템 목록
   */
  @Query(
      "SELECT i FROM OrderItemEntity i "
          + "WHERE i.orderId = :orderId "
          + "AND i.entityStatus = vroong.laas.order.infrastructure.storage.db.EntityStatus.ACTIVE")
  List<OrderItemEntity> findByOrderId(@Param("orderId") Long orderId);

  /**
   * 주문 ID로 주문 아이템 삭제 (soft delete)
   *
   * @param orderId 주문 ID
   */
  @Query(
      "UPDATE OrderItemEntity i "
          + "SET i.entityStatus = vroong.laas.order.infrastructure.storage.db.EntityStatus.DELETED "
          + "WHERE i.orderId = :orderId")
  void deleteByOrderId(@Param("orderId") Long orderId);
}

