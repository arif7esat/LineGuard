package com.lineguard.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * An alarm raised by the threshold-based alarm engine.
 */
@Entity
@Table(name = "alarms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_line_id", nullable = false)
    private ProductionLine productionLine;

    /** Sensor that triggered the alarm. */
    @Column(nullable = false)
    private String sensorName;

    @Column(nullable = false)
    private double triggerValue;

    @Column(nullable = false)
    private double thresholdValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlarmSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlarmStatus status;

    @Column(nullable = false, length = 1024)
    private String message;

    @Column(nullable = false)
    private Instant raisedAt;

    private Instant acknowledgedAt;

    private Instant resolvedAt;

    @PrePersist
    public void prePersist() {
        if (this.raisedAt == null) {
            this.raisedAt = Instant.now();
        }
        if (this.status == null) {
            this.status = AlarmStatus.ACTIVE;
        }
    }
}
