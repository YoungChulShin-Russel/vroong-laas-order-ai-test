package vroong.laas.order.infrastructure.outbox;

import com.vroong.msa.kafka.event.KafkaEvent;
import com.vroong.msa.kafka.event.KafkaEventPayload;

record KafkaOutboxEvent(
    String eventKey,
    KafkaEvent<KafkaEventPayload> kafkaEvent
) {

}
