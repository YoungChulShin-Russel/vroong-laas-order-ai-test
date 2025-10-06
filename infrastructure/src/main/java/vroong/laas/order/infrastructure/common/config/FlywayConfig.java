package vroong.laas.order.infrastructure.common.config;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Flyway 설정 (Local 환경에서만 실행)
 *
 * <p>Spring Boot AutoConfiguration에 의존하지 않고 직접 Flyway를 실행합니다.
 * Production 환경에서는 DBA가 수동으로 스크립트를 실행합니다.
 */
@Slf4j
@Configuration
@Profile("local") // Local 환경에서만 활성화
@RequiredArgsConstructor
class FlywayConfig {

  private final DataSource dataSource;

  @Bean(initMethod = "migrate")
  public Flyway flyway() {
    log.info("===== Flyway Migration Starting (Local Profile) =====");

    Flyway flyway =
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration") // 마이그레이션 파일 위치
            .baselineOnMigrate(true) // 기존 DB가 있어도 실행
            .baselineVersion("0") // Baseline 버전
            .validateOnMigrate(true) // 마이그레이션 검증
            .outOfOrder(false) // 순서 엄격히 지킴
            .cleanDisabled(true) // Clean 비활성화 (실수 방지)
            .load();

    log.info("Flyway configured with locations: classpath:db/migration");

    return flyway;
  }
}
