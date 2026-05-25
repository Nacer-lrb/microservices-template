package com.template.order.controller;

import com.template.order.dto.OrderRequest;
import com.template.order.entity.Order;
import com.template.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/orders — Crée une nouvelle commande.
     * Déclenche un appel Feign vers Product-Service + publie sur Kafka.
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    /**
     * GET /api/orders — Liste toutes les commandes (admin).
     */
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /**
     * GET /api/orders/user/{username} — Liste les commandes d'un utilisateur.
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable String username) {
        return ResponseEntity.ok(orderService.getOrdersByUser(username));
    }
}
