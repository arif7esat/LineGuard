package com.lineguard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

public class SensorReadingDTO {

    /** Request body for submitting a sensor reading. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Sensor name must not be blank")
        private String sensorName;
        private double value;
        @NotBlank(message = "Unit must not be blank")
        private String unit;
    }

    /** Response representation of a sensor reading. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long productionLineId;
        private String sensorName;
        private double value;
        private String unit;
        private Instant recordedAt;
    }
}
