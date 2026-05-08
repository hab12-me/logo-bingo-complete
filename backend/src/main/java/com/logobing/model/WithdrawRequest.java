package com.logobing.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "withdraw_requests")
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
    
    // Getters
    public Long getId() { return id; }
    public String getRequestId() { return requestId; }
    public Player getPlayer() { return player; }
    public Double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getRecipientPhoneNumber() { return recipientPhoneNumber; }
    public String getRecipientName() { return recipientName; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public Long getProcessedByAdminId() { return processedByAdminId; }
    public String getAdminNotes() { return adminNotes; }
    public String getTransactionReference() { return transactionReference; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public void setPlayer(Player player) { this.player = player; }
    public void setAmount(Double amount) { this.amount = amount; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setRecipientPhoneNumber(String recipientPhoneNumber) { this.recipientPhoneNumber = recipientPhoneNumber; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public void setProcessedByAdminId(Long processedByAdminId) { this.processedByAdminId = processedByAdminId; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
    
    // Business Methods
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
