package com.lineguard.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * A single measurement emitted by a sensor on a production line.
 */
@Entity
@Table(name = "sensor_readings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_line_id", nullable = false)
    private ProductionLine productionLine;

    /** Logical name of the sensor, e.g. "temperature", "pressure". */
    @Column(nullable = false)
    private String sensorName;

    @Column(name = "reading_value", nullable = false)
    private double value;

    /** Physical unit of the reading, e.g. "°C", "bar", "rpm". */
    @Column(nullable = false)
    private String unit;

    @Column(nullable = false)
    private Instant recordedAt;

    @PrePersist
    public void prePersist() {
        if (this.recordedAt == null) {
            this.recordedAt = Instant.now();
        }
    }
}
