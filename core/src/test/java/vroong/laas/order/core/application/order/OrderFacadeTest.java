package vroong.laas.order.core.application.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vroong.laas.order.core.domain.address.AddressRefiner;
import vroong.laas.order.core.domain.address.exception.AddressRefineFailedException;
import vroong.laas.order.core.domain.order.DeliveryPolicy;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderCreator;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.order.OrderReader;
import vroong.laas.order.core.domain.order.Origin;
import vroong.laas.order.core.domain.order.command.CreateOrderCommand;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.LatLng;
import vroong.laas.order.core.fixture.OrderFixtures;

@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

  @InjectMocks private OrderFacade orderFacade;

  @Mock private OrderCreator orderCreator;

  @Mock private OrderReader orderReader;

  @Mock private AddressRefiner addressRefiner;

  private OrderFixtures orderFixtures;

  @BeforeEach
  void setUp() {
    FixtureMonkey fixtureMonkey =
        FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .defaultNotNull(true)
            .build();

    orderFixtures = new OrderFixtures(fixtureMonkey);
  }

  @Test
  @DisplayName("주문 생성 시 주소 정제를 수행하고 정제된 주소로 Order를 생성한다")
  void createOrder_refines_addresses() {
    // given
    List<OrderItem> items = orderFixtures.randomOrderItems();
    Origin origin = orderFixtures.randomOrigin();
    Destination destination = orderFixtures.randomDestination();
    DeliveryPolicy deliveryPolicy = orderFixtures.randomDeliveryPolicy();

    CreateOrderCommand command = new CreateOrderCommand(items, origin, destination, deliveryPolicy);

    // 정제된 주소
    Address refinedOriginAddress =
        new Address("서울시 강남구 역삼동 123", "서울시 강남구 테헤란로 123", "1층");
    Address refinedDestinationAddress =
        new Address("서울시 서초구 서초동 456", "서울시 서초구 서초대로 456", "2층");

    given(addressRefiner.refine(eq(origin.latLng()), eq(origin.address())))
        .willReturn(refinedOriginAddress);
    given(addressRefiner.refine(eq(destination.latLng()), eq(destination.address())))
        .willReturn(refinedDestinationAddress);

    Order mockOrder = orderFixtures.order();
    given(orderCreator.create(any(), any(Origin.class), any(Destination.class), any()))
        .willReturn(mockOrder);

    // when
    Order result = orderFacade.createOrder(command);

    // then
    assertThat(result).isEqualTo(mockOrder);

    // 주소 정제가 2번 호출되었는지 확인 (Origin, Destination)
    verify(addressRefiner, times(2)).refine(any(LatLng.class), any(Address.class));

    // OrderCreator에 정제된 주소가 전달되었는지 확인
    ArgumentCaptor<Origin> originCaptor = ArgumentCaptor.forClass(Origin.class);
    ArgumentCaptor<Destination> destinationCaptor = ArgumentCaptor.forClass(Destination.class);

    verify(orderCreator)
        .create(eq(items), originCaptor.capture(), destinationCaptor.capture(), eq(deliveryPolicy));

    // 정제된 주소가 전달되었는지 확인
    assertThat(originCaptor.getValue().address()).isEqualTo(refinedOriginAddress);
    assertThat(destinationCaptor.getValue().address()).isEqualTo(refinedDestinationAddress);
  }

  @Test
  @DisplayName("Origin 주소 정제 실패 시 AddressRefineFailedException 발생")
  void createOrder_origin_refine_fail() {
    // given
    CreateOrderCommand command =
        new CreateOrderCommand(
            orderFixtures.randomOrderItems(),
            orderFixtures.randomOrigin(),
            orderFixtures.randomDestination(),
            orderFixtures.randomDeliveryPolicy());

    given(addressRefiner.refine(any(), any()))
        .willThrow(new AddressRefineFailedException("모든 역지오코딩 서비스가 실패했습니다"));

    // when & then
    assertThatThrownBy(() -> orderFacade.createOrder(command))
        .isInstanceOf(AddressRefineFailedException.class)
        .hasMessageContaining("모든 역지오코딩 서비스가 실패했습니다");

    // OrderCreator는 호출되지 않아야 함
    verify(orderCreator, times(0)).create(any(), any(), any(), any());
  }

  @Test
  @DisplayName("Destination 주소 정제 실패 시 AddressRefineFailedException 발생")
  void createOrder_destination_refine_fail() {
    // given
    Origin origin = orderFixtures.randomOrigin();
    Destination destination = orderFixtures.randomDestination();

    CreateOrderCommand command =
        new CreateOrderCommand(
            orderFixtures.randomOrderItems(), origin, destination, orderFixtures.randomDeliveryPolicy());

    // Origin은 성공, Destination은 실패
    Address refinedOriginAddress =
        new Address("서울시 강남구 역삼동 123", "서울시 강남구 테헤란로 123", "1층");
    given(addressRefiner.refine(eq(origin.latLng()), eq(origin.address())))
        .willReturn(refinedOriginAddress);
    given(addressRefiner.refine(eq(destination.latLng()), eq(destination.address())))
        .willThrow(new AddressRefineFailedException("모든 역지오코딩 서비스가 실패했습니다"));

    // when & then
    assertThatThrownBy(() -> orderFacade.createOrder(command))
        .isInstanceOf(AddressRefineFailedException.class)
        .hasMessageContaining("모든 역지오코딩 서비스가 실패했습니다");

    // OrderCreator는 호출되지 않아야 함
    verify(orderCreator, times(0)).create(any(), any(), any(), any());
  }
}

