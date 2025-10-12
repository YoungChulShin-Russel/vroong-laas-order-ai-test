package vroong.laas.order.job.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Outbox Event Publish Job 설정 Properties
 *
 * <p>Outbox 테이블에서 미전송 이벤트를 Kafka로 발행하는 Job의 설정을 외부화합니다.
 *
 * <p>설정 예시 (application.yml):
 * <pre>
 * job:
 *   outbox:
 *     publish:
 *       enabled: true
 *       fixed-delay: 10000
 *       batch-size: 100
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "job.outbox.publish")
@Getter
@Setter
public class OutboxEventPublishProperties {

  /**
   * Outbox Event Publish Job 활성화 여부
   *
   * <p>false로 설정하면 Job이 실행되지 않습니다.
   */
  private boolean enabled = true;

  /**
   * Job 실행 주기 (milliseconds)
   *
   * <p>이전 실행이 완료된 후 다음 실행까지 대기 시간입니다.
   *
   * <p>권장값:
   * <ul>
   *   <li>실시간성이 중요한 경우: 5000-10000 (5-10초)</li>
   *   <li>일반적인 경우: 10000-30000 (10-30초)</li>
   *   <li>부하가 적은 경우: 60000 (1분)</li>
   * </ul>
   */
  private long fixedDelay = 10000;

  /**
   * 한 번에 처리할 이벤트 수
   *
   * <p>Outbox 테이블에서 가져올 최대 이벤트 수입니다.
   *
   * <p>권장값:
   * <ul>
   *   <li>이벤트가 많은 경우: 100-500</li>
   *   <li>일반적인 경우: 50-100</li>
   *   <li>이벤트가 적은 경우: 10-50</li>
   * </ul>
   */
  private int batchSize = 100;
}

