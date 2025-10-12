package vroong.laas.order.job.scheduled;

/**
 * Scheduled Job 기본 인터페이스
 *
 * <p>모든 Scheduled Job이 구현해야 하는 계약을 정의합니다.
 *
 * <p>구현 방법:
 * <ul>
 *   <li>일반적인 경우: {@link LoggingScheduledJob} 상속 (권장)</li>
 *   <li>특수한 경우: BaseScheduledJob 직접 구현</li>
 * </ul>
 *
 * <p>일반적인 사용 예시 (LoggingScheduledJob 상속):
 * <pre>{@code
 * @Component
 * public class OutboxPollingJob extends LoggingScheduledJob {
 *
 *     @Override
 *     protected String getJobName() {
 *         return "OutboxPolling";
 *     }
 *
 *     @Override
 *     protected void doExecute() {
 *         // 비즈니스 로직
 *     }
 *
 *     @Scheduled(fixedDelay = 10000)
 *     public void schedule() {
 *         execute();
 *     }
 * }
 * }</pre>
 *
 * <p>직접 구현 예시 (특수한 경우):
 * <pre>{@code
 * @Component
 * public class SimpleHealthCheckJob implements BaseScheduledJob {
 *
 *     @Override
 *     public void execute() {
 *         // 간단한 헬스 체크 로직
 *     }
 *
 *     @Scheduled(fixedRate = 60000)
 *     public void schedule() {
 *         execute();
 *     }
 * }
 * }</pre>
 */
public interface BaseScheduledJob {

  /**
   * Job 실행
   *
   * <p>@Scheduled 메서드에서 이 메서드를 호출합니다.
   *
   * <p>{@link LoggingScheduledJob}을 상속하는 경우:
   * <ul>
   *   <li>실행 시작/종료 로깅 자동 수행</li>
   *   <li>실행 시간 자동 측정</li>
   *   <li>예외 자동 처리 (다음 실행 보장)</li>
   * </ul>
   *
   * <p>직접 구현하는 경우:
   * <ul>
   *   <li>로깅, 예외 처리 등을 직접 구현해야 함</li>
   * </ul>
   */
  void execute();
}

