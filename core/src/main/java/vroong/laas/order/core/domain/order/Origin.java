package vroong.laas.order.core.domain.order;

import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.Contact;
import vroong.laas.order.core.domain.shared.LatLng;

public record Origin(
    Contact contact, Address address, LatLng latLng, EntranceInfo entranceInfo) {

  public Origin {
    if (contact == null) {
      throw new IllegalArgumentException("출발지 연락처는 필수입니다");
    }
    if (address == null) {
      throw new IllegalArgumentException("출발지 주소는 필수입니다");
    }
    if (latLng == null) {
      throw new IllegalArgumentException("출발지 좌표는 필수입니다");
    }
    if (entranceInfo == null) {
      entranceInfo = EntranceInfo.empty();
    }
  }
}


