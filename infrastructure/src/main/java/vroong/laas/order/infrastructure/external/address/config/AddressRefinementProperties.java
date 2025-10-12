package vroong.laas.order.infrastructure.external.address.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 주소 정제 설정 Properties
 *
 * <p>application.yml의 {@code address.refinement} 설정을 바인딩합니다.
 *
 * <p>설정 예시:
 *
 * <pre>{@code
 * address:
 *   refinement:
 *     fallback-order:
 *       - neogeo
 *       - naver
 *       - kakao
 * }</pre>
 *
 * @param fallbackOrder 역지오코딩 Provider의 Fallback 순서 (예: neogeo, naver, kakao)
 */
@ConfigurationProperties(prefix = "address.refinement")
public record AddressRefinementProperties(List<String> fallbackOrder) {

  /**
   * 생성자 검증
   *
   * @throws IllegalArgumentException fallbackOrder가 비어있거나 null인 경우
   */
  public AddressRefinementProperties {
    if (fallbackOrder == null || fallbackOrder.isEmpty()) {
      throw new IllegalArgumentException("fallback-order는 최소 1개 이상의 Provider가 필요합니다");
    }
  }
}

