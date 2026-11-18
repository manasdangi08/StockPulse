package com.stockpulse.controller;

import com.stockpulse.dto.OrderResponseDTO;
import com.stockpulse.dto.ReserveRequestDTO;
import com.stockpulse.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 1. Stock Reserve API
    @PostMapping("/reserve")
    public ResponseEntity<OrderResponseDTO> reserveOrder(@Valid @RequestBody ReserveRequestDTO request) {
        OrderResponseDTO response = orderService.reserveStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 Created
    }

    // 2. Payment Confirm API
    @PostMapping("/{id}/confirm")
    public ResponseEntity<OrderResponseDTO> confirmOrder(@PathVariable Long id) {
        OrderResponseDTO response = orderService.confirmOrder(id);
        return ResponseEntity.ok(response); // 200 OK
    }

    // 3. Order History API (N+1 Fix wali)
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders()); // 200 OK
    }
}