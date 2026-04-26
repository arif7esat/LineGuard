package com.lineguard.controller;

import com.lineguard.dto.AlarmDTO;
import com.lineguard.service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for global alarm management (list all, acknowledge, resolve).
 */
@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping
    public List<AlarmDTO.Response> getAllAlarms() {
        return alarmService.getAllAlarms();
    }

    @PostMapping("/{id}/acknowledge")
    public AlarmDTO.Response acknowledge(@PathVariable Long id) {
        return alarmService.acknowledgeAlarm(id);
    }

    @PostMapping("/{id}/resolve")
    public AlarmDTO.Response resolve(@PathVariable Long id) {
        return alarmService.resolveAlarm(id);
    }
}
