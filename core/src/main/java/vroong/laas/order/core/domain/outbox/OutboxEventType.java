package vroong.laas.order.core.domain.outbox;

/**
 * Outbox Event Type
 *
 * <p>Outbox에 저장될 이벤트 타입을 정의합니다.
 *
 * <p>용도:
 * - OutboxEventAppender가 이벤트 타입을 받아서 적절한 매핑 수행
 * - Infrastructure Mapper에서 타입별로 다른 Kafka Payload 생성
 *
 * <p>이벤트 타입 추가 예시:
 * - ORDER_CREATED: 주문 생성
 * - ORDER_CANCELLED: 주문 취소
 * - ORDER_DESTINATION_CHANGED: 배송지 변경
 */
public enum OutboxEventType {
  /** 주문 생성 이벤트 */
  ORDER_CREATED,

  // TODO: Kafka 라이브러리 업데이트 후 구현
  //  - Infrastructure Mapper (KafkaOutboxEventMapper)에 매핑 로직 추가
  //  - Kafka Event Payload 정의 (OrderDestinationChangedKafkaEventPayload)
  //  - OrderLocationChanger에서 outboxEventAppender.append() 호출
  /** 주문 도착지 변경 이벤트 */
  ORDER_DESTINATION_CHANGED,
}
