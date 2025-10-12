package vroong.laas.order.job.scheduled;

/**
 * Scheduled Job 기본 인터페이스
 *
 * <p>모든 Scheduled Job이 구현해야 하는 계약을 정의합니다.
 *
 * <p>사용 방법:
 * <ul>
 *   <li>BaseScheduledJob 인터페이스 구현</li>
 *   <li>execute() 메서드에 비즈니스 로직 작성</li>
 *   <li>execute() 메서드에 @Scheduled 애노테이션 추가</li>
 *   <li>{@link ScheduledJobLoggingAspect}가 자동으로 로깅, 시간측정, 예외처리</li>
 * </ul>
 *
 * <p>사용 예시:
 * <pre>{@code
 * @Component
 * public class OutboxPollingJob implements BaseScheduledJob {
 *
 *     private final OutboxEventService outboxEventService;
 *
 *     @Scheduled(fixedDelay = 10000)
 *     @Override
 *     public void execute() {
 *         // 비즈니스 로직 직접 작성
 *         int count = outboxEventService.publishPendingEvents(100);
 *         if (count > 0) {
 *             log.info("Published {} events", count);
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>AOP가 자동으로 제공하는 기능:
 * <ul>
 *   <li>실행 시작/종료 로깅</li>
 *   <li>실행 시간 측정</li>
 *   <li>예외 처리 (다음 실행 보장)</li>
 * </ul>
 *
 * <p>로그 출력 예시:
 * <pre>
 * 2025-01-12 10:00:00.123 DEBUG [OutboxPollingJob] Job started
 * 2025-01-12 10:00:00.456 INFO  Published 3 events
 * 2025-01-12 10:00:00.789 INFO  [OutboxPollingJob] Job completed successfully in 666ms
 * </pre>
 */
public interface BaseScheduledJob {

  /**
   * Job 실행
   *
   * <p>이 메서드에 @Scheduled 애노테이션을 붙여서 사용합니다.
   *
   * <p>{@link ScheduledJobLoggingAspect}가 자동으로 다음을 수행합니다:
   * <ul>
   *   <li>실행 시작 로그: [JobName] Job started</li>
   *   <li>실행 시간 측정</li>
   *   <li>성공 로그: [JobName] Job completed successfully in Nms</li>
   *   <li>실패 로그: [JobName] Job failed after Nms (예외 발생 시)</li>
   *   <li>다음 실행 보장 (예외를 삼킴)</li>
   * </ul>
   */
  void execute();
}

