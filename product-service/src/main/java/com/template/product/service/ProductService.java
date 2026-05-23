package com.template.product.service;

import com.template.product.entity.Product;
import com.template.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    public Product createProduct(Product product) {
        return repository.save(product);
    }

    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return repository.findById(id);
    }

    public Product updateProduct(Long id, Product newProduct) {
        return repository.findById(id).map(product -> {
            product.setName(newProduct.getName());
            product.setDescription(newProduct.getDescription());
            product.setPrice(newProduct.getPrice());
            product.setStockQuantity(newProduct.getStockQuantity());
            return repository.save(product);
        }).orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    public void deleteProduct(Long id) {
        repository.deleteById(id);
    }
}
