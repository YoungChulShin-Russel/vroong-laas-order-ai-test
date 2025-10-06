package vroong.laas.order.core.common.exception;

import lombok.Getter;

/**
 * 모든 커스텀 예외의 기반 클래스
 *
 * <p>애플리케이션에서 발생하는 모든 커스텀 예외는 이 클래스를 상속받습니다.
 *
 * <p>특징:
 * - ErrorCode를 포함하여 클라이언트에게 구조화된 에러 정보 전달
 * - RuntimeException을 상속하여 Unchecked Exception으로 동작
 * - 로그 추적 및 에러 핸들링을 위한 공통 인터페이스 제공
 *
 * <p>확장 가능:
 * - BusinessException (비즈니스 로직 예외)
 * - TechnicalException (기술적 예외)
 * - InfrastructureException (인프라 예외)
 */
@Getter
public abstract class BaseException extends RuntimeException {

  /** 에러 코드 */
  private final ErrorCode errorCode;

  /**
   * ErrorCode와 커스텀 메시지로 예외 생성
   *
   * @param errorCode 에러 코드
   * @param message 상세 메시지
   */
  protected BaseException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  /**
   * ErrorCode의 기본 메시지로 예외 생성
   *
   * @param errorCode 에러 코드
   */
  protected BaseException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  /**
   * ErrorCode, 커스텀 메시지, 원인 예외로 예외 생성
   *
   * @param errorCode 에러 코드
   * @param message 상세 메시지
   * @param cause 원인 예외
   */
  protected BaseException(ErrorCode errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }
}
