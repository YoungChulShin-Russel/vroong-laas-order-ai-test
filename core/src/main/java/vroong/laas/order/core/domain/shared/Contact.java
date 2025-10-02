package vroong.laas.order.core.domain.shared;

public record Contact(String name, String phoneNumber) {

  public Contact {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("연락처명은 필수입니다");
    }
    if (phoneNumber == null || phoneNumber.isBlank()) {
      throw new IllegalArgumentException("전화번호는 필수입니다");
    }
  }
}

