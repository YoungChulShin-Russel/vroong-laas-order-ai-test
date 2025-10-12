package vroong.laas.order.job.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vroong.laas.order.core.domain.outbox.OutboxEventPublisher;
import vroong.laas.order.job.common.config.OutboxEventPublishProperties;
import vroong.laas.order.job.scheduled.BaseScheduledJob;

/**
 * Outbox Event Publish Job
 *
 * <p>Outbox 테이블에서 미전송 이벤트를 Kafka로 발행합니다.
 *
 * <p>실행 주기: 10초 (job.outbox.publish.fixed-delay)
 *
 * <p>AOP가 자동으로 로깅, 실행 시간 측정, 예외 처리를 수행합니다.
 *
 * <p>아키텍처:
 * <pre>
 * OutboxEventPublishJob (Job Layer)
 *   → OutboxEventPublisher (Domain Service)
 *     → OutboxEventClient (Port)
 *       → KafkaOutboxEventClient (Infrastructure Adapter)
 * </pre>
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    prefix = "job.outbox.publish",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false)
public class OutboxEventPublishJob implements BaseScheduledJob {

  private final OutboxEventPublisher outboxEventPublisher;  // Domain Service
  private final OutboxEventPublishProperties properties;

  /**
   * Outbox Event Publish 실행
   *
   * <p>ScheduledJobLoggingAspect가 자동으로 로깅 및 예외 처리를 수행합니다.
   */
  @Scheduled(fixedDelayString = "${job.outbox.publish.fixed-delay:10000}")
  @Override
  public void execute() {
    int publishedCount = outboxEventPublisher.publishPendingEvents(properties.getBatchSize());

    if (publishedCount > 0) {
      log.info("Published {} events from outbox to Kafka", publishedCount);
    }
  }
}

