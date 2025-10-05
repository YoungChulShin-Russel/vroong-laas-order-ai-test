package vroong.laas.order.core.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * Domain Service를 나타내는 애노테이션
 *
 * <p>Domain Layer의 Service 클래스에 사용됩니다.
 *
 * <p>특징:
 * - Stateless (상태 없음)
 * - 순수 계산 로직
 * - 여러 Aggregate를 조합하는 비즈니스 규칙
 * - Repository 의존 금지
 * - @Transactional 사용 금지
 *
 * <p>Spring의 @Component를 메타 애노테이션으로 사용하여 자동으로 Bean 등록됩니다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface DomainService {}
