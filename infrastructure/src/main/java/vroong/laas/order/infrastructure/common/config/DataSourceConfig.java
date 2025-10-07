package vroong.laas.order.infrastructure.common.config;

import static vroong.laas.order.infrastructure.common.config.DataSourceRoutingConfig.DatabaseType;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import com.zaxxer.hikari.HikariDataSource;

/**
 * DataSource 설정 (Writer/Reader 분리)
 * 
 * <p><strong>Local 환경:</strong>
 * <ul>
 *   <li>Writer/Reader 모두 localhost:3306 사용 (같은 DB)</li>
 *   <li>Routing 로직 테스트 가능</li>
 * </ul>
 * 
 * <p><strong>Production 환경:</strong>
 * <ul>
 *   <li>Writer: Aurora Cluster Endpoint</li>
 *   <li>Reader: Aurora Reader Endpoint</li>
 *   <li>AWS Advanced JDBC Driver 사용</li>
 * </ul>
 * 
 * <p>LazyConnectionDataSourceProxy로 Connection 지연 획득
 */
@Configuration
public class DataSourceConfig {
  
  public static final String SERVICE_NAME = "order";
  public static final String WRITER_DATASOURCE_NAME = "writerDataSource";
  public static final String READER_DATASOURCE_NAME = "readerDataSource";
  public static final String ROUTING_DATASOURCE_NAME = "routingDataSource";
  
  /**
   * Writer DataSource (Aurora Writer Endpoint)
   * 
   * <p>쓰기 작업 전용 (INSERT, UPDATE, DELETE)
   * <p>@Transactional → 이 DataSource 사용
   */
  @Bean(name = WRITER_DATASOURCE_NAME)
  @ConfigurationProperties(prefix = SERVICE_NAME + ".datasource.write")
  public DataSource writerDataSource() {
    return DataSourceBuilder.create()
        .type(HikariDataSource.class)
        .build();
  }
  
  /**
   * Reader DataSource (Aurora Reader Endpoint)
   * 
   * <p>읽기 작업 전용 (SELECT)
   * <p>@Transactional(readOnly = true) → 이 DataSource 사용
   */
  @Bean(name = READER_DATASOURCE_NAME)
  @ConfigurationProperties(prefix = SERVICE_NAME + ".datasource.read")
  public DataSource readerDataSource() {
    return DataSourceBuilder.create()
        .type(HikariDataSource.class)
        .build();
  }
  
  /**
   * Routing DataSource
   * 
   * <p><strong>라우팅 전략:</strong>
   * <ul>
   *   <li>@Transactional → WRITE</li>
   *   <li>@Transactional(readOnly=true) → READ</li>
   *   <li>@Transactional 없음 → READ (Default)</li>
   * </ul>
   * 
   * <p><strong>Default를 READ로 설정하는 이유:</strong>
   * <ul>
   *   <li>조회 시 @Transactional(readOnly=true) 생략 가능</li>
   *   <li>저장 시 @Transactional 누락하면 즉시 에러 발생 (read-only)</li>
   *   <li>Reader Pool 효율적 활용 (읽기 90% 트래픽)</li>
   *   <li>Writer Pool 보호</li>
   * </ul>
   */
  @Bean(name = ROUTING_DATASOURCE_NAME)
  public DataSource routingDataSource(
      @Qualifier(WRITER_DATASOURCE_NAME) DataSource writerDataSource,
      @Qualifier(READER_DATASOURCE_NAME) DataSource readerDataSource) {
    
    DataSourceRoutingConfig routingDataSource = new DataSourceRoutingConfig();
    
    Map<Object, Object> targetDataSourceMap = new HashMap<>();
    targetDataSourceMap.put(DatabaseType.WRITE, writerDataSource);
    targetDataSourceMap.put(DatabaseType.READ, readerDataSource);
    
    routingDataSource.setTargetDataSources(targetDataSourceMap);
    
    // Default는 READ ⭐
    // - @Transactional 없는 조회 → READ Pool 사용 (효율적)
    // - @Transactional 없는 저장 → READ Pool에서 에러 발생 (강제 감지)
    routingDataSource.setDefaultTargetDataSource(readerDataSource);
    
    return routingDataSource;
  }
  
  /**
   * Primary DataSource (LazyConnectionDataSourceProxy)
   * 
   * <p>핵심: Connection 획득을 지연시켜서
   * @Transactional(readOnly=true)가 먼저 적용되도록 함
   * 
   * <p>이것 없으면 readOnly 라우팅이 제대로 작동 안 함!
   */
  @Bean
  @Primary
  public DataSource dataSource(
      @Qualifier(ROUTING_DATASOURCE_NAME) DataSource routingDataSource) {
    return new LazyConnectionDataSourceProxy(routingDataSource);
  }
}
