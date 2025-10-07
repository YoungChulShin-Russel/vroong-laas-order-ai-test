package vroong.laas.order.infrastructure.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static vroong.laas.order.infrastructure.common.config.ReplicationRoutingDataSource.DatabaseType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * ReplicationRoutingDataSource Routing 로직 테스트
 * 
 * <p>전략: @Transactional 강제 + Default READ
 */
class ReplicationRoutingDataSourceTest {
  
  private final ReplicationRoutingDataSource routingDataSource = new ReplicationRoutingDataSource();
  
  @AfterEach
  void tearDown() {
    // TransactionSynchronizationManager 초기화
    TransactionSynchronizationManager.clear();
  }
  
  @Test
  @DisplayName("@Transactional + readOnly = true → READ DataSource")
  void when_transaction_with_readonly_true_then_read_datasource() {
    // given
    TransactionSynchronizationManager.setActualTransactionActive(true);
    TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);
    
    // when
    Object lookupKey = routingDataSource.determineCurrentLookupKey();
    
    // then
    assertThat(lookupKey).isEqualTo(DatabaseType.READ);
  }
  
  @Test
  @DisplayName("@Transactional + readOnly = false → WRITE DataSource")
  void when_transaction_with_readonly_false_then_write_datasource() {
    // given
    TransactionSynchronizationManager.setActualTransactionActive(true);
    TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
    
    // when
    Object lookupKey = routingDataSource.determineCurrentLookupKey();
    
    // then
    assertThat(lookupKey).isEqualTo(DatabaseType.WRITE);
  }
  
  @Test
  @DisplayName("@Transactional 없음 → READ DataSource (Default) ⭐")
  void when_no_transaction_then_read_datasource() {
    // given
    // Transaction 설정 없음 (실제로는 isActualTransactionActive = false)
    
    // when
    Object lookupKey = routingDataSource.determineCurrentLookupKey();
    
    // then
    assertThat(lookupKey).isEqualTo(DatabaseType.READ);
    // → 조회는 자동으로 READ Pool 사용 ✅
    // → 저장은 READ Pool에서 에러 발생 (강제 감지) ✅
  }
  
  @Test
  @DisplayName("Transaction 비활성 + readOnly = true → READ DataSource")
  void when_transaction_inactive_with_readonly_true_then_read_datasource() {
    // given
    TransactionSynchronizationManager.setActualTransactionActive(false);
    TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);
    
    // when
    Object lookupKey = routingDataSource.determineCurrentLookupKey();
    
    // then
    assertThat(lookupKey).isEqualTo(DatabaseType.READ);
  }
}
