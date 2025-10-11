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
 * - ORDER_DESTINATION_UPDATED: 배송지 변경
 */
public enum OutboxEventType {
  /** 주문 생성 이벤트 */
  ORDER_CREATED,
}
