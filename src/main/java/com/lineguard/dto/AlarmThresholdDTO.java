package com.lineguard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AlarmThresholdDTO {

    /** Request body for creating or updating a threshold. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Sensor name must not be blank")
        private String sensorName;
        @Positive(message = "Warning threshold must be positive")
        private double warningThreshold;
        @Positive(message = "Critical threshold must be positive")
        private double criticalThreshold;
        @NotBlank(message = "Unit must not be blank")
        private String unit;
    }

    /** Response representation of an alarm threshold. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long productionLineId;
        private String sensorName;
        private double warningThreshold;
        private double criticalThreshold;
        private String unit;
    }
}
