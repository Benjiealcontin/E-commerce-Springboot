package com.Ecommerce.OrderService.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "CancelOrder")
public class CancelOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerId;

    private Long orderId;

    private String cancelReason;

    @Column(name = "order_at", nullable = false, updatable = false)
    private LocalDateTime orderDate;

    @CreationTimestamp
    @Column(name = "cancel_at", nullable = false, updatable = false)
    private LocalDateTime cancellationDate;
}
