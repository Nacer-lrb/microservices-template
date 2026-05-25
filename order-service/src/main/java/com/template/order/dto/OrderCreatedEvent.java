package com.template.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Événement Kafka publié lorsqu'une commande est confirmée.
 * Le Notification-Service consommera ce message.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;
    private String username;
    private String productName;
    private int quantity;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
}
