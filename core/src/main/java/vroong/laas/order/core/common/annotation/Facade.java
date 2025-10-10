package vroong.laas.order.core.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * Facade를 나타내는 애노테이션
 *
 * <p>Application Layer의 Facade 클래스에 사용됩니다.
 *
 * <p>책임:
 * - API 진입점 (Controller에서 호출)
 * - Domain Service 호출 및 조합 (OrderCreator, OrderReader 등)
 * - 외부 서비스 호출 (이벤트 발행, 알림 전송 등)
 * - 트랜잭션 경계 분리 (Domain Service에서 관리)
 *
 * <p>특징:
 * - 1개의 Aggregate = 1개의 Facade
 * - Domain Service를 조합하여 비즈니스 흐름 제어
 * - 트랜잭션은 Domain Service에 위임
 * - Infrastructure 직접 의존 금지 (Domain Port만 의존)
 *
 * <p>Spring의 @Component를 메타 애노테이션으로 사용하여 자동으로 Bean 등록됩니다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Facade {}

