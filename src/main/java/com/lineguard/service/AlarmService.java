package com.lineguard.service;

import com.lineguard.dto.AlarmDTO;
import com.lineguard.exception.ResourceNotFoundException;
import com.lineguard.model.*;
import com.lineguard.repository.AlarmRepository;
import com.lineguard.repository.AlarmThresholdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Threshold-based alarm engine.
 *
 * <p>When a {@link SensorReading} is submitted, {@link #evaluate} checks whether
 * it exceeds the configured WARNING or CRITICAL thresholds and raises an
 * {@link Alarm} accordingly.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final AlarmThresholdRepository thresholdRepository;

    // -------------------------------------------------------------------------
    // Core alarm engine
    // -------------------------------------------------------------------------

    /**
     * Evaluates a sensor reading against configured thresholds and raises an
     * alarm when a threshold is exceeded.
     *
     * @param line    the production line that owns the reading
     * @param reading the sensor reading to evaluate
     * @return the raised alarm, or empty if no threshold was exceeded
     */
    public Optional<Alarm> evaluate(ProductionLine line, SensorReading reading) {
        Optional<AlarmThreshold> opt = thresholdRepository
                .findByProductionLineIdAndSensorName(line.getId(), reading.getSensorName());

        if (opt.isEmpty()) {
            return Optional.empty();
        }

        AlarmThreshold threshold = opt.get();
        AlarmSeverity severity = null;
        double limitValue = 0;

        if (reading.getValue() >= threshold.getCriticalThreshold()) {
            severity = AlarmSeverity.CRITICAL;
            limitValue = threshold.getCriticalThreshold();
        } else if (reading.getValue() >= threshold.getWarningThreshold()) {
            severity = AlarmSeverity.WARNING;
            limitValue = threshold.getWarningThreshold();
        }

        if (severity == null) {
            return Optional.empty();
        }

        String message = String.format(
                "[%s] %s reading %.2f %s exceeded %s threshold %.2f %s on line '%s'.",
                severity, reading.getSensorName(), reading.getValue(), reading.getUnit(),
                severity.name().toLowerCase(), limitValue, reading.getUnit(), line.getName());

        Alarm alarm = Alarm.builder()
                .productionLine(line)
                .sensorName(reading.getSensorName())
                .triggerValue(reading.getValue())
                .thresholdValue(limitValue)
                .severity(severity)
                .status(AlarmStatus.ACTIVE)
                .message(message)
                .raisedAt(Instant.now())
                .build();

        return Optional.of(alarmRepository.save(alarm));
    }

    // -------------------------------------------------------------------------
    // Alarm queries
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<AlarmDTO.Response> getAllAlarms() {
        return alarmRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlarmDTO.Response> getAlarmsForLine(Long lineId) {
        return alarmRepository.findByProductionLineId(lineId).stream()
                .map(this::toResponse)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Alarm lifecycle
    // -------------------------------------------------------------------------

    public AlarmDTO.Response acknowledgeAlarm(Long alarmId) {
        Alarm alarm = findAlarmById(alarmId);
        alarm.setStatus(AlarmStatus.ACKNOWLEDGED);
        alarm.setAcknowledgedAt(Instant.now());
        return toResponse(alarmRepository.save(alarm));
    }

    public AlarmDTO.Response resolveAlarm(Long alarmId) {
        Alarm alarm = findAlarmById(alarmId);
        alarm.setStatus(AlarmStatus.RESOLVED);
        alarm.setResolvedAt(Instant.now());
        return toResponse(alarmRepository.save(alarm));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Alarm findAlarmById(Long id) {
        return alarmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alarm not found: " + id));
    }

    private AlarmDTO.Response toResponse(Alarm a) {
        return AlarmDTO.Response.builder()
                .id(a.getId())
                .productionLineId(a.getProductionLine().getId())
                .sensorName(a.getSensorName())
                .triggerValue(a.getTriggerValue())
                .thresholdValue(a.getThresholdValue())
                .severity(a.getSeverity())
                .status(a.getStatus())
                .message(a.getMessage())
                .raisedAt(a.getRaisedAt())
                .acknowledgedAt(a.getAcknowledgedAt())
                .resolvedAt(a.getResolvedAt())
                .build();
    }
}
