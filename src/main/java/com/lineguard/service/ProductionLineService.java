package com.lineguard.service;

import com.lineguard.dto.AlarmThresholdDTO;
import com.lineguard.dto.ProductionLineDTO;
import com.lineguard.dto.SensorReadingDTO;
import com.lineguard.exception.ResourceNotFoundException;
import com.lineguard.model.*;
import com.lineguard.repository.AlarmThresholdRepository;
import com.lineguard.repository.ProductionLineRepository;
import com.lineguard.repository.SensorReadingRepository;
import com.lineguard.statemachine.ProductionLineStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for production line lifecycle management, sensor data ingestion,
 * and threshold configuration.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductionLineService {

    private final ProductionLineRepository lineRepository;
    private final SensorReadingRepository readingRepository;
    private final AlarmThresholdRepository thresholdRepository;
    private final ProductionLineStateMachine stateMachine;
    private final AlarmService alarmService;

    // -------------------------------------------------------------------------
    // Production line CRUD
    // -------------------------------------------------------------------------

    public ProductionLineDTO.Response createLine(ProductionLineDTO.CreateRequest request) {
        ProductionLine line = ProductionLine.builder()
                .name(request.getName())
                .description(request.getDescription())
                .state(ProductionLineState.IDLE)
                .build();
        return toResponse(lineRepository.save(line));
    }

    @Transactional(readOnly = true)
    public List<ProductionLineDTO.Response> getAllLines() {
        return lineRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductionLineDTO.Response getLine(Long id) {
        return toResponse(findLineById(id));
    }

    // -------------------------------------------------------------------------
    // State transitions
    // -------------------------------------------------------------------------

    public ProductionLineDTO.Response applyEvent(Long id, String event) {
        ProductionLine line = findLineById(id);
        ProductionLineState newState = stateMachine.transition(line.getState(), event);
        line.setState(newState);
        return toResponse(lineRepository.save(line));
    }

    // -------------------------------------------------------------------------
    // Sensor readings
    // -------------------------------------------------------------------------

    public SensorReadingDTO.Response addReading(Long lineId, SensorReadingDTO.CreateRequest request) {
        ProductionLine line = findLineById(lineId);
        SensorReading reading = SensorReading.builder()
                .productionLine(line)
                .sensorName(request.getSensorName())
                .value(request.getValue())
                .unit(request.getUnit())
                .build();
        SensorReading saved = readingRepository.save(reading);

        // Evaluate thresholds and potentially raise alarms
        alarmService.evaluate(line, saved);

        return toReadingResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SensorReadingDTO.Response> getReadings(Long lineId) {
        findLineById(lineId); // ensure the line exists
        return readingRepository.findByProductionLineIdOrderByRecordedAtDesc(lineId)
                .stream()
                .map(this::toReadingResponse)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Alarm thresholds
    // -------------------------------------------------------------------------

    public AlarmThresholdDTO.Response setThreshold(Long lineId, AlarmThresholdDTO.CreateRequest request) {
        ProductionLine line = findLineById(lineId);
        AlarmThreshold threshold = thresholdRepository
                .findByProductionLineIdAndSensorName(lineId, request.getSensorName())
                .orElseGet(() -> AlarmThreshold.builder()
                        .productionLine(line)
                        .sensorName(request.getSensorName())
                        .build());
        threshold.setWarningThreshold(request.getWarningThreshold());
        threshold.setCriticalThreshold(request.getCriticalThreshold());
        threshold.setUnit(request.getUnit());
        return toThresholdResponse(thresholdRepository.save(threshold));
    }

    @Transactional(readOnly = true)
    public List<AlarmThresholdDTO.Response> getThresholds(Long lineId) {
        findLineById(lineId);
        return thresholdRepository.findByProductionLineId(lineId)
                .stream()
                .map(this::toThresholdResponse)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    public ProductionLine findLineById(Long id) {
        return lineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Production line not found: " + id));
    }

    private ProductionLineDTO.Response toResponse(ProductionLine line) {
        return ProductionLineDTO.Response.builder()
                .id(line.getId())
                .name(line.getName())
                .description(line.getDescription())
                .state(line.getState())
                .createdAt(line.getCreatedAt())
                .updatedAt(line.getUpdatedAt())
                .build();
    }

    private SensorReadingDTO.Response toReadingResponse(SensorReading r) {
        return SensorReadingDTO.Response.builder()
                .id(r.getId())
                .productionLineId(r.getProductionLine().getId())
                .sensorName(r.getSensorName())
                .value(r.getValue())
                .unit(r.getUnit())
                .recordedAt(r.getRecordedAt())
                .build();
    }

    private AlarmThresholdDTO.Response toThresholdResponse(AlarmThreshold t) {
        return AlarmThresholdDTO.Response.builder()
                .id(t.getId())
                .productionLineId(t.getProductionLine().getId())
                .sensorName(t.getSensorName())
                .warningThreshold(t.getWarningThreshold())
                .criticalThreshold(t.getCriticalThreshold())
                .unit(t.getUnit())
                .build();
    }
}
