package vroong.laas.order.core.domain.address.exception;

import vroong.laas.order.core.common.exception.BaseException;
import vroong.laas.order.core.common.exception.ErrorCode;

/**
 * 주소 정제 실패 예외
 *
 * <p>역지오코딩을 통한 주소 정제가 실패했을 때 발생합니다.
 *
 * <p>발생 시나리오:
 * - 모든 역지오코딩 Provider(Neogeo, Naver, Kakao)가 실패한 경우
 * - 네트워크 오류, Timeout, 4xx/5xx 에러 등
 *
 * <p>처리 방법:
 * - Order 생성 실패로 처리됨
 * - 사용자에게 주소 확인 요청
 */
public class AddressRefineFailedException extends BaseException {

  /**
   * 메시지와 함께 예외 생성
   *
   * @param message 상세 메시지
   */
  public AddressRefineFailedException(String message) {
    super(ErrorCode.ADDRESS_REFINE_FAILED, message);
  }

  /**
   * 메시지와 원인 예외와 함께 예외 생성
   *
   * @param message 상세 메시지
   * @param cause 원인 예외
   */
  public AddressRefineFailedException(String message, Throwable cause) {
    super(ErrorCode.ADDRESS_REFINE_FAILED, message, cause);
  }
}

