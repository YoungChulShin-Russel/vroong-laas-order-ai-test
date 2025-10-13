package vroong.laas.order.core.domain.order.command;

import vroong.laas.order.core.domain.order.EntranceInfo;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.LatLng;

/**
 * 도착지 주소 변경 Command
 *
 * <p>주문의 도착지 주소를 변경하기 위한 Command입니다.
 *
 * <p>변경 범위:
 * - Address (주소)
 * - LatLng (위경도)
 * - EntranceInfo (출입 가이드)
 *
 * <p>유지되는 것:
 * - Contact (연락처) - 변경되지 않음
 *
 * <p>특징:
 * - 불변 객체 (record)
 * - Domain Layer에 위치
 * - 필수 값 검증
 */
public record ChangeDestinationAddressCommand(
    Long orderId, Address newAddress, LatLng newLatLng, EntranceInfo newEntranceInfo) {

  /**
   * Compact Constructor (필수 값 검증)
   */
  public ChangeDestinationAddressCommand {
    if (orderId == null) {
      throw new IllegalArgumentException("주문 ID는 필수입니다");
    }
    if (newAddress == null) {
      throw new IllegalArgumentException("새 주소는 필수입니다");
    }
    if (newLatLng == null) {
      throw new IllegalArgumentException("새 위경도는 필수입니다");
    }
    if (newEntranceInfo == null) {
      throw new IllegalArgumentException("새 출입 정보는 필수입니다");
    }
  }
}

