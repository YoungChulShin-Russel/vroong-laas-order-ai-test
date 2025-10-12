package vroong.laas.order.job.scheduled;

import lombok.extern.slf4j.Slf4j;

/**
 * 로깅 기능이 포함된 Scheduled Job 추상 클래스
 *
 * <p>BaseScheduledJob의 기본 구현을 제공합니다.
 *
 * <p>제공하는 공통 기능:
 * <ul>
 *   <li>실행 시작/종료 로깅</li>
 *   <li>실행 시간 측정</li>
 *   <li>예외 처리 및 로깅</li>
 *   <li>일관된 실행 흐름</li>
 * </ul>
 *
 * <p>하위 클래스는 다음을 구현해야 합니다:
 * <ul>
 *   <li>getJobName(): Job 이름 (로깅용)</li>
 *   <li>doExecute(): 실제 비즈니스 로직</li>
 * </ul>
 *
 * <p>예시:
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
 */
@Slf4j
public abstract class LoggingScheduledJob implements BaseScheduledJob {

  /**
   * Job 이름 반환
   *
   * <p>로깅 및 모니터링에 사용됩니다.
   *
   * @return Job 이름 (예: "OutboxPolling", "OrderStatistics")
   */
  protected abstract String getJobName();

  /**
   * 실제 비즈니스 로직 구현
   *
   * <p>하위 클래스에서 구현합니다.
   *
   * <p>예외 발생 시 자동으로 로깅되고 다음 실행이 보장됩니다.
   */
  protected abstract void doExecute();

  /**
   * Job 실행 (템플릿 메서드 패턴)
   *
   * <p>공통 로직:
   * <ol>
   *   <li>실행 시작 로그</li>
   *   <li>실행 시간 측정 시작</li>
   *   <li>doExecute() 호출 (비즈니스 로직)</li>
   *   <li>실행 시간 측정 종료</li>
   *   <li>성공/실패 로그</li>
   * </ol>
   *
   * <p>@Scheduled 메서드에서 이 메서드를 호출합니다.
   */
  @Override
  public final void execute() {
    String jobName = getJobName();
    log.debug("[{}] Job started", jobName);

    long startTime = System.currentTimeMillis();

    try {
      doExecute();

      long duration = System.currentTimeMillis() - startTime;
      log.info("[{}] Job completed successfully in {}ms", jobName, duration);

    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("[{}] Job failed after {}ms: {}", jobName, duration, e.getMessage(), e);
      // 예외를 다시 던지지 않음 → 다음 실행 보장
    }
  }
}

