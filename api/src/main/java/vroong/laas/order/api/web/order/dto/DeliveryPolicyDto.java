package vroong.laas.order.api.web.order.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import vroong.laas.order.core.domain.order.DeliveryPolicy;

/**
 * 배송 정책 DTO
 */
public record DeliveryPolicyDto(
    @NotNull(message = "주류 배송 여부는 필수입니다")
    Boolean alcoholDelivery,

    @NotNull(message = "비대면 배송 여부는 필수입니다")
    Boolean contactlessDelivery,

    @NotNull(message = "예약 배송 여부는 필수입니다")
    Boolean reservedDelivery,

    Instant reservedDeliveryStartTime,  // 선택
    Instant pickupRequestTime           // 선택
) {

  /** DeliveryPolicy Domain → DeliveryPolicyDto 변환 */
  public static DeliveryPolicyDto from(DeliveryPolicy policy) {
    return new DeliveryPolicyDto(
        policy.alcoholDelivery(),
        policy.contactlessDelivery(),
        policy.reservedDelivery(),
        policy.reservedDeliveryStartTime(),
        policy.pickupRequestTime());
  }

  /** DeliveryPolicyDto → DeliveryPolicy Domain 변환 */
  public DeliveryPolicy toDomain() {
    return new DeliveryPolicy(
        alcoholDelivery,
        contactlessDelivery,
        reservedDelivery,
        reservedDeliveryStartTime,
        pickupRequestTime);
  }
}
