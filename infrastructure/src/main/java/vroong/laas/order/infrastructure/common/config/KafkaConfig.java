package vroong.laas.order.infrastructure.common.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@ComponentScan(basePackages = {
    "com.vroong.msa.kafka.eventpublisher"
})
class KafkaConfig {

}
