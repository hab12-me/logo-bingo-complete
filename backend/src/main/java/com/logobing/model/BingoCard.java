package com.logobing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bingo_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    protected void onCreate() { lockedAt = LocalDateTime.now(); }
    
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
