package vroong.laas.order.infrastructure.storage.db.order;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Order JPA Repository
 *
 * <p>Spring Data JPA를 사용한 주문 영속성 인터페이스
 */
public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

  /**
   * 주문번호로 주문 조회
   *
   * @param orderNumber 주문번호
   * @return 주문 Entity (없으면 Optional.empty())
   */
  Optional<OrderEntity> findByOrderNumber(String orderNumber);

  /**
   * 주문번호 존재 여부 확인
   *
   * @param orderNumber 주문번호
   * @return 존재하면 true, 아니면 false
   */
  boolean existsByOrderNumber(String orderNumber);

  /**
   * 주문과 연관 Entity를 함께 조회
   *
   * <p>N+1 문제 방지를 위해 JOIN FETCH 사용
   *
   * @param id 주문 ID
   * @return 주문 Entity (없으면 Optional.empty())
   */
  @Query(
      "SELECT o FROM OrderEntity o "
          + "WHERE o.id = :id "
          + "AND o.entityStatus = vroong.laas.order.infrastructure.storage.db.EntityStatus.ACTIVE")
  Optional<OrderEntity> findByIdWithDetails(@Param("id") Long id);
}

