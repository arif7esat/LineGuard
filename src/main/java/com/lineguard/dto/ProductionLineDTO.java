package com.lineguard.dto;

import com.lineguard.model.ProductionLineState;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

public class ProductionLineDTO {

    /** Request body for creating a production line. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Name must not be blank")
        private String name;
        private String description;
    }

    /** Full representation of a production line returned by the API. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private ProductionLineState state;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
