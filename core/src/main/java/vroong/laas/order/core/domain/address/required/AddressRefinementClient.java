package vroong.laas.order.core.domain.address.required;

import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.LatLng;

/**
 * 주소 정제 Client Port
 *
 * <p>위/경도 좌표를 기반으로 역지오코딩하여 정제된 주소를 반환합니다.
 *
 * <p>Fallback Chain:
 * - 여러 역지오코딩 Provider를 순차적으로 시도
 * - 순서는 application.yml의 fallback-order 설정으로 변경 가능
 * - 지원 Provider: Neogeo (내부), Naver (외부), Kakao (외부)
 * - 모두 실패 시: AddressRefineFailedException 발생
 *
 * <p>Fallback 조건:
 * - HTTP 4xx, 5xx 에러
 * - Timeout (application.yml 설정, 기본: 3초)
 * - 네트워크 에러
 *
 * <p>Infrastructure에서 Adapter로 구현됩니다.
 * - AddressRefinementAdapter: Fallback Chain 구현
 * - ReverseGeocodingProvider: 각 Provider별 구현
 */
public interface AddressRefinementClient {

  /**
   * 위/경도 좌표를 기반으로 역지오코딩하여 정제된 주소를 반환합니다.
   *
   * <p>동작:
   * - application.yml의 fallback-order 순서대로 Provider 시도
   * - 각 Provider 실패 시 다음 Provider로 Fallback
   * - 모두 실패 시 AddressRefineFailedException 발생
   *
   * @param latLng 위/경도 좌표
   * @param originalAddress 원본 주소 (로그용)
   * @return 정제된 주소
   * @throws vroong.laas.order.core.domain.address.exception.AddressRefineFailedException 모든 역지오코딩 서비스가 실패한 경우
   */
  Address refineByReverseGeocoding(LatLng latLng, Address originalAddress);
}

