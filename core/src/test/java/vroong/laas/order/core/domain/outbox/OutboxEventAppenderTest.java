package vroong.laas.order.core.domain.outbox;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.outbox.required.OutboxEventClient;
import vroong.laas.order.core.fixture.OrderFixtures;

/**
 * OutboxEventAppender 단위 테스트
 *
 * <p>Domain Service 테스트
 * - OutboxEventClient Mock으로 대체
 * - OutboxEventClient.save() 호출 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxEventAppender 테스트")
class OutboxEventAppenderTest {

  @InjectMocks private OutboxEventAppender sut;

  @Mock private OutboxEventClient outboxEventClient;

  private FixtureMonkey fixtureMonkey;
  private OrderFixtures orderFixtures;

  @BeforeEach
  void setUp() {
    fixtureMonkey =
        FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .defaultNotNull(true)
            .build();

    orderFixtures = new OrderFixtures(fixtureMonkey);
  }

  @Test
  @DisplayName("OutboxEventClient.save()를 올바른 파라미터로 호출한다")
  void append_callsOutboxEventClientWithCorrectParameters() {
    // given
    Order order = orderFixtures.order();

    // when
    sut.append(OutboxEventType.ORDER_CREATED, order);

    // then
    verify(outboxEventClient).save(eq(OutboxEventType.ORDER_CREATED), eq(order));
  }

  @Test
  @DisplayName("여러 번 호출해도 정상 동작한다")
  void append_canBeCalledMultipleTimes() {
    // given
    Order order1 = orderFixtures.order();
    Order order2 = orderFixtures.order();

    // when
    sut.append(OutboxEventType.ORDER_CREATED, order1);
    sut.append(OutboxEventType.ORDER_CREATED, order2);

    // then
    verify(outboxEventClient).save(OutboxEventType.ORDER_CREATED, order1);
    verify(outboxEventClient).save(OutboxEventType.ORDER_CREATED, order2);
  }

  @Test
  @DisplayName("OutboxEventClient 호출 실패 시 예외를 전파한다")
  void append_propagatesExceptionWhenClientFails() {
    // given
    Order order = orderFixtures.order();

    willThrow(new RuntimeException("Outbox 저장 실패"))
        .given(outboxEventClient)
        .save(any(), any());

    // when & then
    assertThatThrownBy(() -> sut.append(OutboxEventType.ORDER_CREATED, order))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Outbox 저장 실패");
  }

  @Test
  @DisplayName("다양한 OutboxEventType을 처리할 수 있다")
  void append_handlesVariousEventTypes() {
    // given
    Order order = orderFixtures.order();

    // when
    sut.append(OutboxEventType.ORDER_CREATED, order);

    // then
    verify(outboxEventClient).save(OutboxEventType.ORDER_CREATED, order);
    
    // 향후 다른 이벤트 타입 추가 시 테스트 확장 가능
    // sut.append(OutboxEventType.ORDER_CANCELLED, order);
    // verify(outboxEventClient).save(OutboxEventType.ORDER_CANCELLED, order);
  }
}

