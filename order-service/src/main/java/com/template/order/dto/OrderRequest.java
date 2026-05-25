package com.template.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO reçu pour créer une commande.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private String username;
    private Long productId;
    private int quantity;
}
