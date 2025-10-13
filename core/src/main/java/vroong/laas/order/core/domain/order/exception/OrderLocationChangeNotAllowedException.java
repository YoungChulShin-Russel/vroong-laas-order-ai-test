package vroong.laas.order.core.domain.order.exception;

import vroong.laas.order.core.common.exception.BaseException;
import vroong.laas.order.core.common.exception.ErrorCode;

/**
 * 주문 위치 정보를 변경할 수 없을 때 발생하는 예외
 *
 * <p>사용 시나리오:
 * - CREATED 상태가 아닌 주문의 도착지 변경 시도
 * - CREATED 상태가 아닌 주문의 출발지 변경 시도
 */
public class OrderLocationChangeNotAllowedException extends BaseException {

  /**
   * 기본 메시지로 예외 생성
   *
   * @param message 상세 메시지
   */
  public OrderLocationChangeNotAllowedException(String message) {
    super(ErrorCode.ORDER_LOCATION_CHANGE_NOT_ALLOWED, message);
  }
}

