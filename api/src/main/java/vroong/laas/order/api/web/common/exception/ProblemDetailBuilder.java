package vroong.laas.order.api.web.common.exception;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import vroong.laas.order.core.common.exception.ErrorCode;

/**
 * ProblemDetail을 생성하는 Builder 클래스
 *
 * <p>RFC 7807 (Problem Details for HTTP APIs) 표준을 따르는 에러 응답을 일관되게 생성합니다.
 *
 * <p>사용 예시:
 *
 * <pre>{@code
 * ProblemDetail problem = ProblemDetailBuilder
 *     .of(HttpStatus.SERVICE_UNAVAILABLE, errorCode, detail, exception)
 *     .retryable(true)
 *     .build();
 * }</pre>
 */
public class ProblemDetailBuilder {

  private final ProblemDetail problemDetail;

  private ProblemDetailBuilder(
      HttpStatus status, ErrorCode errorCode, String detail, Exception exception) {
    this.problemDetail = ProblemDetail.forStatus(status);
    this.problemDetail.setDetail(detail);

    // 기본 속성 (항상 포함)
    this.problemDetail.setProperty("timestamp", Instant.now());
    this.problemDetail.setProperty("errorCode", errorCode.getCode());
    this.problemDetail.setProperty("exception", exception.getClass().getSimpleName());
  }

  /**
   * ProblemDetailBuilder 인스턴스 생성
   *
   * @param status HTTP 상태 코드
   * @param errorCode 에러 코드
   * @param detail 상세 메시지
   * @param exception 발생한 예외
   * @return ProblemDetailBuilder
   */
  public static ProblemDetailBuilder of(
      HttpStatus status, ErrorCode errorCode, String detail, Exception exception) {
    return new ProblemDetailBuilder(status, errorCode, detail, exception);
  }

  /**
   * 재시도 가능 여부 설정
   *
   * <p>일시적 장애(503)로 인한 에러인 경우 true로 설정합니다.
   *
   * @param retryable 재시도 가능 여부
   * @return this
   */
  public ProblemDetailBuilder retryable(boolean retryable) {
    this.problemDetail.setProperty("retryable", retryable);
    return this;
  }

  /**
   * Bean Validation 에러 필드 추가
   *
   * @param fieldErrors 필드별 에러 메시지 (fieldName → errorMessage)
   * @return this
   */
  public ProblemDetailBuilder fieldErrors(Map<String, String> fieldErrors) {
    this.problemDetail.setProperty("fieldErrors", fieldErrors);
    return this;
  }

  /**
   * 커스텀 속성 추가
   *
   * @param key 속성 키
   * @param value 속성 값
   * @return this
   */
  public ProblemDetailBuilder property(String key, Object value) {
    this.problemDetail.setProperty(key, value);
    return this;
  }

  /**
   * ProblemDetail 생성
   *
   * @return ProblemDetail
   */
  public ProblemDetail build() {
    return this.problemDetail;
  }
}

