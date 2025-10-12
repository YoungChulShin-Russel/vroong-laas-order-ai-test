package vroong.laas.order.infrastructure.external.address;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import vroong.laas.order.core.domain.address.exception.AddressRefineFailedException;
import vroong.laas.order.core.domain.address.required.AddressRefinementClient;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.LatLng;
import vroong.laas.order.infrastructure.external.address.provider.ReverseGeocodingProvider;

/**
 * 주소 정제 Adapter (Fallback Chain 구현)
 *
 * <p>AddressRefinementClient Port의 구현체입니다.
 *
 * <p>Fallback Chain:
 * - 설정된 순서대로 ReverseGeocodingProvider 시도
 * - 한 Provider 실패 시 다음 Provider로 Fallback
 * - 모든 Provider 실패 시 AddressRefineFailedException 발생
 *
 * <p>Fallback 조건:
 * - 모든 Exception (4xx, 5xx, Timeout, 네트워크 에러 등)
 *
 * <p>로그:
 * - 각 Provider 시도/성공/실패 로그 기록
 * - 원본 주소 → 정제된 주소 변환 로그
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class AddressRefinementAdapter implements AddressRefinementClient {

  private final List<ReverseGeocodingProvider> providers;

  /**
   * 위/경도 좌표를 기반으로 역지오코딩하여 정제된 주소를 반환합니다.
   *
   * <p>Fallback Chain:
   * 1. providers 리스트 순서대로 역지오코딩 시도
   * 2. 성공 시 즉시 반환
   * 3. 실패 시 다음 Provider로 Fallback
   * 4. 모든 Provider 실패 시 AddressRefineFailedException 발생
   *
   * @param latLng 위/경도 좌표
   * @param originalAddress 원본 주소 (로그용)
   * @return 정제된 주소
   * @throws AddressRefineFailedException 모든 역지오코딩 서비스가 실패한 경우
   */
  @Override
  public Address refineByReverseGeocoding(LatLng latLng, Address originalAddress) {
    log.info(
        "[주소정제] 시작: latLng={}, original=[지번:{}, 도로명:{}]",
        latLng,
        originalAddress.jibnunAddress(),
        originalAddress.roadAddress());

    AddressRefineFailedException lastException = null;

    for (ReverseGeocodingProvider provider : providers) {
      try {
        log.info("[주소정제] Provider 시도: provider={}, latLng={}", provider.getProviderName(), latLng);

        Address refined = provider.reverseGeocode(latLng);

        log.info(
            "[주소정제] 성공: provider={}, original=[도로명:{}] → refined=[도로명:{}]",
            provider.getProviderName(),
            originalAddress.roadAddress(),
            refined.roadAddress());

        return refined;

      } catch (Exception e) {
        log.warn(
            "[주소정제] 실패: provider={}, latLng={}, error={}",
            provider.getProviderName(),
            latLng,
            e.getMessage());

        lastException =
            new AddressRefineFailedException(
                String.format(
                    "주소 정제 실패 - Provider: %s, LatLng: %s",
                    provider.getProviderName(), latLng),
                e);
      }
    }

    // 모든 Provider 실패
    log.error(
        "[주소정제] 모든 Provider 실패: latLng={}, original=[도로명:{}], providers={}",
        latLng,
        originalAddress.roadAddress(),
        providers.stream().map(ReverseGeocodingProvider::getProviderName).toList());

    throw new AddressRefineFailedException(
        String.format("모든 역지오코딩 서비스가 실패했습니다 - LatLng: %s", latLng), lastException);
  }
}

