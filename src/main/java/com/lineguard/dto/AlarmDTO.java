package com.lineguard.dto;

import com.lineguard.model.AlarmSeverity;
import com.lineguard.model.AlarmStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

public class AlarmDTO {

    /** Response representation of an alarm. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long productionLineId;
        private String sensorName;
        private double triggerValue;
        private double thresholdValue;
        private AlarmSeverity severity;
        private AlarmStatus status;
        private String message;
        private Instant raisedAt;
        private Instant acknowledgedAt;
        private Instant resolvedAt;
    }
}
