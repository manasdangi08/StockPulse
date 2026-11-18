package com.stockpulse.dto;

import com.stockpulse.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderResponseDTO {

    private Long orderId;
    private String customerName;
    private String productName;
    private Integer quantity;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime expiresAt;

    public OrderResponseDTO(Long orderId, String customerName, String productName,
                            Integer quantity, BigDecimal totalAmount,
                            OrderStatus status, LocalDateTime expiresAt) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.productName = productName;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    // Getters
    public Long getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}