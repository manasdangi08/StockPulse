package com.stockpulse.service;

import com.stockpulse.dto.OrderResponseDTO;
import com.stockpulse.dto.ReserveRequestDTO;
import com.stockpulse.entity.Order;
import com.stockpulse.entity.OrderStatus;
import com.stockpulse.entity.Product;
import com.stockpulse.repository.OrderRepository;
import com.stockpulse.repository.ProductRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    // Constructor Injection (Best Practice)
    public OrderService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    // 1. STOCK RESERVE KARNA (2 Minute ke lock ke saath)
    @Transactional
    public OrderResponseDTO reserveStock(ReserveRequestDTO request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + request.getProductId()));

        if (product.getAvailableStock() < request.getQuantity()) {
            throw new IllegalStateException("Insufficient stock! Only " + product.getAvailableStock() + " left.");
        }

        // Stock kam karein (Yahan Hibernate @Version check karega)
        product.setAvailableStock(product.getAvailableStock() - request.getQuantity());
        productRepository.save(product);

        // Total amount calculate karein (BigDecimal multiplication)
        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        // Order create karein (Testing ke liye 2 minute ka expiry time)
        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setProduct(product);
        order.setQuantity(request.getQuantity());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.RESERVED);
        order.setExpiresAt(LocalDateTime.now().plusMinutes(2));

        Order savedOrder = orderRepository.save(order);
        return mapToDTO(savedOrder);
    }

    // 2. PAYMENT CONFIRM KARNA
    @Transactional
    public OrderResponseDTO confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order is already confirmed.");
        }

        if (order.getStatus() == OrderStatus.EXPIRED || order.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("EXPIRED: Reservation time has passed. Please place a new order.");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        return mapToDTO(orderRepository.save(order));
    }

    // 3. SAARE ORDERS LAANA (N+1 Fix wali query use karke)
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAllWithProducts()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 4. BACKGROUND JOB: Har 15 second mein expired orders ka stock wapas add karna
    @Scheduled(fixedRate = 15000)
    @Transactional
    public void releaseExpiredReservations() {
        List<Order> expiredOrders = orderRepository.findExpiredOrders(OrderStatus.RESERVED, LocalDateTime.now());

        for (Order order : expiredOrders) {
            order.setStatus(OrderStatus.EXPIRED);
            Product product = order.getProduct();
            product.setAvailableStock(product.getAvailableStock() + order.getQuantity());
            
            productRepository.save(product);
            orderRepository.save(order);
            System.out.println("Auto-released stock for Expired Order ID: " + order.getId());
        }
    }

    // Entity ko safe DTO mein badalne wala helper method
    private OrderResponseDTO mapToDTO(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getCustomerName(),
                order.getProduct().getName(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getExpiresAt()
        );
    }
}