package com.lineguard.service;

import com.lineguard.model.*;
import com.lineguard.repository.AlarmRepository;
import com.lineguard.repository.AlarmThresholdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlarmService – alarm engine")
class AlarmServiceTest {

    @Mock
    private AlarmRepository alarmRepository;

    @Mock
    private AlarmThresholdRepository thresholdRepository;

    @InjectMocks
    private AlarmService alarmService;

    private ProductionLine line;
    private AlarmThreshold threshold;

    @BeforeEach
    void setUp() {
        line = ProductionLine.builder()
                .id(1L)
                .name("Line A")
                .state(ProductionLineState.RUNNING)
                .build();

        threshold = AlarmThreshold.builder()
                .id(1L)
                .productionLine(line)
                .sensorName("temperature")
                .warningThreshold(80.0)
                .criticalThreshold(100.0)
                .unit("°C")
                .build();
    }

    @Test
    @DisplayName("no alarm when reading is below warning threshold")
    void noAlarmBelowWarning() {
        SensorReading reading = buildReading(70.0);
        when(thresholdRepository.findByProductionLineIdAndSensorName(1L, "temperature"))
                .thenReturn(Optional.of(threshold));

        Optional<Alarm> result = alarmService.evaluate(line, reading);

        assertThat(result).isEmpty();
        verify(alarmRepository, never()).save(any());
    }

    @Test
    @DisplayName("WARNING alarm when reading is at warning threshold")
    void warningAlarmAtWarningThreshold() {
        SensorReading reading = buildReading(80.0);
        when(thresholdRepository.findByProductionLineIdAndSensorName(1L, "temperature"))
                .thenReturn(Optional.of(threshold));
        when(alarmRepository.save(any(Alarm.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Alarm> result = alarmService.evaluate(line, reading);

        assertThat(result).isPresent();
        assertThat(result.get().getSeverity()).isEqualTo(AlarmSeverity.WARNING);
        assertThat(result.get().getThresholdValue()).isEqualTo(80.0);
    }

    @Test
    @DisplayName("CRITICAL alarm when reading is at critical threshold")
    void criticalAlarmAtCriticalThreshold() {
        SensorReading reading = buildReading(100.0);
        when(thresholdRepository.findByProductionLineIdAndSensorName(1L, "temperature"))
                .thenReturn(Optional.of(threshold));
        when(alarmRepository.save(any(Alarm.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Alarm> result = alarmService.evaluate(line, reading);

        assertThat(result).isPresent();
        assertThat(result.get().getSeverity()).isEqualTo(AlarmSeverity.CRITICAL);
        assertThat(result.get().getThresholdValue()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("no alarm when no threshold configured for sensor")
    void noAlarmWhenNoThresholdConfigured() {
        SensorReading reading = buildReading(999.0);
        when(thresholdRepository.findByProductionLineIdAndSensorName(1L, "temperature"))
                .thenReturn(Optional.empty());

        Optional<Alarm> result = alarmService.evaluate(line, reading);

        assertThat(result).isEmpty();
        verify(alarmRepository, never()).save(any());
    }

    @Test
    @DisplayName("alarm message contains sensor name and line name")
    void alarmMessageContainsContext() {
        SensorReading reading = buildReading(95.0);
        when(thresholdRepository.findByProductionLineIdAndSensorName(1L, "temperature"))
                .thenReturn(Optional.of(threshold));
        ArgumentCaptor<Alarm> captor = ArgumentCaptor.forClass(Alarm.class);
        when(alarmRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        alarmService.evaluate(line, reading);

        Alarm saved = captor.getValue();
        assertThat(saved.getMessage()).contains("temperature");
        assertThat(saved.getMessage()).contains("Line A");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private SensorReading buildReading(double value) {
        return SensorReading.builder()
                .id(10L)
                .productionLine(line)
                .sensorName("temperature")
                .value(value)
                .unit("°C")
                .build();
    }
}
