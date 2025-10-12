package vroong.laas.order.core.domain.address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vroong.laas.order.core.domain.address.exception.AddressRefineFailedException;
import vroong.laas.order.core.domain.address.required.AddressRefinementClient;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.LatLng;
import vroong.laas.order.core.fixture.OrderFixtures;

@ExtendWith(MockitoExtension.class)
class AddressRefinerTest {

  @InjectMocks private AddressRefiner addressRefiner;

  @Mock private AddressRefinementClient addressRefinementClient;

  private OrderFixtures orderFixtures;

  @BeforeEach
  void setUp() {
    FixtureMonkey fixtureMonkey =
        FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .defaultNotNull(true)
            .build();

    orderFixtures = new OrderFixtures(fixtureMonkey);
  }

  @Test
  @DisplayName("주소 정제 성공")
  void refine_success() {
    // given
    LatLng latLng = orderFixtures.randomLatLng();
    Address originalAddress = orderFixtures.randomAddress();
    Address refinedAddress =
        new Address("서울시 강남구 역삼동 123", "서울시 강남구 테헤란로 123", "1층");

    given(addressRefinementClient.refineByReverseGeocoding(eq(latLng), eq(originalAddress)))
        .willReturn(refinedAddress);

    // when
    Address result = addressRefiner.refine(latLng, originalAddress);

    // then
    assertThat(result).isEqualTo(refinedAddress);
    assertThat(result.roadAddress()).isEqualTo("서울시 강남구 테헤란로 123");
    verify(addressRefinementClient).refineByReverseGeocoding(latLng, originalAddress);
  }

  @Test
  @DisplayName("주소 정제 실패 시 AddressRefineFailedException 발생")
  void refine_fail() {
    // given
    LatLng latLng = orderFixtures.randomLatLng();
    Address originalAddress = orderFixtures.randomAddress();

    given(addressRefinementClient.refineByReverseGeocoding(any(), any()))
        .willThrow(new AddressRefineFailedException("모든 역지오코딩 서비스가 실패했습니다"));

    // when & then
    assertThatThrownBy(() -> addressRefiner.refine(latLng, originalAddress))
        .isInstanceOf(AddressRefineFailedException.class)
        .hasMessageContaining("모든 역지오코딩 서비스가 실패했습니다");
  }
}

