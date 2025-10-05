package vroong.laas.order.api.web.common.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Web API 전역 예외 처리기
 *
 * <p>모든 REST Controller에서 발생하는 예외를 RFC 7807 표준(ProblemDetail)에 따라 처리합니다.
 */
@RestControllerAdvice
public class WebApiControllerAdvice {

  private static final Logger log = LoggerFactory.getLogger(WebApiControllerAdvice.class);

  /**
   * Domain 검증 예외 처리
   *
   * <p>IllegalArgumentException은 주로 Domain Layer에서 비즈니스 규칙 위반 시 발생합니다.
   *
   * @param e IllegalArgumentException
   * @return 400 Bad Request
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ProblemDetail> handleIllegalArgumentException(
      IllegalArgumentException e) {
    log.warn("Domain validation failed: {}", e.getMessage());

    ProblemDetail problem = getProblemDetail(HttpStatus.BAD_REQUEST, e);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

  /**
   * Bean Validation 예외 처리
   *
   * <p>@Valid 어노테이션으로 인한 검증 실패 시 발생합니다.
   *
   * @param e MethodArgumentNotValidException
   * @return 400 Bad Request
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    log.warn("Bean validation failed: {}", e.getMessage());

    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError error : e.getBindingResult().getFieldErrors()) {
      fieldErrors.put(error.getField(), error.getDefaultMessage());
    }

    ProblemDetail problem = getProblemDetail(HttpStatus.BAD_REQUEST, e);
    problem.setDetail("입력값 검증에 실패했습니다"); // 메시지 변경
    problem.setProperty("fieldErrors", fieldErrors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

  /**
   * 일반 예외 처리
   *
   * <p>예상하지 못한 예외가 발생한 경우 처리합니다.
   *
   * @param e Exception
   * @return 500 Internal Server Error
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleException(Exception e) {
    log.error("Unexpected error occurred", e);

    ProblemDetail problem = getProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, e);
    problem.setDetail("서버 오류가 발생했습니다"); // 메시지 변경

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
  }

   /**
   * ProblemDetail 생성 공통 메서드
   *
   * @param status HTTP 상태 코드
   * @param exception 예외
   * @return ProblemDetail (timestamp, exception 포함)
   */
  private static ProblemDetail getProblemDetail(HttpStatus status, Exception exception) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(status, exception.getMessage());
    problemDetail.setProperty("timestamp", LocalDateTime.now());
    problemDetail.setProperty("exception", exception.getClass().getSimpleName());

    return problemDetail;
  }
}
