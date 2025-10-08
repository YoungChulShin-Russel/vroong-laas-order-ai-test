package vroong.laas.order.infrastructure.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read Only 조회용 트랜잭션 애노테이션
 * 
 * <p><strong>특징:</strong>
 * <ul>
 *   <li>readOnly = true: JDBC Connection에 readOnly 힌트 전달</li>
 *   <li>propagation = SUPPORTS: 기존 트랜잭션이 있으면 참여, 없으면 Non-Transactional</li>
 * </ul>
 * 
 * <p><strong>성능 최적화:</strong>
 * <ul>
 *   <li>트랜잭션이 없는 경우: SET autocommit 오버헤드 제거 (~50% 성능 향상)</li>
 *   <li>CQRS 패턴: ReplicationRoutingDataSource → Reader DataSource 라우팅</li>
 *   <li>실제 트랜잭션은 JpaRepository에서만 시작 (트랜잭션 경계 최소화)</li>
 * </ul>
 * 
 * <p><strong>사용 위치:</strong>
 * <ul>
 *   <li>OrderReaderAdapter - 모든 조회 메서드</li>
 *   <li>OrderStoreAdapter - 조회 메서드 (findById, findByIdWithItems 등)</li>
 *   <li>@DataJpaTest 제외 (테스트는 기본 @Transactional 사용)</li>
 * </ul>
 * 
 * <p><strong>예시:</strong>
 * <pre>
 * {@code
 * @Repository
 * @RequiredArgsConstructor
 * public class OrderReaderAdapter implements OrderReader {
 *     
 *     @ReadOnlyTransactional
 *     @Override
 *     public Optional<Order> findById(Long id) {
 *         return orderJpaRepository.findById(id)
 *             .map(OrderJpaEntity::toDomain);
 *     }
 * }
 * }
 * </pre>
 * 
 * <p><strong>트랜잭션 흐름:</strong>
 * <pre>
 * Adapter.findById() 시작
 *   ↓
 * [@ReadOnlyTransactional - SUPPORTS]
 *   ↓ (Non-Transactional - 트랜잭션 시작 안 함)
 *   ↓
 * JpaRepository.findById()
 *   ↓
 * [@Transactional(REQUIRED) - 트랜잭션 시작!] ⭐
 *   ↓
 * em.find() 실행
 *   ↓
 * [트랜잭션 종료] ⭐
 *   ↓
 * toDomain() (Non-Transactional)
 *   ↓
 * Adapter.findById() 종료
 * </pre>
 * 
 * @see Propagation#SUPPORTS
 * @see org.springframework.transaction.annotation.Transactional
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public @interface ReadOnlyTransactional {
}
