package vroong.laas.order.core.domain.order;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import org.springframework.stereotype.Service;

/**
 * 주문번호 생성기
 *
 * <p>주문번호 생성 규칙:
 * - 형식: ORD-YYYYMMDDHHMMSS + 3자리 난수
 * - 예시: ORD-20240101123045001
 *
 * <p>특징:
 * - Stateless (상태 없음)
 * - 순수 계산 로직
 * - Thread-safe
 */
@Service
public class OrderNumberGenerator {

  private static final String PREFIX = "ORD-";
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
  private static final Random RANDOM = new Random();

  /**
   * 주문번호 생성
   *
   * @return 생성된 주문번호
   */
  public OrderNumber generate() {
    String timestamp = LocalDateTime.now().format(FORMATTER);
    String randomSuffix = String.format("%03d", RANDOM.nextInt(1000));
    String value = PREFIX + timestamp + randomSuffix;
    return new OrderNumber(value);
  }
}
