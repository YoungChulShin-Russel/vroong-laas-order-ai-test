package vroong.laas.order.infrastructure.external.address.config;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vroong.laas.order.infrastructure.external.address.provider.KakaoReverseGeocodingProvider;
import vroong.laas.order.infrastructure.external.address.provider.NaverReverseGeocodingProvider;
import vroong.laas.order.infrastructure.external.address.provider.NeogeoReverseGeocodingProvider;
import vroong.laas.order.infrastructure.external.address.provider.ReverseGeocodingProvider;

/**
 * 주소 정제 설정
 *
 * <p>역지오코딩 Provider의 Fallback 순서를 설정합니다.
 *
 * <p>설정 방법:
 * - application.yml의 address.refinement.fallback-order 설정
 * - 순서대로 Provider를 시도 (첫 번째가 최우선)
 *
 * <p>예시:
 * <pre>
 * address:
 *   refinement:
 *     fallback-order:
 *       - neogeo
 *       - naver
 *       - kakao
 * </pre>
 */
@Configuration
@EnableConfigurationProperties(AddressRefinementProperties.class)
@RequiredArgsConstructor
@Slf4j
public class AddressRefinementConfig {

  private final AddressRefinementProperties properties;

  /**
   * 역지오코딩 Provider 리스트를 Fallback 순서대로 반환
   *
   * <p>application.yml의 fallback-order 설정 순서대로 Provider를 정렬합니다.
   *
   * @param neogeo Neogeo Provider
   * @param naver Naver Provider
   * @param kakao Kakao Provider
   * @return Fallback 순서대로 정렬된 Provider 리스트
   * @throws IllegalArgumentException 설정에 알 수 없는 Provider 이름이 있는 경우
   */
  @Bean
  public List<ReverseGeocodingProvider> reverseGeocodingProviders(
      NeogeoReverseGeocodingProvider neogeo,
      NaverReverseGeocodingProvider naver,
      KakaoReverseGeocodingProvider kakao) {

    Map<String, ReverseGeocodingProvider> providerMap =
        Map.of(
            "neogeo", neogeo,
            "naver", naver,
            "kakao", kakao);

    List<ReverseGeocodingProvider> providers =
        properties.fallbackOrder().stream()
            .map(
                name -> {
                  ReverseGeocodingProvider provider = providerMap.get(name.toLowerCase());
                  if (provider == null) {
                    throw new IllegalArgumentException(
                        String.format(
                            "알 수 없는 역지오코딩 Provider: %s (지원: neogeo, naver, kakao)", name));
                  }
                  return provider;
                })
            .toList();

    log.info(
        "[AddressRefinementConfig] Fallback 순서 설정 완료: {}",
        providers.stream().map(ReverseGeocodingProvider::getProviderName).toList());

    return providers;
  }
}

