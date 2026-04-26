package com.lineguard.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Configures the threshold values that trigger alarms for a specific sensor
 * on a production line.
 */
@Entity
@Table(name = "alarm_thresholds",
       uniqueConstraints = @UniqueConstraint(columnNames = {"production_line_id", "sensor_name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_line_id", nullable = false)
    private ProductionLine productionLine;

    @Column(name = "sensor_name", nullable = false)
    private String sensorName;

    /** Readings above this value trigger a WARNING alarm. */
    @Column(nullable = false)
    private double warningThreshold;

    /** Readings above this value trigger a CRITICAL alarm. */
    @Column(nullable = false)
    private double criticalThreshold;

    /** Physical unit label for documentation purposes. */
    @Column(nullable = false)
    private String unit;
}
