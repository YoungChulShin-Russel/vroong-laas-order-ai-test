package vroong.laas.order.core.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Service;

/**
 * Use Case를 나타내는 애노테이션
 *
 * <p>Application Layer의 Use Case 클래스에 사용됩니다.
 *
 * <p>특징:
 * - 하나의 Use Case = 하나의 파일 = 하나의 execute() 메서드
 * - Domain Port만 의존 (Store, Reader, 기타 Port)
 * - 비즈니스 로직은 Domain에, UseCase는 흐름 제어만
 * - Infrastructure 직접 의존 금지
 *
 * <p>Spring의 @Service를 메타 애노테이션으로 사용하여 자동으로 Bean 등록됩니다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface UseCase {}
