package com.stockpulse.controller;

import com.stockpulse.entity.Product;
import com.stockpulse.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // App start hote hi agar table khali hai toh 3 demo products automatically add ho jayenge
    @PostConstruct
    public void seedProducts() {
        if (productRepository.count() == 0) {
            productRepository.save(new Product("iPhone 16 Pro (Flash Deal)", new BigDecimal("119900.00"), 1));
            productRepository.save(new Product("Sony WH-1000XM5 Headphones", new BigDecimal("24990.00"), 5));
            productRepository.save(new Product("Mechanical Keychron Keyboard", new BigDecimal("8500.00"), 3));
        }
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}