package com.template.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;       // Qui a passé la commande
    private Long productId;        // Quel produit a été commandé
    private String productName;    // Nom du produit au moment de la commande (snapshot)
    private int quantity;          // Quantité commandée
    private BigDecimal totalPrice; // Prix total calculé

    @Enumerated(EnumType.STRING)
    private OrderStatus status;    // PENDING, CONFIRMED, CANCELLED

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = OrderStatus.PENDING;
        }
    }

    public enum OrderStatus {
        PENDING, CONFIRMED, CANCELLED
    }
}
