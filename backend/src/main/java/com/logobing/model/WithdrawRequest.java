package com.logobing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "withdraw_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String requestId;
    
    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;
    
    private Double amount;
    private String paymentMethod = "TELEBIRR";
    private String recipientPhoneNumber;
    private String recipientName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
    private Long processedByAdminId;
    private String adminNotes;
    private String transactionReference;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        requestId = "WDR" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
        status = "PENDING";
    }
    
    public void approve(Long adminId, String reference) {
        this.status = "COMPLETED";
        this.processedAt = LocalDateTime.now();
        this.completedAt = LocalDateTime.now();
        this.processedByAdminId = adminId;
        this.transactionReference = reference;
    }
    
    public void reject(String reason, Long adminId) {
        this.status = "FAILED";
        this.processedAt = LocalDateTime.now();
        this.processedByAdminId = adminId;
        this.adminNotes = reason;
    }
}
