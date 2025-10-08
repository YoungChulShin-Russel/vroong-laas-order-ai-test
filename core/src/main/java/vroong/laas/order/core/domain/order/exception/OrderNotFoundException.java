package vroong.laas.order.core.domain.order.exception;

import vroong.laas.order.core.common.exception.BaseException;
import vroong.laas.order.core.common.exception.ErrorCode;
import vroong.laas.order.core.domain.order.OrderNumber;

/**
 * 주문을 찾을 수 없을 때 발생하는 예외
 *
 * <p>사용 시나리오: - ID로 주문 조회 실패 - 주문번호로 주문 조회 실패
 */
public class OrderNotFoundException extends BaseException {

  /**
   * 주문 ID로 예외 생성
   *
   * @param orderId 주문 ID
   */
  public OrderNotFoundException(Long orderId) {
    super(ErrorCode.ORDER_NOT_FOUND, "주문을 찾을 수 없습니다. ID: " + orderId);
  }

  /**
   * 주문번호로 예외 생성
   *
   * @param orderNumber 주문번호 (Domain Value Object)
   */
  public OrderNotFoundException(OrderNumber orderNumber) {
    super(
        ErrorCode.ORDER_NOT_FOUND, "주문을 찾을 수 없습니다. 주문번호: " + orderNumber.value());
  }
}
