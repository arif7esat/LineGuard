package com.lineguard.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical or virtual production line managed by LineGuard.
 */
@Entity
@Table(name = "production_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 512)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductionLineState state;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "productionLine", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SensorReading> sensorReadings = new ArrayList<>();

    @OneToMany(mappedBy = "productionLine", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Alarm> alarms = new ArrayList<>();

    @OneToMany(mappedBy = "productionLine", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AlarmThreshold> thresholds = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.state == null) {
            this.state = ProductionLineState.IDLE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
