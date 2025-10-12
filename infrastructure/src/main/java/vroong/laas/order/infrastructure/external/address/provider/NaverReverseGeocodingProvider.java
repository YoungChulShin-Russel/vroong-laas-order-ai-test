package vroong.laas.order.infrastructure.external.address.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.LatLng;

/**
 * Naver 역지오코딩 Provider (외부 서비스)
 *
 * <p>Naver Geocoding API를 통한 역지오코딩 구현체입니다.
 *
 * <p>현재 상태: Stub (API 미구현)
 * - 실제 Feign Client 구현 전까지는 예외 발생
 * - Fallback Chain 동작 확인용
 *
 * <p>TODO: Feign Client 구현
 * - NaverGeocodeFeignClient 생성
 * - Request/Response DTO 정의
 * - Client ID/Secret 설정
 * - Timeout, Retry 설정
 *
 * <p>API 문서: https://api.ncloud-docs.com/docs/ai-naver-mapsreversegeocoding
 */
@Component
@Slf4j
public class NaverReverseGeocodingProvider implements ReverseGeocodingProvider {

  @Override
  public String getProviderName() {
    return "NAVER";
  }

  /**
   * Naver 역지오코딩 (Stub - 성공 응답)
   *
   * <p>TODO: Feign Client로 Naver Geocoding API 호출 구현
   *
   * @param latLng 위/경도 좌표
   * @return 변환된 주소
   */
  @Override
  public Address reverseGeocode(LatLng latLng) {
    log.info("[NAVER] 역지오코딩 요청 (Stub - 성공): latLng={}", latLng);

    // TODO: 실제 Naver Geocoding API 호출 구현
    // NaverReverseGeocodeResponse response = naverGeocodeFeignClient.reverseGeocode(...);
    // return toAddress(response);

    // ⭐ Stub 구현: Fallback Chain 테스트를 위해 성공 응답 반환
    return new Address(
        "서울특별시 강남구 역삼동 123-45",  // 지번 주소
        "서울특별시 강남구 테헤란로 123",    // 도로명 주소
        null                                  // 상세 주소 (역지오코딩에서는 없음)
    );
  }
}

