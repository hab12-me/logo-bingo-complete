package com.logobing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "deposit_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String requestId;
    
    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;
    
    private Double amount;
    private String paymentMethod = "TELEBIRR";
    private String transactionId;
    private String senderPhoneNumber;
    private String receiverPhoneNumber = "0931721793";
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;
    private LocalDateTime completedAt;
    private Long verifiedByAdminId;
    private String adminNotes;
    private String verificationMessage;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        requestId = "DEP" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
        status = "PENDING";
    }
    
    public void approve(Long adminId) {
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
        this.verifiedByAdminId = adminId;
        this.verifiedAt = LocalDateTime.now();
    }
    
    public void reject(String reason, Long adminId) {
        this.status = "FAILED";
        this.completedAt = LocalDateTime.now();
        this.verifiedByAdminId = adminId;
        this.adminNotes = reason;
    }
}
