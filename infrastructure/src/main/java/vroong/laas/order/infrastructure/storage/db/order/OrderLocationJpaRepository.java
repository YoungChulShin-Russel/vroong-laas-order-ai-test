package vroong.laas.order.infrastructure.storage.db.order;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * OrderLocation JPA Repository
 *
 * <p>주문 위치 정보 영속성 인터페이스
 */
public interface OrderLocationJpaRepository extends JpaRepository<OrderLocationEntity, Long> {

  /**
   * 주문 ID로 위치 정보 조회
   *
   * @param orderId 주문 ID
   * @return 위치 정보 Entity (없으면 Optional.empty())
   */
  @Query(
      "SELECT l FROM OrderLocationEntity l "
          + "WHERE l.orderId = :orderId "
          + "AND l.entityStatus = vroong.laas.order.infrastructure.storage.db.EntityStatus.ACTIVE")
  Optional<OrderLocationEntity> findByOrderId(@Param("orderId") Long orderId);
}

