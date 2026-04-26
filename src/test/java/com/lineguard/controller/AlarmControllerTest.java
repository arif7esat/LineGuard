package com.lineguard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lineguard.dto.AlarmThresholdDTO;
import com.lineguard.dto.ProductionLineDTO;
import com.lineguard.dto.SensorReadingDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Alarm REST API using MockMvc and an H2 database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("AlarmController – integration")
class AlarmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/alarms – returns all alarms globally")
    void getAllAlarms() throws Exception {
        raiseAlarmOnLine("L1", "temperature", 80.0, 100.0, 120.0);
        raiseAlarmOnLine("L2", "pressure", 5.0, 10.0, 12.0);

        mockMvc.perform(get("/api/alarms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("POST /api/alarms/{id}/acknowledge – changes status to ACKNOWLEDGED")
    void acknowledgeAlarm() throws Exception {
        long alarmId = raiseAlarmOnLine("L3", "temperature", 80.0, 100.0, 120.0);

        mockMvc.perform(post("/api/alarms/" + alarmId + "/acknowledge"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACKNOWLEDGED"));
    }

    @Test
    @DisplayName("POST /api/alarms/{id}/resolve – changes status to RESOLVED")
    void resolveAlarm() throws Exception {
        long alarmId = raiseAlarmOnLine("L4", "temperature", 80.0, 100.0, 120.0);

        mockMvc.perform(post("/api/alarms/" + alarmId + "/resolve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    @DisplayName("POST /api/alarms/{id}/acknowledge – 404 for unknown alarm")
    void acknowledgeUnknownAlarm() throws Exception {
        mockMvc.perform(post("/api/alarms/999/acknowledge"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // Helper: create a line, set threshold, submit reading above critical
    // -------------------------------------------------------------------------

    private long raiseAlarmOnLine(String lineName, String sensor,
                                   double warning, double critical, double readingValue)
            throws Exception {
        // create line
        ProductionLineDTO.CreateRequest lineReq = ProductionLineDTO.CreateRequest.builder()
                .name(lineName).build();
        MvcResult lineResult = mockMvc.perform(post("/api/lines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lineReq)))
                .andReturn();
        long lineId = objectMapper.readTree(lineResult.getResponse().getContentAsString())
                .get("id").asLong();

        // set threshold
        AlarmThresholdDTO.CreateRequest threshReq = AlarmThresholdDTO.CreateRequest.builder()
                .sensorName(sensor)
                .warningThreshold(warning)
                .criticalThreshold(critical)
                .unit("unit")
                .build();
        mockMvc.perform(post("/api/lines/" + lineId + "/thresholds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(threshReq)));

        // submit reading above critical
        SensorReadingDTO.CreateRequest readingReq = SensorReadingDTO.CreateRequest.builder()
                .sensorName(sensor).value(readingValue).unit("unit").build();
        mockMvc.perform(post("/api/lines/" + lineId + "/readings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(readingReq)));

        // fetch and return alarm id
        MvcResult alarmsResult = mockMvc.perform(get("/api/lines/" + lineId + "/alarms"))
                .andReturn();
        return objectMapper.readTree(alarmsResult.getResponse().getContentAsString())
                .get(0).get("id").asLong();
    }
}
