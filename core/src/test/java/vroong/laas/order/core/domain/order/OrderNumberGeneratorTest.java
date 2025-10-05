package vroong.laas.order.core.domain.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderNumberGeneratorTest {

  private OrderNumberGenerator sut;

  @BeforeEach
  void setUp() {
    sut = new OrderNumberGenerator();
  }

  @Test
  @DisplayName("주문번호를 생성한다")
  void generate_creates_order_number() {
    // when
    OrderNumber orderNumber = sut.generate();

    // then
    assertThat(orderNumber).isNotNull();
    assertThat(orderNumber.value()).isNotBlank();
  }

  @Test
  @DisplayName("생성된 주문번호는 'ORD-'로 시작한다")
  void generated_order_number_starts_with_prefix() {
    // when
    OrderNumber orderNumber = sut.generate();

    // then
    assertThat(orderNumber.value()).startsWith("ORD-");
  }

  @Test
  @DisplayName("생성된 주문번호는 올바른 포맷이다 (ORD-YYYYMMDDHHMMSS + 3자리 난수)")
  void generated_order_number_has_correct_format() {
    // given
    Pattern pattern = Pattern.compile("^ORD-\\d{14}\\d{3}$");

    // when
    OrderNumber orderNumber = sut.generate();

    // then
    assertThat(orderNumber.value()).matches(pattern);
    assertThat(orderNumber.value()).hasSize(21); // ORD-(4) + 14자리 + 3자리 = 21
  }

  @Test
  @DisplayName("여러 번 생성 시 다른 주문번호를 생성한다")
  void generate_creates_unique_order_numbers() {
    // given
    Set<String> orderNumbers = new HashSet<>();
    int count = 100;

    // when
    for (int i = 0; i < count; i++) {
      orderNumbers.add(sut.generate().value());
    }

    // then
    // 최소 85% 이상은 유니크해야 함 (난수 3자리이므로 충돌 가능성 고려)
    assertThat(orderNumbers).hasSizeGreaterThanOrEqualTo(85);
  }

  @Test
  @DisplayName("생성된 주문번호의 타임스탬프 부분은 현재 시간과 유사하다")
  void generated_order_number_timestamp_is_close_to_current_time() {
    // given
    LocalDateTime before = LocalDateTime.now();

    // when
    OrderNumber orderNumber = sut.generate();

    // then
    // ORD- 제거하고 앞 14자리 추출
    String timestamp = orderNumber.value().substring(4, 18);

    // 년, 월, 일까지 체크 (YYYYMMDD)
    String expectedDate = before.toString().replaceAll("[:-]", "").substring(0, 8);

    assertThat(timestamp).startsWith(expectedDate);
  }
}
