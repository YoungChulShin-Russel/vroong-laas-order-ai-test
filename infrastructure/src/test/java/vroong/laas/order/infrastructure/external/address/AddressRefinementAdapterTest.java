package vroong.laas.order.infrastructure.external.address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vroong.laas.order.core.domain.address.exception.AddressRefineFailedException;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.LatLng;
import vroong.laas.order.infrastructure.external.address.provider.KakaoReverseGeocodingProvider;
import vroong.laas.order.infrastructure.external.address.provider.NaverReverseGeocodingProvider;
import vroong.laas.order.infrastructure.external.address.provider.NeogeoReverseGeocodingProvider;

@ExtendWith(MockitoExtension.class)
class AddressRefinementAdapterTest {

  @InjectMocks private AddressRefinementAdapter addressRefinementAdapter;

  @Mock private NeogeoReverseGeocodingProvider neogeoProvider;

  @Mock private NaverReverseGeocodingProvider naverProvider;

  @Mock private KakaoReverseGeocodingProvider kakaoProvider;

  private FixtureMonkey fixtureMonkey;

  @BeforeEach
  void setUp() {
    fixtureMonkey =
        FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .defaultNotNull(true)
            .build();

    // Fallback 순서: Neogeo → Naver → Kakao
    addressRefinementAdapter =
        new AddressRefinementAdapter(List.of(neogeoProvider, naverProvider, kakaoProvider));
  }

  @Test
  @DisplayName("첫 번째 Provider(Neogeo)가 성공하면 즉시 반환")
  void first_provider_success() {
    // given
    LatLng latLng = randomLatLng();
    Address originalAddress = randomAddress();
    Address refinedAddress =
        new Address("서울시 강남구 역삼동 123", "서울시 강남구 테헤란로 123", "1층");

    given(neogeoProvider.getProviderName()).willReturn("NEOGEO");
    given(neogeoProvider.reverseGeocode(latLng)).willReturn(refinedAddress);

    // when
    Address result = addressRefinementAdapter.refineByReverseGeocoding(latLng, originalAddress);

    // then
    assertThat(result).isEqualTo(refinedAddress);
    verify(neogeoProvider, times(1)).reverseGeocode(latLng);
    verify(naverProvider, never()).reverseGeocode(any()); // Fallback 안 함
    verify(kakaoProvider, never()).reverseGeocode(any());
  }

  @Test
  @DisplayName("첫 번째 Provider 실패 시 두 번째 Provider(Naver)로 Fallback")
  void first_provider_fail_fallback_to_second() {
    // given
    LatLng latLng = randomLatLng();
    Address originalAddress = randomAddress();
    Address refinedAddress =
        new Address("서울시 서초구 서초동 456", "서울시 서초구 서초대로 456", "2층");

    given(neogeoProvider.getProviderName()).willReturn("NEOGEO");
    given(neogeoProvider.reverseGeocode(latLng))
        .willThrow(new RuntimeException("Neogeo API 미구현 (Stub)"));

    given(naverProvider.getProviderName()).willReturn("NAVER");
    given(naverProvider.reverseGeocode(latLng)).willReturn(refinedAddress);

    // when
    Address result = addressRefinementAdapter.refineByReverseGeocoding(latLng, originalAddress);

    // then
    assertThat(result).isEqualTo(refinedAddress);
    verify(neogeoProvider, times(1)).reverseGeocode(latLng);
    verify(naverProvider, times(1)).reverseGeocode(latLng);
    verify(kakaoProvider, never()).reverseGeocode(any()); // 세 번째는 시도 안 함
  }

  @Test
  @DisplayName("두 번째 Provider도 실패 시 세 번째 Provider(Kakao)로 Fallback")
  void second_provider_fail_fallback_to_third() {
    // given
    LatLng latLng = randomLatLng();
    Address originalAddress = randomAddress();
    Address refinedAddress =
        new Address("서울시 송파구 잠실동 789", "서울시 송파구 올림픽로 789", "3층");

    given(neogeoProvider.getProviderName()).willReturn("NEOGEO");
    given(neogeoProvider.reverseGeocode(latLng))
        .willThrow(new RuntimeException("Neogeo API 미구현"));

    given(naverProvider.getProviderName()).willReturn("NAVER");
    given(naverProvider.reverseGeocode(latLng)).willThrow(new RuntimeException("Naver API 실패"));

    given(kakaoProvider.getProviderName()).willReturn("KAKAO");
    given(kakaoProvider.reverseGeocode(latLng)).willReturn(refinedAddress);

    // when
    Address result = addressRefinementAdapter.refineByReverseGeocoding(latLng, originalAddress);

    // then
    assertThat(result).isEqualTo(refinedAddress);
    verify(neogeoProvider, times(1)).reverseGeocode(latLng);
    verify(naverProvider, times(1)).reverseGeocode(latLng);
    verify(kakaoProvider, times(1)).reverseGeocode(latLng);
  }

  @Test
  @DisplayName("모든 Provider 실패 시 AddressRefineFailedException 발생")
  void all_providers_fail() {
    // given
    LatLng latLng = randomLatLng();
    Address originalAddress = randomAddress();

    given(neogeoProvider.getProviderName()).willReturn("NEOGEO");
    given(neogeoProvider.reverseGeocode(latLng))
        .willThrow(new RuntimeException("Neogeo API 미구현"));

    given(naverProvider.getProviderName()).willReturn("NAVER");
    given(naverProvider.reverseGeocode(latLng)).willThrow(new RuntimeException("Naver API 실패"));

    given(kakaoProvider.getProviderName()).willReturn("KAKAO");
    given(kakaoProvider.reverseGeocode(latLng)).willThrow(new RuntimeException("Kakao API 실패"));

    // when & then
    assertThatThrownBy(
            () -> addressRefinementAdapter.refineByReverseGeocoding(latLng, originalAddress))
        .isInstanceOf(AddressRefineFailedException.class)
        .hasMessageContaining("모든 역지오코딩 서비스가 실패했습니다");

    verify(neogeoProvider, times(1)).reverseGeocode(latLng);
    verify(naverProvider, times(1)).reverseGeocode(latLng);
    verify(kakaoProvider, times(1)).reverseGeocode(latLng);
  }

  // Helper methods
  private LatLng randomLatLng() {
    return fixtureMonkey.giveMeOne(LatLng.class);
  }

  private Address randomAddress() {
    return fixtureMonkey.giveMeOne(Address.class);
  }
}

