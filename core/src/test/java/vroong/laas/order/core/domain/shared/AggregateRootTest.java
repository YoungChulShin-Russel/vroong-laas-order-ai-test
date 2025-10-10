package vroong.laas.order.core.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.order.core.domain.shared.event.DomainEvent;

class AggregateRootTest {

  @Test
  @DisplayName("도메인 이벤트를 추가하고 조회할 수 있다")
  void add_and_get_domain_events() {
    // given
    TestAggregate aggregate = new TestAggregate();
    TestDomainEvent event1 = new TestDomainEvent("event1");
    TestDomainEvent event2 = new TestDomainEvent("event2");

    // when
    aggregate.addEvent(event1);
    aggregate.addEvent(event2);

    // then
    List<DomainEvent> events = aggregate.getDomainEvents();
    assertThat(events).hasSize(2);
    assertThat(events.get(0)).isEqualTo(event1);
    assertThat(events.get(1)).isEqualTo(event2);
  }

  @Test
  @DisplayName("도메인 이벤트 목록은 불변이다")
  void domain_events_are_immutable() {
    // given
    TestAggregate aggregate = new TestAggregate();
    aggregate.addEvent(new TestDomainEvent("event1"));

    // when
    List<DomainEvent> events = aggregate.getDomainEvents();

    // then - UnsupportedOperationException 발생
    assertThat(events).isUnmodifiable();
  }

  @Test
  @DisplayName("도메인 이벤트를 초기화할 수 있다")
  void clear_domain_events() {
    // given
    TestAggregate aggregate = new TestAggregate();
    aggregate.addEvent(new TestDomainEvent("event1"));
    aggregate.addEvent(new TestDomainEvent("event2"));

    assertThat(aggregate.getDomainEvents()).hasSize(2);

    // when
    aggregate.clearDomainEvents();

    // then
    assertThat(aggregate.getDomainEvents()).isEmpty();
  }

  // === 테스트용 클래스 ===

  static class TestAggregate extends AggregateRoot {
    public void addEvent(DomainEvent event) {
      addDomainEvent(event);
    }
  }

  record TestDomainEvent(String name) implements DomainEvent {
    @Override
    public Instant occurredAt() {
      return Instant.now();
    }
  }
}

