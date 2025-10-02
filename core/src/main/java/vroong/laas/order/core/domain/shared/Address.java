package vroong.laas.order.core.domain.shared;

public record Address(String jibnunAddress, String roadAddress, String detailAddress) {

  public Address {
    if ((jibnunAddress == null || jibnunAddress.isBlank())
        && (roadAddress == null || roadAddress.isBlank())) {
      throw new IllegalArgumentException("지번 주소 또는 도로명 주소 중 하나는 필수입니다");
    }
  }
}

