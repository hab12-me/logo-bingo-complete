package com.logobing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String transactionId;
    
    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;
    
    private String type;
    private Double amount;
    private Double previousBalance;
    private Double newBalance;
    private String status;
    private String paymentMethod;
    private String paymentReference;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        transactionId = "TXN" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
        status = "PENDING";
    }
    
    public void complete() {
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
    }
}
