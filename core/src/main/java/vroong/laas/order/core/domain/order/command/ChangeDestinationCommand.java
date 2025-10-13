package vroong.laas.order.core.domain.order.command;

import vroong.laas.order.core.domain.order.Destination;

/**
 * 도착지 변경 Command
 *
 * <p>주문의 도착지를 변경하기 위한 Command입니다.
 *
 * <p>특징:
 * - 불변 객체 (record)
 * - Domain Layer에 위치
 * - 필수 값 검증
 */
public record ChangeDestinationCommand(Long orderId, Destination newDestination) {

  /**
   * Compact Constructor (필수 값 검증)
   */
  public ChangeDestinationCommand {
    if (orderId == null) {
      throw new IllegalArgumentException("주문 ID는 필수입니다");
    }
    if (newDestination == null) {
      throw new IllegalArgumentException("새 도착지는 필수입니다");
    }
  }
}

