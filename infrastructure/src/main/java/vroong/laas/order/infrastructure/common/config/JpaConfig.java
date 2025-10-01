package vroong.laas.order.infrastructure.common.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "vroong.laas.order.infrastructure.storage.db")
@EnableJpaRepositories(basePackages = "vroong.laas.order.infrastructure.storage.db")
class JpaConfig {

}
