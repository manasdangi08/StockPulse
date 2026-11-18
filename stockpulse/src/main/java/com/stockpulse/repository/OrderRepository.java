package com.stockpulse.repository;

import com.stockpulse.entity.Order;
import com.stockpulse.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 1. N+1 Query Problem Fix
    @Query("SELECT o FROM Order o JOIN FETCH o.product ORDER BY o.id DESC")
    List<Order> findAllWithProducts();

    // 2. Expired orders dhundhne ke liye (Background Job ke liye)
    @Query("SELECT o FROM Order o JOIN FETCH o.product WHERE o.status = :status AND o.expiresAt < :now")
    List<Order> findExpiredOrders(OrderStatus status, LocalDateTime now);
}