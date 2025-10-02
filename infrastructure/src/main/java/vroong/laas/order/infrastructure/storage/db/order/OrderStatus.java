package vroong.laas.order.infrastructure.storage.db.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
  CREATED("생성"),
  DELIVERED("배송완료"),
  CANCELLED("취소");

  private final String description;
}

