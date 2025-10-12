package vroong.laas.order.core.domain.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vroong.laas.order.core.domain.outbox.required.OutboxEventClient;

/**
 * Outbox Event Publisher (Domain Service)
 *
 * <p>Outbox 테이블에서 미전송 이벤트를 Kafka로 발행하는 Domain Service입니다.
 *
 * <p>책임:
 * <ul>
 *   <li>Outbox Polling Job에서 주기적으로 호출</li>
 *   <li>OutboxEventClient Port를 통해 Infrastructure 호출</li>
 *   <li>발행된 이벤트 수 반환</li>
 * </ul>
 *
 * <p>흐름:
 * <pre>
 * OutboxPollingJob (Job Layer)
 *   → OutboxEventPublisher (Domain Service)
 *     → OutboxEventClient (Port)
 *       → KafkaOutboxEventClient (Infrastructure Adapter)
 *         → OutboxEventService.publishPendingEvents() (외부 라이브러리)
 * </pre>
 */
@Service
@RequiredArgsConstructor
public class OutboxEventPublisher {

  private final OutboxEventClient outboxEventClient;

  /**
   * Outbox에서 미전송 이벤트를 Kafka로 발행
   *
   * @param batchSize 한 번에 처리할 이벤트 수
   * @return 발행된 이벤트 수
   */
  public int publishPendingEvents(int batchSize) {
    return outboxEventClient.publishPendingEvents(batchSize);
  }
}

