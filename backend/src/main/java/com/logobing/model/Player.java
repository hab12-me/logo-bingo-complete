package com.logobing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "players")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    
    @Id
    private String id;
    
    private String username;
    private String firstName;
    private String lastName;
    
    @Column(unique = true)
    private String phoneNumber;
    
    private Boolean isRegistered = false;
    private LocalDateTime registeredAt;
    private String languageCode = "en";
    private Double wallet = 0.0;
    private Double totalDeposited = 0.0;
    private Double totalWithdrawn = 0.0;
    private Double totalWon = 0.0;
    private Double totalBet = 0.0;
    private Integer gamesPlayed = 0;
    private Integer gamesWon = 0;
    private Boolean isAdmin = false;
    private Boolean isActive = true;
    private Boolean isBanned = false;
    private LocalDateTime createdAt;
    private LocalDateTime lastSeen;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastSeen = LocalDateTime.now();
        registeredAt = LocalDateTime.now();
    }
    
    public void updateLastSeen() { this.lastSeen = LocalDateTime.now(); }
    public void addToWallet(Double amount) { this.wallet += amount; this.totalWon += amount; }
    public void deductFromWallet(Double amount) { this.wallet -= amount; this.totalBet += amount; }
    public void addDeposit(Double amount) { this.wallet += amount; this.totalDeposited += amount; }
    public void addWithdraw(Double amount) { this.wallet -= amount; this.totalWithdrawn += amount; }
    public boolean hasSufficientFunds(Double amount) { return this.wallet >= amount; }
    public void completeRegistration(String phoneNumber, String firstName, String lastName, String languageCode) {
        this.phoneNumber = phoneNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.languageCode = languageCode;
        this.isRegistered = true;
        this.registeredAt = LocalDateTime.now();
    }
}
