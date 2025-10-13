package vroong.laas.order.core.domain.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.order.core.domain.order.required.OrderRepository;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.LatLng;

/**
 * 주문 위치 정보 변경 Domain Service
 *
 * <p>주문의 출발지/도착지 변경을 담당합니다.
 *
 * <p>책임:
 * - 도착지 주소 변경 (현재 구현)
 * - 출발지 변경 (향후 추가 예정)
 * - 상태 검증 (CREATED만 허용)
 * - 도메인 이벤트 발행
 *
 * <p>주의:
 * - 주소 정제는 Facade에서 수행 (인프라 의존적 처리)
 * - 이 서비스는 정제된 주소를 받아서 순수 비즈니스 로직만 수행
 *
 * <p>사용처:
 * - OrderFacade.changeDestinationAddress()
 */
@Service
@RequiredArgsConstructor
public class OrderLocationChanger {

  private final OrderRepository orderRepository;
  // TODO: private final OutboxEventAppender outboxEventAppender;

  /**
   * 도착지 주소 변경
   *
   * <p>비즈니스 규칙:
   * - CREATED 상태에서만 변경 가능
   * - 도메인 이벤트 발행 (OrderDestinationAddressChangedEvent)
   *
   * <p>변경 범위:
   * - Address (주소)
   * - LatLng (위경도)
   * - EntranceInfo (출입 가이드)
   *
   * <p>유지되는 것:
   * - Contact (연락처) - 변경되지 않음
   *
   * <p>주의:
   * - Order 조회는 Facade에서 완료 (빠른 실패)
   * - 주소 정제는 Facade에서 완료 (불필요한 외부 API 호출 방지)
   *
   * @param order Order (Facade에서 조회 완료)
   * @param refinedAddress 정제된 주소 (Facade에서 주소 정제 완료)
   * @param refinedLatLng 정제된 위경도
   * @param refinedEntranceInfo 정제된 출입 정보
   * @return 변경된 Order
   * @throws vroong.laas.order.core.domain.order.exception.OrderLocationChangeNotAllowedException CREATED 상태가 아님
   */
  @Transactional
  public Order changeDestinationAddress(
      Order order,
      Address refinedAddress,
      LatLng refinedLatLng,
      EntranceInfo refinedEntranceInfo) {
    // 1. 도착지 주소 변경 (상태 검증 포함, 도메인 이벤트 추가)
    order.changeDestinationAddress(refinedAddress, refinedLatLng, refinedEntranceInfo);

    // 2. DB 업데이트
    orderRepository.updateDestinationAddress(
        order.getId(), refinedAddress, refinedLatLng, refinedEntranceInfo);

    // TODO: 3. Outbox 이벤트 발행
    // Kafka 라이브러리 업데이트 후 구현 예정
    // outboxEventAppender.append(
    //     OutboxEventType.ORDER_DESTINATION_ADDRESS_CHANGED,
    //     order
    // );

    return order;
  }

  // TODO: 향후 추가 예정
  // @Transactional
  // public Order changeOrigin(Long orderId, Origin newOrigin) {
  //     // 동일한 패턴
  // }
}

