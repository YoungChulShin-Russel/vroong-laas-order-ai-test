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
   * ID로 주문과 연관 Entity를 함께 조회 (JOIN FETCH)
   *
   * <p>성능 최적화: 1개의 쿼리로 모든 연관 데이터 조회 (4쿼리 → 1쿼리)
   *
   * <p>조회 데이터:
   * - OrderEntity
   * - OrderItemEntity (1:N)
   * - OrderLocationEntity (1:1)
   * - OrderDeliveryPolicyEntity (1:1)
   *
   * @param id 주문 ID
   * @return 주문 Entity (없으면 Optional.empty())
   */
  @Query(
      "SELECT DISTINCT o FROM OrderEntity o "
          + "LEFT JOIN FETCH o.items "
          + "LEFT JOIN FETCH o.location "
          + "LEFT JOIN FETCH o.deliveryPolicy "
          + "WHERE o.id = :id "
          + "AND o.entityStatus = vroong.laas.order.infrastructure.storage.db.EntityStatus.ACTIVE")
  Optional<OrderEntity> findByIdWithDetails(@Param("id") Long id);

  /**
   * 주문번호로 주문과 연관 Entity를 함께 조회 (JOIN FETCH)
   *
   * <p>성능 최적화: 1개의 쿼리로 모든 연관 데이터 조회 (4쿼리 → 1쿼리)
   *
   * @param orderNumber 주문번호
   * @return 주문 Entity (없으면 Optional.empty())
   */
  @Query(
      "SELECT DISTINCT o FROM OrderEntity o "
          + "LEFT JOIN FETCH o.items "
          + "LEFT JOIN FETCH o.location "
          + "LEFT JOIN FETCH o.deliveryPolicy "
          + "WHERE o.orderNumber = :orderNumber "
          + "AND o.entityStatus = vroong.laas.order.infrastructure.storage.db.EntityStatus.ACTIVE")
  Optional<OrderEntity> findByOrderNumberWithDetails(@Param("orderNumber") String orderNumber);
}

