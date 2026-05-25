package com.template.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Miroir du DTO publié par le Order-Service sur le topic Kafka "order.created".
 * Les champs doivent correspondre exactement pour la désérialisation JSON.
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
