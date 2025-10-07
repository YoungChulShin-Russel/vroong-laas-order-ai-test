package vroong.laas.order.infrastructure.common.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Local 환경 DataSource 설정
 * 
 * <p>Docker MySQL 단일 DataSource 사용
 * <p>Read/Write 분리 없음 (단순 구조)
 */
@Configuration
@Profile("local")
public class LocalDataSourceConfig {
  
  public static final String SERVICE_NAME = "order";
  
  /**
   * Local DataSource (Docker MySQL)
   * 
   * <p>단일 DataSource만 사용
   * <p>Read/Write 구분 없음
   * 
   * <p>설정 구조:
   * <pre>
   * order:
   *   datasource:
   *     driver-class-name: ...
   *     url: ...
   *     username: ...
   *     password: ...
   *     hikari:
   *       pool-name: ...
   *       maximum-pool-size: ...
   * </pre>
   */
  @Bean
  @Primary
  @ConfigurationProperties(prefix = SERVICE_NAME + ".datasource")
  public DataSource dataSource() {
    return DataSourceBuilder.create()
        .type(HikariDataSource.class)
        .build();
  }
}
