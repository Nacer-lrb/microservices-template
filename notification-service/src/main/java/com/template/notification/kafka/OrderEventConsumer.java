package com.template.notification.kafka;

import com.template.notification.dto.OrderCreatedEvent;
import com.template.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumer Kafka qui écoute le topic "order.created" publié par le Order-Service.
 * Spring Kafka gère automatiquement la désérialisation JSON → OrderCreatedEvent.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final NotificationService notificationService;

    /**
     * Méthode déclenchée automatiquement à chaque message reçu sur le topic.
     *
     * @param event     L'événement désérialisé depuis le message Kafka.
     * @param partition La partition Kafka depuis laquelle le message est lu.
     * @param offset    L'offset du message dans la partition.
     */
    @KafkaListener(
            topics = "order.created",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderCreatedEvent(
            @Payload OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("✅ Événement reçu sur 'order.created' | Partition: {} | Offset: {} | OrderId: {}",
                partition, offset, event.getOrderId());

        notificationService.processOrderNotification(event);
    }
}
