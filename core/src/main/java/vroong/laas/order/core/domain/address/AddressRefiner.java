package vroong.laas.order.core.domain.address;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vroong.laas.order.core.domain.address.required.AddressRefinementClient;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.Origin;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.LatLng;

/**
 * 주소 정제 Domain Service
 *
 * <p>위/경도 좌표를 기반으로 역지오코딩하여 주소를 정제합니다.
 *
 * <p>책임:
 * - 위/경도 → 정제된 주소 변환
 * - Origin/Destination 주소 정제
 *
 * <p>사용처:
 * - OrderFacade.createOrder(): Origin/Destination 주소 정제
 *
 * <p>Fallback Chain:
 * - AddressRefinementClient (Port)에서 처리
 * - 순서는 application.yml 설정으로 변경 가능 (기본: Neogeo → Naver → Kakao)
 *
 * <p>로그:
 * - Infrastructure Layer (AddressRefinementAdapter)에서 기록
 */
@Service
@RequiredArgsConstructor
public class AddressRefiner {

  private final AddressRefinementClient addressRefinementClient;

  /**
   * 위/경도 좌표를 기반으로 주소를 정제합니다.
   *
   * <p>AddressRefinementClient를 통해 역지오코딩을 수행합니다.
   * 로그는 Infrastructure Layer에서 기록됩니다.
   *
   * @param latLng 위/경도 좌표
   * @param originalAddress 원본 주소 (로그용)
   * @return 정제된 주소
   * @throws vroong.laas.order.core.domain.address.exception.AddressRefineFailedException 역지오코딩 실패 시
   */
  public Address refine(LatLng latLng, Address originalAddress) {
    return addressRefinementClient.refineByReverseGeocoding(latLng, originalAddress);
  }

  /**
   * Origin 주소를 정제합니다.
   *
   * <p>역지오코딩을 통해 정제된 주소로 Origin을 재생성합니다.
   *
   * @param origin 원본 Origin
   * @return 정제된 주소가 적용된 Origin
   * @throws vroong.laas.order.core.domain.address.exception.AddressRefineFailedException 역지오코딩 실패 시
   */
  public Origin refineOrigin(Origin origin) {
    Address refinedAddress = refine(origin.latLng(), origin.address());
    return new Origin(origin.contact(), refinedAddress, origin.latLng(), origin.entranceInfo());
  }

  /**
   * Destination 주소를 정제합니다.
   *
   * <p>역지오코딩을 통해 정제된 주소로 Destination을 재생성합니다.
   *
   * @param destination 원본 Destination
   * @return 정제된 주소가 적용된 Destination
   * @throws vroong.laas.order.core.domain.address.exception.AddressRefineFailedException 역지오코딩 실패 시
   */
  public Destination refineDestination(Destination destination) {
    Address refinedAddress = refine(destination.latLng(), destination.address());
    return new Destination(
        destination.contact(), refinedAddress, destination.latLng(), destination.entranceInfo());
  }
}

