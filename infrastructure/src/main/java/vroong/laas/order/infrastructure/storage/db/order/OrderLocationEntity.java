package vroong.laas.order.infrastructure.storage.db.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vroong.laas.order.infrastructure.storage.db.BaseEntity;

@Entity
@Table(name = "order_locations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderLocationEntity extends BaseEntity {

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  // 출발지 정보
  @Column(name = "origin_contact_name", length = 100)
  private String originContactName;

  @Column(name = "origin_contact_phone_number", length = 20)
  private String originContactPhoneNumber;

  @Column(name = "origin_entrance_password", length = 50)
  private String originEntrancePassword;

  @Column(name = "origin_entrance_guide", length = 500)
  private String originEntranceGuide;

  @Column(name = "origin_request_message", length = 1000)
  private String originRequestMessage;

  @Column(name = "origin_latitude", precision = 10, scale = 7)
  private BigDecimal originLatitude;

  @Column(name = "origin_longitude", precision = 10, scale = 7)
  private BigDecimal originLongitude;

  @Column(name = "origin_jibnun_address", length = 300)
  private String originJibnunAddress;

  @Column(name = "origin_road_address", length = 300)
  private String originRoadAddress;

  @Column(name = "origin_detail_address", length = 300)
  private String originDetailAddress;

  // 도착지 정보
  @Column(name = "destination_contact_name", length = 100)
  private String destinationContactName;

  @Column(name = "destination_contact_phone_number", length = 20)
  private String destinationContactPhoneNumber;

  @Column(name = "destination_entrance_password", length = 50)
  private String destinationEntrancePassword;

  @Column(name = "destination_entrance_guide", length = 500)
  private String destinationEntranceGuide;

  @Column(name = "destination_request_message", length = 1000)
  private String destinationRequestMessage;

  @Column(name = "destination_latitude", precision = 10, scale = 7)
  private BigDecimal destinationLatitude;

  @Column(name = "destination_longitude", precision = 10, scale = 7)
  private BigDecimal destinationLongitude;

  @Column(name = "destination_jibnun_address", length = 300)
  private String destinationJibnunAddress;

  @Column(name = "destination_road_address", length = 300)
  private String destinationRoadAddress;

  @Column(name = "destination_detail_address", length = 300)
  private String destinationDetailAddress;

  @Builder
  public OrderLocationEntity(
      Long orderId,
      String originContactName,
      String originContactPhoneNumber,
      String originEntrancePassword,
      String originEntranceGuide,
      String originRequestMessage,
      BigDecimal originLatitude,
      BigDecimal originLongitude,
      String originJibnunAddress,
      String originRoadAddress,
      String originDetailAddress,
      String destinationContactName,
      String destinationContactPhoneNumber,
      String destinationEntrancePassword,
      String destinationEntranceGuide,
      String destinationRequestMessage,
      BigDecimal destinationLatitude,
      BigDecimal destinationLongitude,
      String destinationJibnunAddress,
      String destinationRoadAddress,
      String destinationDetailAddress) {
    this.orderId = orderId;
    this.originContactName = originContactName;
    this.originContactPhoneNumber = originContactPhoneNumber;
    this.originEntrancePassword = originEntrancePassword;
    this.originEntranceGuide = originEntranceGuide;
    this.originRequestMessage = originRequestMessage;
    this.originLatitude = originLatitude;
    this.originLongitude = originLongitude;
    this.originJibnunAddress = originJibnunAddress;
    this.originRoadAddress = originRoadAddress;
    this.originDetailAddress = originDetailAddress;
    this.destinationContactName = destinationContactName;
    this.destinationContactPhoneNumber = destinationContactPhoneNumber;
    this.destinationEntrancePassword = destinationEntrancePassword;
    this.destinationEntranceGuide = destinationEntranceGuide;
    this.destinationRequestMessage = destinationRequestMessage;
    this.destinationLatitude = destinationLatitude;
    this.destinationLongitude = destinationLongitude;
    this.destinationJibnunAddress = destinationJibnunAddress;
    this.destinationRoadAddress = destinationRoadAddress;
    this.destinationDetailAddress = destinationDetailAddress;
  }

  // TODO: Domain 생성 후 구현
  // public static OrderLocationEntity from(OrderLocation location) { }
  // public OrderLocation toDomain() { }
}

