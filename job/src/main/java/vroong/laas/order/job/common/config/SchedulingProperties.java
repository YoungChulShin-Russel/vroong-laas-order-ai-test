package vroong.laas.order.job.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Scheduling 설정 Properties
 *
 * <p>Scheduled Job의 Thread Pool 설정을 외부화합니다.
 *
 * <p>설정 예시 (application.yml):
 * <pre>
 * job:
 *   scheduling:
 *     pool-size: 10
 *     thread-name-prefix: "scheduled-job-"
 *     await-termination-seconds: 60
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "job.scheduling")
@Getter
@Setter
public class SchedulingProperties {

  /**
   * Thread Pool Size (동시에 실행 가능한 Job 수)
   *
   * <p>권장값:
   * <ul>
   *   <li>일반적인 경우: 10-20</li>
   *   <li>Job이 많은 경우: Job 개수 × 1.5</li>
   *   <li>CPU 집약적: CPU 코어 수</li>
   *   <li>I/O 집약적: CPU 코어 수 × 2</li>
   * </ul>
   */
  private int poolSize = 10;

  /**
   * Thread 이름 Prefix
   *
   * <p>로깅, 디버깅 시 Thread를 식별하기 위해 사용됩니다.
   */
  private String threadNamePrefix = "scheduled-job-";

  /**
   * Shutdown 대기 시간 (초)
   *
   * <p>애플리케이션 종료 시 실행 중인 Job 완료를 대기하는 최대 시간입니다.
   */
  private int awaitTerminationSeconds = 60;
}

