package com.lineguard.controller;

import com.lineguard.dto.AlarmDTO;
import com.lineguard.dto.AlarmThresholdDTO;
import com.lineguard.dto.ProductionLineDTO;
import com.lineguard.dto.SensorReadingDTO;
import com.lineguard.service.AlarmService;
import com.lineguard.service.ProductionLineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for production line lifecycle management, sensor ingestion, and
 * threshold configuration.
 */
@RestController
@RequestMapping("/api/lines")
@RequiredArgsConstructor
public class ProductionLineController {

    private final ProductionLineService lineService;
    private final AlarmService alarmService;

    // -------------------------------------------------------------------------
    // Production line CRUD
    // -------------------------------------------------------------------------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductionLineDTO.Response createLine(@RequestBody @Valid ProductionLineDTO.CreateRequest request) {
        return lineService.createLine(request);
    }

    @GetMapping
    public List<ProductionLineDTO.Response> getAllLines() {
        return lineService.getAllLines();
    }

    @GetMapping("/{id}")
    public ProductionLineDTO.Response getLine(@PathVariable Long id) {
        return lineService.getLine(id);
    }

    // -------------------------------------------------------------------------
    // State transitions
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/start")
    public ProductionLineDTO.Response start(@PathVariable Long id) {
        return lineService.applyEvent(id, "start");
    }

    @PostMapping("/{id}/pause")
    public ProductionLineDTO.Response pause(@PathVariable Long id) {
        return lineService.applyEvent(id, "pause");
    }

    @PostMapping("/{id}/resume")
    public ProductionLineDTO.Response resume(@PathVariable Long id) {
        return lineService.applyEvent(id, "resume");
    }

    @PostMapping("/{id}/stop")
    public ProductionLineDTO.Response stop(@PathVariable Long id) {
        return lineService.applyEvent(id, "stop");
    }

    @PostMapping("/{id}/fault")
    public ProductionLineDTO.Response fault(@PathVariable Long id) {
        return lineService.applyEvent(id, "fault");
    }

    @PostMapping("/{id}/reset")
    public ProductionLineDTO.Response reset(@PathVariable Long id) {
        return lineService.applyEvent(id, "reset");
    }

    // -------------------------------------------------------------------------
    // Sensor readings
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/readings")
    @ResponseStatus(HttpStatus.CREATED)
    public SensorReadingDTO.Response addReading(
            @PathVariable Long id,
            @RequestBody @Valid SensorReadingDTO.CreateRequest request) {
        return lineService.addReading(id, request);
    }

    @GetMapping("/{id}/readings")
    public List<SensorReadingDTO.Response> getReadings(@PathVariable Long id) {
        return lineService.getReadings(id);
    }

    // -------------------------------------------------------------------------
    // Alarm thresholds
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/thresholds")
    @ResponseStatus(HttpStatus.CREATED)
    public AlarmThresholdDTO.Response setThreshold(
            @PathVariable Long id,
            @RequestBody @Valid AlarmThresholdDTO.CreateRequest request) {
        return lineService.setThreshold(id, request);
    }

    @GetMapping("/{id}/thresholds")
    public List<AlarmThresholdDTO.Response> getThresholds(@PathVariable Long id) {
        return lineService.getThresholds(id);
    }

    // -------------------------------------------------------------------------
    // Alarms (per line)
    // -------------------------------------------------------------------------

    @GetMapping("/{id}/alarms")
    public List<AlarmDTO.Response> getAlarms(@PathVariable Long id) {
        return alarmService.getAlarmsForLine(id);
    }
}
