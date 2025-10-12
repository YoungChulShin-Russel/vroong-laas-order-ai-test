package vroong.laas.order.infrastructure.external.address.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.LatLng;

/**
 * Neogeo 역지오코딩 Provider (내부 서비스)
 *
 * <p>Vroong 내부 Neogeo 서비스를 통한 역지오코딩 구현체입니다.
 *
 * <p>현재 상태: Stub (API 미구현)
 * - 실제 Feign Client 구현 전까지는 예외 발생
 * - Fallback Chain 동작 확인용
 *
 * <p>TODO: Feign Client 구현
 * - NeogeoFeignClient 생성
 * - Request/Response DTO 정의
 * - Timeout, Retry 설정
 */
@Component
@Slf4j
public class NeogeoReverseGeocodingProvider implements ReverseGeocodingProvider {

  @Override
  public String getProviderName() {
    return "NEOGEO";
  }

  /**
   * Neogeo 역지오코딩 (현재 Stub)
   *
   * <p>TODO: Feign Client로 Neogeo API 호출 구현
   *
   * @param latLng 위/경도 좌표
   * @return 변환된 주소
   * @throws RuntimeException Stub이므로 항상 예외 발생 (Fallback 테스트용)
   */
  @Override
  public Address reverseGeocode(LatLng latLng) {
    log.debug("[NEOGEO] 역지오코딩 요청: latLng={}", latLng);

    // TODO: 실제 Neogeo API 호출 구현
    // NeogeoResponse response = neogeoFeignClient.reverseGeocode(...);
    // return toAddress(response);

    throw new RuntimeException("Neogeo API 미구현 (Stub)");
  }
}

