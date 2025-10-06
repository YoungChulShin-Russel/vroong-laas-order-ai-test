package vroong.laas.order.core.domain.order.exception;

import vroong.laas.order.core.common.exception.BaseException;
import vroong.laas.order.core.common.exception.ErrorCode;
import vroong.laas.order.core.domain.order.OrderStatus;

/**
 * 현재 상태에서 취소할 수 없는 주문을 취소하려 할 때 발생하는 예외
 *
 * <p>사용 시나리오:
 * - 이미 배송 완료된 주문 취소 시도
 * - 이미 취소된 주문 재취소 시도
 */
public class OrderNotCancellableException extends BaseException {

  /**
   * 주문 ID로 예외 생성
   *
   * @param orderId 주문 ID
   */
  public OrderNotCancellableException(Long orderId) {
    super(ErrorCode.ORDER_NOT_CANCELLABLE, "현재 상태에서는 취소할 수 없습니다. 주문 ID: " + orderId);
  }

  /**
   * 주문 ID와 현재 상태로 예외 생성
   *
   * @param orderId 주문 ID
   * @param currentStatus 현재 주문 상태
   */
  public OrderNotCancellableException(Long orderId, OrderStatus currentStatus) {
    super(
        ErrorCode.ORDER_NOT_CANCELLABLE,
        "현재 상태(" + currentStatus + ")에서는 취소할 수 없습니다. 주문 ID: " + orderId);
  }

  /**
   * 기본 메시지로 예외 생성 (Domain 내부 검증용)
   *
   * @param message 상세 메시지
   */
  public OrderNotCancellableException(String message) {
    super(ErrorCode.ORDER_NOT_CANCELLABLE, message);
  }
}
