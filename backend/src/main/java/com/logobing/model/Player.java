package com.logobing.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "players")
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
    private String banReason;
    private LocalDateTime createdAt;
    private LocalDateTime lastSeen;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastSeen = LocalDateTime.now();
        registeredAt = LocalDateTime.now();
    }
    
    // ==================== GETTERS ====================
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhoneNumber() { return phoneNumber; }
    public Boolean getIsRegistered() { return isRegistered; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public String getLanguageCode() { return languageCode; }
    public Double getWallet() { return wallet; }
    public Double getTotalDeposited() { return totalDeposited; }
    public Double getTotalWithdrawn() { return totalWithdrawn; }
    public Double getTotalWon() { return totalWon; }
    public Double getTotalBet() { return totalBet; }
    public Integer getGamesPlayed() { return gamesPlayed; }
    public Integer getGamesWon() { return gamesWon; }
    public Boolean getIsAdmin() { return isAdmin; }
    public Boolean getIsActive() { return isActive; }
    public Boolean getIsBanned() { return isBanned; }
    public String getBanReason() { return banReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastSeen() { return lastSeen; }
    
    // ==================== SETTERS ====================
    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setIsRegistered(Boolean isRegistered) { this.isRegistered = isRegistered; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }
    public void setWallet(Double wallet) { this.wallet = wallet; }
    public void setTotalDeposited(Double totalDeposited) { this.totalDeposited = totalDeposited; }
    public void setTotalWithdrawn(Double totalWithdrawn) { this.totalWithdrawn = totalWithdrawn; }
    public void setTotalWon(Double totalWon) { this.totalWon = totalWon; }
    public void setTotalBet(Double totalBet) { this.totalBet = totalBet; }
    public void setGamesPlayed(Integer gamesPlayed) { this.gamesPlayed = gamesPlayed; }
    public void setGamesWon(Integer gamesWon) { this.gamesWon = gamesWon; }
    public void setIsAdmin(Boolean isAdmin) { this.isAdmin = isAdmin; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setIsBanned(Boolean isBanned) { this.isBanned = isBanned; }
    public void setBanReason(String banReason) { this.banReason = banReason; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }
    
    // ==================== BUSINESS METHODS ====================
    public String getName() {
        return this.firstName + (this.lastName != null ? " " + this.lastName : "");
    }
    
    public void updateLastSeen() { 
        this.lastSeen = LocalDateTime.now(); 
    }
    
    public void addToWallet(Double amount) { 
        this.wallet += amount; 
        this.totalWon += amount; 
    }
    
    public void deductFromWallet(Double amount) { 
        this.wallet -= amount; 
        this.totalBet += amount; 
    }
    
    public void addDeposit(Double amount) { 
        this.wallet += amount; 
        this.totalDeposited += amount; 
    }
    
    public void addWithdraw(Double amount) { 
        this.wallet -= amount; 
        this.totalWithdrawn += amount; 
    }
    
    public boolean hasSufficientFunds(Double amount) { 
        return this.wallet >= amount; 
    }
    
    public void completeRegistration(String phoneNumber, String firstName, String lastName, String languageCode) {
        this.phoneNumber = phoneNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.languageCode = languageCode;
        this.isRegistered = true;
        this.registeredAt = LocalDateTime.now();
    }
}
