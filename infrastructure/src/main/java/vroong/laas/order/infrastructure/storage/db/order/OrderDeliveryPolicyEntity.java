package vroong.laas.order.infrastructure.storage.db.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vroong.laas.order.core.domain.order.DeliveryPolicy;
import vroong.laas.order.infrastructure.common.util.JsonUtil;
import vroong.laas.order.infrastructure.storage.db.BaseEntity;

@Entity
@Table(name = "order_delivery_policies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderDeliveryPolicyEntity extends BaseEntity {

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "delivery_policy_json", nullable = false, columnDefinition = "TEXT")
  private String deliveryPolicyJson;

  @Builder
  public OrderDeliveryPolicyEntity(Long orderId, String deliveryPolicyJson) {
    this.orderId = orderId;
    this.deliveryPolicyJson = deliveryPolicyJson;
  }

  // Domain → Entity
  public static OrderDeliveryPolicyEntity from(DeliveryPolicy policy, Long orderId) {
    String json = JsonUtil.toJson(policy);
    return new OrderDeliveryPolicyEntity(orderId, json);
  }

  // Entity → Domain
  public DeliveryPolicy toDomain() {
    return JsonUtil.fromJson(deliveryPolicyJson, DeliveryPolicy.class);
  }
}

