package vroong.laas.order.core.domain.order.exception;

import vroong.laas.order.core.common.exception.BaseException;
import vroong.laas.order.core.common.exception.ErrorCode;

/**
 * 유효하지 않은 주문 데이터로 인한 예외
 *
 * <p>사용 시나리오:
 * - 필수 값 누락
 * - 유효하지 않은 값 범위
 * - 비즈니스 규칙 위반
 *
 * <p>Domain 검증 실패 시 사용됩니다.
 */
public class InvalidOrderException extends BaseException {

  /**
   * 기본 메시지로 예외 생성
   *
   * @param message 상세 메시지
   */
  public InvalidOrderException(String message) {
    super(ErrorCode.INVALID_ORDER, message);
  }

  /**
   * 원인 예외를 포함하여 예외 생성
   *
   * @param message 상세 메시지
   * @param cause 원인 예외
   */
  public InvalidOrderException(String message, Throwable cause) {
    super(ErrorCode.INVALID_ORDER, message, cause);
  }
}
