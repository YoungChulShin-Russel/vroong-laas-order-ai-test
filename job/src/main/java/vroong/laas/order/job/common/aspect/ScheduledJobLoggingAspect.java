package vroong.laas.order.job.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import vroong.laas.order.job.scheduled.BaseScheduledJob;

/**
 * Scheduled Job 로깅 AOP
 *
 * <p>@Scheduled 애노테이션이 붙은 메서드를 자동으로 가로채서 다음 기능을 제공합니다:
 * <ul>
 *   <li>실행 시작/종료 로깅</li>
 *   <li>실행 시간 측정</li>
 *   <li>예외 처리 및 로깅</li>
 *   <li>다음 실행 보장 (예외 발생 시에도)</li>
 * </ul>
 *
 * <p>사용 예시:
 * <pre>{@code
 * @Component
 * public class OutboxPollingJob implements BaseScheduledJob {
 *
 *     @Scheduled(fixedDelay = 10000)
 *     @Override
 *     public void execute() {
 *         // 비즈니스 로직
 *         // AOP가 자동으로 로깅, 시간측정, 예외처리
 *     }
 * }
 * }</pre>
 *
 * <p>로그 출력 예시:
 * <pre>
 * 2025-01-12 10:00:00.123 DEBUG [OutboxPollingJob] Job started
 * 2025-01-12 10:00:00.456 INFO  Published 3 events from outbox to Kafka
 * 2025-01-12 10:00:00.789 INFO  [OutboxPollingJob] Job completed successfully in 666ms
 * </pre>
 */
@Aspect
@Component
@Slf4j
public class ScheduledJobLoggingAspect {

  /**
   * @Scheduled 애노테이션이 붙은 메서드를 가로채서 로깅 및 시간 측정
   *
   * <p>Pointcut:
   * - @Scheduled 애노테이션이 있는 메서드
   * - BaseScheduledJob 타입의 클래스 내부
   *
   * @param joinPoint AOP Join Point
   * @return 메서드 실행 결과
   */
  @Around("@annotation(org.springframework.scheduling.annotation.Scheduled) && target(job)")
  public Object logScheduledJob(ProceedingJoinPoint joinPoint, BaseScheduledJob job)
      throws Throwable {

    String jobName = job.getClass().getSimpleName();
    log.debug("[{}] Job started", jobName);

    long startTime = System.currentTimeMillis();

    try {
      // 실제 Job 메서드 실행
      Object result = joinPoint.proceed();

      long duration = System.currentTimeMillis() - startTime;
      log.info("[{}] Job completed successfully in {}ms", jobName, duration);

      return result;

    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("[{}] Job failed after {}ms: {}", jobName, duration, e.getMessage(), e);

      // 예외를 다시 던지지 않음 → 다음 실행 보장
      return null;
    }
  }
}

