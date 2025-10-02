package vroong.laas.order.core.domain.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
  CREATED("생성"),
  DELIVERING("배송중"),
  DELIVERED("배송완료"),
  CANCELLED("취소");

  private final String description;
}

