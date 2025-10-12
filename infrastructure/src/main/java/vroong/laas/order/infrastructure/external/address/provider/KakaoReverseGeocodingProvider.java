package vroong.laas.order.infrastructure.external.address.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.LatLng;

/**
 * Kakao 역지오코딩 Provider (외부 서비스)
 *
 * <p>Kakao Local API를 통한 역지오코딩 구현체입니다.
 *
 * <p>현재 상태: Stub (API 미구현)
 * - 실제 Feign Client 구현 전까지는 예외 발생
 * - Fallback Chain 동작 확인용
 *
 * <p>TODO: Feign Client 구현
 * - KakaoLocalFeignClient 생성
 * - Request/Response DTO 정의
 * - API Key 설정
 * - Timeout, Retry 설정
 *
 * <p>API 문서: https://developers.kakao.com/docs/latest/ko/local/dev-guide#coord-to-address
 */
@Component
@Slf4j
public class KakaoReverseGeocodingProvider implements ReverseGeocodingProvider {

  @Override
  public String getProviderName() {
    return "KAKAO";
  }

  /**
   * Kakao 역지오코딩 (현재 Stub)
   *
   * <p>TODO: Feign Client로 Kakao Local API 호출 구현
   *
   * @param latLng 위/경도 좌표
   * @return 변환된 주소
   * @throws RuntimeException Stub이므로 항상 예외 발생 (Fallback 테스트용)
   */
  @Override
  public Address reverseGeocode(LatLng latLng) {
    log.debug("[KAKAO] 역지오코딩 요청: latLng={}", latLng);

    // TODO: 실제 Kakao Local API 호출 구현
    // KakaoCoord2AddressResponse response = kakaoLocalFeignClient.coord2Address(...);
    // return toAddress(response);

    throw new RuntimeException("Kakao API 미구현 (Stub)");
  }
}

