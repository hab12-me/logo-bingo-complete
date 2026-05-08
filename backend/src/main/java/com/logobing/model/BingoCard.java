package com.logobing.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bingo_cards")
public class BingoCard {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Integer cardNumber;
    
    @Column(columnDefinition = "TEXT")
    private String gridData;
    
    private Double price = 10.0;
    private Boolean isAvailable = true;
    
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Player owner;
    
    @ManyToOne
    @JoinColumn(name = "locked_by")
    private Player lockedBy;
    
    private LocalDateTime lockedAt;
    
    @PrePersist
    protected void onCreate() { 
        lockedAt = LocalDateTime.now(); 
    }
    
    // Getters
    public Long getId() { return id; }
    public Integer getCardNumber() { return cardNumber; }
    public String getGridData() { return gridData; }
    public Double getPrice() { return price; }
    public Boolean getIsAvailable() { return isAvailable; }
    public Player getOwner() { return owner; }
    public Player getLockedBy() { return lockedBy; }
    public LocalDateTime getLockedAt() { return lockedAt; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setCardNumber(Integer cardNumber) { this.cardNumber = cardNumber; }
    public void setGridData(String gridData) { this.gridData = gridData; }
    public void setPrice(Double price) { this.price = price; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    public void setOwner(Player owner) { this.owner = owner; }
    public void setLockedBy(Player lockedBy) { this.lockedBy = lockedBy; }
    public void setLockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; }
    
    // Business Methods
    public void lock(Player player) {
        this.lockedBy = player;
        this.lockedAt = LocalDateTime.now();
        this.isAvailable = false;
    }
    
    public void unlock() {
        this.lockedBy = null;
        this.lockedAt = null;
        this.isAvailable = true;
    }
}
