package vroong.laas.order.core.domain.order.exception;

import vroong.laas.order.core.common.exception.BaseException;
import vroong.laas.order.core.common.exception.ErrorCode;

/**
 * 이미 배정된 주문에 대한 중복 배정 시도 시 발생하는 예외
 *
 * <p>사용 시나리오:
 * - 2명의 기사가 동시에 같은 주문 배정 요청
 * - 이미 배정된 주문을 다시 배정하려는 경우
 *
 * <p>HTTP 상태 코드: 409 Conflict 권장
 */
public class OrderAlreadyAssignedException extends BaseException {

  /**
   * 주문 ID로 예외 생성
   *
   * @param orderId 주문 ID
   */
  public OrderAlreadyAssignedException(Long orderId) {
    super(
        ErrorCode.ORDER_ALREADY_ASSIGNED,
        "이미 다른 기사님이 배정된 주문입니다. 주문 ID: " + orderId);
  }

  /**
   * 주문 ID와 배정된 기사 ID로 예외 생성
   *
   * @param orderId 주문 ID
   * @param assignedDriverId 배정된 기사 ID
   */
  public OrderAlreadyAssignedException(Long orderId, Long assignedDriverId) {
    super(
        ErrorCode.ORDER_ALREADY_ASSIGNED,
        "이미 다른 기사님이 배정된 주문입니다. 주문 ID: " + orderId + ", 기사 ID: " + assignedDriverId);
  }
}
