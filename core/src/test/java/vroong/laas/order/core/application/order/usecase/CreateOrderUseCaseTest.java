package vroong.laas.order.core.application.order.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vroong.laas.order.core.application.order.command.CreateOrderCommand;
import vroong.laas.order.core.domain.order.DeliveryPolicy;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.EntranceInfo;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.order.OrderStatus;
import vroong.laas.order.core.domain.order.Origin;
import vroong.laas.order.core.domain.order.required.OrderStore;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.Contact;
import vroong.laas.order.core.domain.shared.LatLng;
import vroong.laas.order.core.domain.shared.Money;
import vroong.laas.order.core.domain.shared.Volume;
import vroong.laas.order.core.domain.shared.Weight;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

  @InjectMocks private CreateOrderUseCase sut;

  @Mock private OrderStore orderStore;

  private CreateOrderCommand command;

  @BeforeEach
  void setUp() {
    command = createCommand();
  }

  @Test
  @DisplayName("주문을 생성하고 저장한다")
  void execute_creates_and_stores_order() {
    // given
    given(orderStore.store(any(Order.class)))
        .willAnswer(
            invocation -> {
              Order order = invocation.getArgument(0);
              order.assignId(1L);
              return order;
            });

    // when
    Order result = sut.execute(command);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getStatus()).isEqualTo(OrderStatus.CREATED);
    assertThat(result.getItems()).hasSize(1);

    verify(orderStore).store(any(Order.class));
  }

  @Test
  @DisplayName("Command의 값이 Domain 객체로 올바르게 변환된다")
  void execute_converts_command_to_domain_correctly() {
    // given
    given(orderStore.store(any(Order.class)))
        .willAnswer(
            invocation -> {
              Order order = invocation.getArgument(0);
              order.assignId(1L);
              return order;
            });

    // when
    Order result = sut.execute(command);

    // then
    assertThat(result.getOrderNumber().value()).isEqualTo("ORD-20240101-001");
    assertThat(result.getOrigin().contact().name()).isEqualTo("홍길동");
    assertThat(result.getDestination().contact().name()).isEqualTo("김철수");
    assertThat(result.getDeliveryPolicy().alcoholDelivery()).isFalse();
    assertThat(result.getDeliveryPolicy().contactlessDelivery()).isTrue();
  }

  private CreateOrderCommand createCommand() {
    // Domain Model 생성
    Contact originContact = new Contact("홍길동", "010-1234-5678");
    Address originAddress = new Address(null, "서울시 강남구 테헤란로 1", "1층");
    LatLng originLatLng = new LatLng(BigDecimal.valueOf(37.5665), BigDecimal.valueOf(126.9780));
    EntranceInfo originEntranceInfo =
        new EntranceInfo("1234", "현관 비밀번호", "문 앞에 놓아주세요");
    Origin origin = new Origin(originContact, originAddress, originLatLng, originEntranceInfo);

    Contact destinationContact = new Contact("김철수", "010-9876-5432");
    Address destinationAddress = new Address("서울시 서초구 서초대로 2", null, "2층");
    LatLng destinationLatLng =
        new LatLng(BigDecimal.valueOf(37.4833), BigDecimal.valueOf(127.0322));
    Destination destination =
        new Destination(
            destinationContact, destinationAddress, destinationLatLng, EntranceInfo.empty());

    DeliveryPolicy deliveryPolicy =
        new DeliveryPolicy(false, true, false, null, Instant.now());

    // OrderItem 생성
    OrderItem orderItem =
        new OrderItem(
            "테스트 상품",
            2,
            new Money(BigDecimal.valueOf(10000)),
            "식품",
            new Weight(BigDecimal.valueOf(1.5)),
            new Volume(
                BigDecimal.valueOf(10), BigDecimal.valueOf(10), BigDecimal.valueOf(10)));

    return new CreateOrderCommand(
        "ORD-20240101-001", List.of(orderItem), origin, destination, deliveryPolicy);
  }
}
