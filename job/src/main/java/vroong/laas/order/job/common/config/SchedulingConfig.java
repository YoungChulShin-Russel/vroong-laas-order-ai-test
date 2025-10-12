package vroong.laas.order.job.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Scheduling 설정
 *
 * <p>Spring @Scheduled의 Thread Pool을 설정합니다.
 *
 * <p>기본 설정:
 * <ul>
 *   <li>Pool Size: 10 (동시에 10개의 Job 실행 가능)</li>
 *   <li>Thread Name Prefix: "scheduled-job-"</li>
 *   <li>Daemon: false (애플리케이션 종료 시 작업 완료 대기)</li>
 *   <li>Wait for Tasks: true (Graceful Shutdown)</li>
 * </ul>
 *
 * <p>이유:
 * <ul>
 *   <li>기본 @Scheduled는 단일 스레드로 동작 (Job 순차 실행)</li>
 *   <li>Thread Pool 설정으로 여러 Job이 동시에 실행 가능</li>
 *   <li>긴 실행 시간의 Job이 다른 Job을 블로킹하지 않음</li>
 * </ul>
 *
 * <p>예시:
 * <pre>
 * OutboxPollingJob (10초마다) ──┐
 * OrderStatisticsJob (1시간마다) ├─→ Thread Pool (동시 실행)
 * DataCleanupJob (2시간마다) ────┘
 * </pre>
 */
@Configuration
@RequiredArgsConstructor
public class SchedulingConfig {

  private final SchedulingProperties properties;

  /**
   * TaskScheduler Bean 설정
   *
   * <p>Spring이 @Scheduled 애노테이션을 처리할 때 이 TaskScheduler를 사용합니다.
   *
   * @return ThreadPoolTaskScheduler
   */
  @Bean
  public ThreadPoolTaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

    // Thread Pool Size: 동시에 실행 가능한 Job 수
    scheduler.setPoolSize(properties.getPoolSize());

    // Thread 이름 Prefix (로깅, 디버깅 시 유용)
    scheduler.setThreadNamePrefix(properties.getThreadNamePrefix());

    // Daemon Thread 여부 (false: 애플리케이션 종료 시 작업 완료 대기)
    scheduler.setDaemon(false);

    // Graceful Shutdown: 종료 시 실행 중인 Job 완료 대기
    scheduler.setWaitForTasksToCompleteOnShutdown(true);

    // Shutdown 대기 시간 (초)
    scheduler.setAwaitTerminationSeconds(properties.getAwaitTerminationSeconds());

    scheduler.initialize();

    return scheduler;
  }
}

