package com.logobing.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "deposit_requests")
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
    private Integer verificationAttempts = 0;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        requestId = "DEP" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
        status = "PENDING";
        verificationAttempts = 0;
    }
    
    // Getters
    public Long getId() { return id; }
    public String getRequestId() { return requestId; }
    public Player getPlayer() { return player; }
    public Double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getTransactionId() { return transactionId; }
    public String getSenderPhoneNumber() { return senderPhoneNumber; }
    public String getReceiverPhoneNumber() { return receiverPhoneNumber; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public Long getVerifiedByAdminId() { return verifiedByAdminId; }
    public String getAdminNotes() { return adminNotes; }
    public String getVerificationMessage() { return verificationMessage; }
    public Integer getVerificationAttempts() { return verificationAttempts; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public void setPlayer(Player player) { this.player = player; }
    public void setAmount(Double amount) { this.amount = amount; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public void setSenderPhoneNumber(String senderPhoneNumber) { this.senderPhoneNumber = senderPhoneNumber; }
    public void setReceiverPhoneNumber(String receiverPhoneNumber) { this.receiverPhoneNumber = receiverPhoneNumber; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public void setVerifiedByAdminId(Long verifiedByAdminId) { this.verifiedByAdminId = verifiedByAdminId; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    public void setVerificationMessage(String verificationMessage) { this.verificationMessage = verificationMessage; }
    public void setVerificationAttempts(Integer verificationAttempts) { this.verificationAttempts = verificationAttempts; }
    
    // Business Methods
    public void incrementAttempts() { this.verificationAttempts++; }
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
