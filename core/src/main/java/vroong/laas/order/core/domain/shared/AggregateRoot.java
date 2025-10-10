package vroong.laas.order.core.domain.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import vroong.laas.order.core.domain.shared.event.DomainEvent;

/**
 * Aggregate Root 추상 클래스
 *
 * <p>도메인 이벤트 관리 기능을 제공합니다.
 *
 * <p>특징:
 * - 순수 Java (Spring 의존성 없음)
 * - 도메인 이벤트 추가, 조회, 초기화 지원
 * - 불변 리스트 반환으로 캡슐화 보장
 *
 * <p>사용 예시:
 * <pre>{@code
 * public class Order extends AggregateRoot {
 *   public static Order create(...) {
 *     Order order = new Order(...);
 *     order.addDomainEvent(new OrderCreatedEvent(...));
 *     return order;
 *   }
 * }
 * }</pre>
 */
public abstract class AggregateRoot {

  private final List<DomainEvent> domainEvents = new ArrayList<>();

  /**
   * 도메인 이벤트 추가
   *
   * @param event 도메인 이벤트
   */
  protected void addDomainEvent(DomainEvent event) {
    this.domainEvents.add(event);
  }

  /**
   * 도메인 이벤트 목록 조회 (불변)
   *
   * @return 도메인 이벤트 목록
   */
  public List<DomainEvent> getDomainEvents() {
    return Collections.unmodifiableList(domainEvents);
  }

  /**
   * 도메인 이벤트 초기화
   *
   * <p>이벤트 발행 후 호출하여 이벤트 목록을 비웁니다.
   */
  public void clearDomainEvents() {
    this.domainEvents.clear();
  }
}

