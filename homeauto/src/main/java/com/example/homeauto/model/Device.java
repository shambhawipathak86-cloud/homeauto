package com.example.homeauto.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String type;

    private String room;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean status = false;

    @Column(nullable = false)
    private Integer adjustmentLevel = 100;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceCompatibilityStatus compatibilityStatus =
            DeviceCompatibilityStatus.PENDING;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    public Device() {
    }

    @PrePersist
    public void prePersist() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (compatibilityStatus == null) {
            compatibilityStatus =
                    DeviceCompatibilityStatus.PENDING;
        }

        if (adjustmentLevel == null) {
            adjustmentLevel = 100;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Integer getAdjustmentLevel() {
        return adjustmentLevel;
    }

    public void setAdjustmentLevel(Integer adjustmentLevel) {

        if (adjustmentLevel == null) {
            this.adjustmentLevel = 100;
            return;
        }

        if (adjustmentLevel < 0) {
            this.adjustmentLevel = 0;
        } else if (adjustmentLevel > 100) {
            this.adjustmentLevel = 100;
        } else {
            this.adjustmentLevel = adjustmentLevel;
        }
    }

    public DeviceCompatibilityStatus getCompatibilityStatus() {
        return compatibilityStatus;
    }

    public void setCompatibilityStatus(
            DeviceCompatibilityStatus compatibilityStatus) {

        this.compatibilityStatus = compatibilityStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public AppUser getOwner() {
        return owner;
    }

    public void setOwner(AppUser owner) {
        this.owner = owner;
    }
}