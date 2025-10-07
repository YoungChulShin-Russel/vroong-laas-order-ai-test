package vroong.laas.order.infrastructure.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Read/Write DataSource 자동 라우팅
 * 
 * <p><strong>전략: @Transactional 강제 + Default READ</strong>
 * 
 * <p>라우팅 규칙:
 * <ul>
 *   <li>@Transactional → WRITE DataSource (쓰기 작업)</li>
 *   <li>@Transactional(readOnly = true) → READ DataSource (읽기 전용)</li>
 *   <li>@Transactional 없음 → READ DataSource (Default, 조회 최적화)</li>
 * </ul>
 * 
 * <p><strong>장점:</strong>
 * <ul>
 *   <li>조회 시 @Transactional(readOnly=true) 생략 가능 → 자동으로 READ Pool 사용</li>
 *   <li>저장 시 @Transactional 누락 시 즉시 에러 (read-only 에러 발생)</li>
 *   <li>Writer Pool 보호 (읽기 요청이 Writer Pool 점유 방지)</li>
 *   <li>Reader Pool 효율적 활용 (읽기 90% 트래픽에 최적화)</li>
 * </ul>
 * 
 * <p><strong>주의:</strong> LazyConnectionDataSourceProxy와 함께 사용해야
 * @Transactional 설정이 Connection 획득 전에 적용됩니다.
 */
@Slf4j
public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {
  
  /**
   * Database 타입 (타입 안전성)
   */
  public enum DatabaseType {
    WRITE,  // 쓰기 전용 (Writer Endpoint)
    READ    // 읽기 전용 (Reader Endpoint)
  }
  
  /**
   * 현재 스레드의 트랜잭션 상태에 따라 DataSource 결정
   * 
   * <p>판단 로직:
   * <ol>
   *   <li>Transaction이 활성화되어 있고 readOnly가 아니면 → WRITE</li>
   *   <li>나머지 모든 경우 → READ (Default)</li>
   * </ol>
   * 
   * <p>이렇게 하면:
   * <ul>
   *   <li>@Transactional 없는 조회 → READ (자동 최적화)</li>
   *   <li>@Transactional 없는 저장 → READ (에러 발생, 강제 감지)</li>
   *   <li>@Transactional(readOnly=true) → READ (명시적 읽기)</li>
   *   <li>@Transactional → WRITE (명시적 쓰기)</li>
   * </ul>
   * 
   * @return WRITE 또는 READ
   */
  @Override
  protected Object determineCurrentLookupKey() {
    // Transaction이 활성화되어 있고, readOnly가 아닌 경우만 WRITE
    if (TransactionSynchronizationManager.isActualTransactionActive() &&
        !TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
      
      if (log.isTraceEnabled()) {
        log.trace("Current DataSource: WRITE (transaction active, not readOnly)");
      }
      return DatabaseType.WRITE;
    }
    
    // 나머지는 모두 READ (Default)
    // - @Transactional 없음 → READ
    // - @Transactional(readOnly=true) → READ
    if (log.isTraceEnabled()) {
      log.trace("Current DataSource: READ (default)");
    }
    return DatabaseType.READ;
  }
}
