package vroong.laas.order.api.web.common.exception;

import java.time.Instant;
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
import vroong.laas.order.api.web.common.dto.ErrorCode;

/**
 * Web API 전역 예외 처리기
 *
 * <p>모든 REST Controller에서 발생하는 예외를 ProblemDetail (RFC 7807)로 통일하여 처리합니다.
 *
 * <p>응답 정책:
 * - 성공 (2xx): 객체 직접 반환 (OrderResponse, PageResponse 등)
 * - 실패 (4xx, 5xx): ProblemDetail 반환
 *
 * <p>HTTP 상태 코드 정책:
 * - 4xx: 클라이언트 입력 에러
 * - 5xx: 서버 에러
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
   * @return 400 Bad Request + ProblemDetail
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ProblemDetail> handleIllegalArgumentException(
      IllegalArgumentException e) {
    log.warn("Domain validation failed: {}", e.getMessage());

    ProblemDetail problem = createProblemDetail(
        HttpStatus.BAD_REQUEST,
        ErrorCode.INVALID_INPUT,
        e.getMessage(),
        e
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

  /**
   * Bean Validation 예외 처리
   *
   * <p>@Valid 어노테이션으로 인한 검증 실패 시 발생합니다.
   *
   * @param e MethodArgumentNotValidException
   * @return 400 Bad Request + ProblemDetail
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    log.warn("Bean validation failed: {}", e.getMessage());

    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError error : e.getBindingResult().getFieldErrors()) {
      fieldErrors.put(error.getField(), error.getDefaultMessage());
    }

    ProblemDetail problem = createProblemDetail(
        HttpStatus.BAD_REQUEST,
        ErrorCode.VALIDATION_ERROR,
        ErrorCode.VALIDATION_ERROR.getMessage(),
        e
    );
    problem.setProperty("fieldErrors", fieldErrors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

  /**
   * 일반 예외 처리
   *
   * <p>예상하지 못한 예외가 발생한 경우 처리합니다.
   *
   * @param e Exception
   * @return 500 Internal Server Error + ProblemDetail
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleException(Exception e) {
    log.error("Unexpected error occurred", e);

    ProblemDetail problem = createProblemDetail(
        HttpStatus.INTERNAL_SERVER_ERROR,
        ErrorCode.INTERNAL_SERVER_ERROR,
        ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
        e
    );

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
  }

  /**
   * ProblemDetail 생성 헬퍼 메서드
   *
   * <p>RFC 7807 표준에 따라 ProblemDetail을 생성하고, 추가 정보를 포함합니다.
   *
   * @param status HTTP 상태 코드
   * @param errorCode 에러 코드 enum
   * @param detail 상세 메시지
   * @param exception 예외 객체
   * @return ProblemDetail
   */
  private static ProblemDetail createProblemDetail(
      HttpStatus status,
      ErrorCode errorCode,
      String detail,
      Exception exception) {
    ProblemDetail problem = ProblemDetail.forStatus(status);
    problem.setDetail(detail);
    problem.setProperty("timestamp", Instant.now());
    problem.setProperty("errorCode", errorCode.getCode());
    problem.setProperty("exception", exception.getClass().getSimpleName());
    return problem;
  }
}
