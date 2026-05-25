package com.template.order.service;

import com.template.order.client.ProductClient;
import com.template.order.config.KafkaConfig;
import com.template.order.dto.OrderCreatedEvent;
import com.template.order.dto.OrderRequest;
import com.template.order.dto.ProductResponse;
import com.template.order.entity.Order;
import com.template.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;  // Appel synchrone via Feign
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate; // Publication async Kafka

    /**
     * Crée une commande :
     * 1. Appelle le Product-Service (Feign) pour récupérer le produit et vérifier le stock.
     * 2. Calcule le prix total.
     * 3. Sauvegarde la commande en base.
     * 4. Publie un événement Kafka "order.created".
     */
    public Order createOrder(OrderRequest request) {
        // --- ÉTAPE 1 : Appel synchrone vers Product-Service via Feign ---
        log.info("Fetching product {} from product-service...", request.getProductId());
        ProductResponse product = productClient.getProductById(request.getProductId());

        // Vérification du stock
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException(
                "Stock insuffisant pour le produit '" + product.getName() +
                "'. Stock disponible : " + product.getStockQuantity()
            );
        }

        // --- ÉTAPE 2 : Calcul du prix total ---
        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        // --- ÉTAPE 3 : Sauvegarde de la commande ---
        Order order = new Order();
        order.setUsername(request.getUsername());
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(totalPrice);
        order.setStatus(Order.OrderStatus.CONFIRMED);

        Order savedOrder = orderRepository.save(order);
        log.info("Order {} confirmed for user '{}'", savedOrder.getId(), savedOrder.getUsername());

        // --- ÉTAPE 4 : Publication de l'événement Kafka ---
        OrderCreatedEvent event = new OrderCreatedEvent(
            savedOrder.getId(),
            savedOrder.getUsername(),
            savedOrder.getProductName(),
            savedOrder.getQuantity(),
            savedOrder.getTotalPrice(),
            savedOrder.getCreatedAt()
        );

        kafkaTemplate.send(KafkaConfig.ORDER_CREATED_TOPIC, event);
        log.info("Event published to Kafka topic '{}'", KafkaConfig.ORDER_CREATED_TOPIC);

        return savedOrder;
    }

    public List<Order> getOrdersByUser(String username) {
        return orderRepository.findByUsername(username);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
