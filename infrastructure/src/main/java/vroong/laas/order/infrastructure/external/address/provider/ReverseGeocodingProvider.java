package vroong.laas.order.infrastructure.external.address.provider;

import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.LatLng;

/**
 * 역지오코딩 Provider 공통 인터페이스
 *
 * <p>위/경도 좌표를 주소로 변환하는 Provider들의 공통 인터페이스입니다.
 *
 * <p>구현체:
 * - NeogeoReverseGeocodingProvider: 내부 Neogeo 서비스
 * - NaverReverseGeocodingProvider: Naver Geocoding API
 * - KakaoReverseGeocodingProvider: Kakao Local API
 *
 * <p>Fallback Chain:
 * - AddressRefinementAdapter에서 설정 순서대로 Provider 시도
 * - 한 Provider 실패 시 다음 Provider로 Fallback
 */
public interface ReverseGeocodingProvider {

  /**
   * Provider 이름 반환 (로그용)
   *
   * @return Provider 이름 (예: "NEOGEO", "NAVER", "KAKAO")
   */
  String getProviderName();

  /**
   * 위/경도 좌표를 주소로 변환 (역지오코딩)
   *
   * @param latLng 위/경도 좌표
   * @return 변환된 주소
   * @throws Exception 역지오코딩 실패 시 (4xx, 5xx, Timeout 등)
   */
  Address reverseGeocode(LatLng latLng);
}

