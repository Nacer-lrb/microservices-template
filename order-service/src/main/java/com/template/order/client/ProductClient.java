package com.template.order.client;

import com.template.order.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client Feign qui appelle le Product-Service via Eureka.
 * Le nom "product-service" doit correspondre exactement au spring.application.name du Product-Service.
 * Spring Cloud LoadBalancer résoudra automatiquement l'adresse via Eureka.
 */
@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductResponse getProductById(@PathVariable("id") Long id);
}
