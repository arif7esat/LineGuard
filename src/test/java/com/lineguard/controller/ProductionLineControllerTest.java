package com.lineguard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lineguard.dto.ProductionLineDTO;
import com.lineguard.dto.SensorReadingDTO;
import com.lineguard.dto.AlarmThresholdDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the ProductionLine REST API using MockMvc and an
 * in-memory H2 database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("ProductionLineController – integration")
class ProductionLineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/lines – creates a line with IDLE state")
    void createLine() throws Exception {
        ProductionLineDTO.CreateRequest req = ProductionLineDTO.CreateRequest.builder()
                .name("Assembly-1")
                .description("Main assembly line")
                .build();

        mockMvc.perform(post("/api/lines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Assembly-1"))
                .andExpect(jsonPath("$.state").value("IDLE"));
    }

    @Test
    @DisplayName("POST /api/lines – 400 when name is blank")
    void createLineBlankName() throws Exception {
        ProductionLineDTO.CreateRequest req = ProductionLineDTO.CreateRequest.builder()
                .name("   ")
                .build();

        mockMvc.perform(post("/api/lines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/lines – returns list of lines")
    void getAllLines() throws Exception {
        createTestLine("Line-A");
        createTestLine("Line-B");

        mockMvc.perform(get("/api/lines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/lines/{id} – 404 for unknown id")
    void getUnknownLine() throws Exception {
        mockMvc.perform(get("/api/lines/999"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // State machine transitions
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("start → pause → resume → stop flow")
    void fullLifecycle() throws Exception {
        long id = createTestLine("Line-SM");

        // IDLE → RUNNING
        mockMvc.perform(post("/api/lines/" + id + "/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("RUNNING"));

        // RUNNING → PAUSED
        mockMvc.perform(post("/api/lines/" + id + "/pause"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("PAUSED"));

        // PAUSED → RUNNING
        mockMvc.perform(post("/api/lines/" + id + "/resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("RUNNING"));

        // RUNNING → STOPPED
        mockMvc.perform(post("/api/lines/" + id + "/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("STOPPED"));
    }

    @Test
    @DisplayName("fault + reset flow")
    void faultResetFlow() throws Exception {
        long id = createTestLine("Line-F");

        mockMvc.perform(post("/api/lines/" + id + "/start")).andExpect(status().isOk());

        // RUNNING → FAULT
        mockMvc.perform(post("/api/lines/" + id + "/fault"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("FAULT"));

        // FAULT → IDLE
        mockMvc.perform(post("/api/lines/" + id + "/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("IDLE"));
    }

    @Test
    @DisplayName("invalid transition returns 409")
    void invalidTransitionReturns409() throws Exception {
        long id = createTestLine("Line-G");

        // IDLE → pause is invalid
        mockMvc.perform(post("/api/lines/" + id + "/pause"))
                .andExpect(status().isConflict());
    }

    // -------------------------------------------------------------------------
    // Sensor readings
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/lines/{id}/readings – creates a reading")
    void addReading() throws Exception {
        long id = createTestLine("Line-R");
        SensorReadingDTO.CreateRequest req = SensorReadingDTO.CreateRequest.builder()
                .sensorName("temperature")
                .value(75.0)
                .unit("°C")
                .build();

        mockMvc.perform(post("/api/lines/" + id + "/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sensorName").value("temperature"))
                .andExpect(jsonPath("$.value").value(75.0));
    }

    // -------------------------------------------------------------------------
    // Alarm thresholds
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/lines/{id}/thresholds – creates a threshold")
    void setThreshold() throws Exception {
        long id = createTestLine("Line-T");
        AlarmThresholdDTO.CreateRequest req = AlarmThresholdDTO.CreateRequest.builder()
                .sensorName("pressure")
                .warningThreshold(5.0)
                .criticalThreshold(10.0)
                .unit("bar")
                .build();

        mockMvc.perform(post("/api/lines/" + id + "/thresholds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sensorName").value("pressure"))
                .andExpect(jsonPath("$.warningThreshold").value(5.0));
    }

    // -------------------------------------------------------------------------
    // Alarm integration
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("sensor reading above CRITICAL threshold raises alarm")
    void readingRaisesAlarm() throws Exception {
        long id = createTestLine("Line-AL");

        // configure threshold
        AlarmThresholdDTO.CreateRequest threshold = AlarmThresholdDTO.CreateRequest.builder()
                .sensorName("temperature")
                .warningThreshold(80.0)
                .criticalThreshold(100.0)
                .unit("°C")
                .build();
        mockMvc.perform(post("/api/lines/" + id + "/thresholds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(threshold)))
                .andExpect(status().isCreated());

        // submit critical reading
        SensorReadingDTO.CreateRequest reading = SensorReadingDTO.CreateRequest.builder()
                .sensorName("temperature")
                .value(120.0)
                .unit("°C")
                .build();
        mockMvc.perform(post("/api/lines/" + id + "/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reading)))
                .andExpect(status().isCreated());

        // verify alarm was raised
        mockMvc.perform(get("/api/lines/" + id + "/alarms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].severity").value("CRITICAL"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private long createTestLine(String name) throws Exception {
        ProductionLineDTO.CreateRequest req = ProductionLineDTO.CreateRequest.builder()
                .name(name)
                .build();
        MvcResult result = mockMvc.perform(post("/api/lines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }
}
