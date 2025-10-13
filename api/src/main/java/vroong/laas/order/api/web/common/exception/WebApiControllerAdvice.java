package vroong.laas.order.api.web.common.exception;

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
import vroong.laas.order.core.common.exception.BaseException;
import vroong.laas.order.core.common.exception.ErrorCode;
import vroong.laas.order.core.domain.address.exception.AddressRefineFailedException;

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
   * 주소 정제 실패 예외 처리
   *
   * <p>모든 역지오코딩 Provider가 실패하여 주소 정제에 실패한 경우 발생합니다.
   *
   * <p>외부 서비스 장애로 인한 일시적 오류이므로 503 Service Unavailable을 반환하며,
   * 클라이언트가 재시도할 수 있도록 Retry-After 헤더를 추가합니다.
   *
   * @param e AddressRefineFailedException
   * @return 503 Service Unavailable + ProblemDetail
   */
  @ExceptionHandler(AddressRefineFailedException.class)
  public ResponseEntity<ProblemDetail> handleAddressRefineFailed(
      AddressRefineFailedException e) {
    log.warn("Address refinement failed: {}", e.getMessage());

    ProblemDetail problem =
        ProblemDetailBuilder.of(
                HttpStatus.SERVICE_UNAVAILABLE, e.getErrorCode(), e.getMessage(), e)
            .retryable(true) // ⭐ 재시도 가능 (부릉 내부 표준)
            .build();

    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .header("Retry-After", "60") // 60초 후 재시도 권장
        .body(problem);
  }

  /**
   * 커스텀 예외 처리
   *
   * <p>Domain/Application Layer에서 발생하는 모든 커스텀 예외를 처리합니다.
   *
   * <p>처리 대상:
   * - OrderNotFoundException
   * - OrderAlreadyAssignedException
   * - OrderNotCancellableException
   * - OrderLocationChangeNotAllowedException
   * - InvalidOrderException 등
   *
   * @param e BaseException
   * @return 400 Bad Request + ProblemDetail
   */
  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ProblemDetail> handleBaseException(BaseException e) {
    log.warn("Base exception: {} - {}", e.getErrorCode().getCode(), e.getMessage());

    ProblemDetail problem =
        ProblemDetailBuilder.of(HttpStatus.BAD_REQUEST, e.getErrorCode(), e.getMessage(), e)
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

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

    ProblemDetail problem =
        ProblemDetailBuilder.of(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_INPUT, e.getMessage(), e)
            .build();

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

    ProblemDetail problem =
        ProblemDetailBuilder.of(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                ErrorCode.VALIDATION_ERROR.getMessage(),
                e)
            .fieldErrors(fieldErrors)
            .build();

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

    ProblemDetail problem =
        ProblemDetailBuilder.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                e)
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
  }
}
