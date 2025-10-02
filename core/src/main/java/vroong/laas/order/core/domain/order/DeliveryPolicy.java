package vroong.laas.order.core.domain.order;

import java.time.Instant;

public record DeliveryPolicy(
    boolean alcoholDelivery,
    boolean contactlessDelivery,
    boolean reservedDelivery,
    Instant reservedDeliveryStartTime,
    Instant pickupRequestTime) {

  public DeliveryPolicy {
    if (reservedDelivery && reservedDeliveryStartTime == null) {
      throw new IllegalArgumentException("예약 배송인 경우 예약 시작 시간은 필수입니다");
    }
    if (pickupRequestTime == null) {
      throw new IllegalArgumentException("픽업 요청 시간은 필수입니다");
    }
  }
}


