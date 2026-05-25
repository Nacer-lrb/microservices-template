package com.template.notification.service;

import com.template.notification.dto.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    /**
     * Traite un événement de commande confirmée.
     * En production, cette méthode enverrait un email via JavaMail / SendGrid / etc.
     * Ici, on simule l'envoi avec des logs structurés.
     *
     * @param event L'événement Kafka désérialisé depuis le topic "order.created"
     */
    public void processOrderNotification(OrderCreatedEvent event) {
        log.info("╔══════════════════════════════════════════════════════╗");
        log.info("║         📧 NOTIFICATION DE COMMANDE CONFIRMÉE         ║");
        log.info("╠══════════════════════════════════════════════════════╣");
        log.info("║  Commande ID  : #{}", event.getOrderId());
        log.info("║  Client       : {}", event.getUsername());
        log.info("║  Produit      : {}", event.getProductName());
        log.info("║  Quantité     : {}", event.getQuantity());
        log.info("║  Total        : {}€", event.getTotalPrice());
        log.info("║  Date         : {}", event.getCreatedAt());
        log.info("╚══════════════════════════════════════════════════════╝");

        // Simulation envoi email
        sendEmailSimulation(event);
    }

    private void sendEmailSimulation(OrderCreatedEvent event) {
        log.info("[EMAIL SIMULÉ] → À : {}@example.com", event.getUsername());
        log.info("[EMAIL SIMULÉ]   Sujet : Votre commande #{} est confirmée !", event.getOrderId());
        log.info("[EMAIL SIMULÉ]   Corps : Bonjour {}, votre commande de {} x \"{}\" " +
                "pour un total de {}€ a bien été enregistrée.",
                event.getUsername(),
                event.getQuantity(),
                event.getProductName(),
                event.getTotalPrice());
    }
}
