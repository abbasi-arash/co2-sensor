package com.arash.sensor.rest;

import com.arash.sensor.dto.enums.SensorStatus;
import com.arash.sensor.dto.request.RegisterMeasurementRequest;
import com.arash.sensor.dto.response.MetricsResponse;
import com.arash.sensor.dto.response.SensorStatusResponse;
import com.arash.sensor.service.SensorService;
import com.arash.sensor.web.rest.SensorResource;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(SensorResource.class)
public class SensorResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SensorService sensorService;

    @Test
    public void registerMeasurements() throws Exception {
        RegisterMeasurementRequest exampleJson = new RegisterMeasurementRequest(200, ZonedDateTime.now());
        Mockito.doNothing().when(sensorService).registerMeasurement(Mockito.any(RegisterMeasurementRequest.class), Mockito.anyString());
        mockMvc.perform(post("/api/v1/sensors/38112cac-6fd9-40c2-a270-33e85b50c40f/measurements").content(convertObjectToJsonBytes(exampleJson))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void findSensorStatus() throws Exception {
        SensorStatusResponse returnResponse = new SensorStatusResponse(SensorStatus.OK);
        Mockito.when(sensorService.findStatus(Mockito.anyString())).thenReturn(returnResponse);
        mockMvc.perform(get("/api/v1/sensors/38112cac-6fd9-40c2-a270-33e85b50c40f")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"));
    }

    @Test
    public void findSensorMetrics() throws Exception {
        MetricsResponse metricsResponse = new MetricsResponse(2500,1000);
        Mockito.when(sensorService.findMetrics(Mockito.anyString())).thenReturn(metricsResponse);
        mockMvc.perform(get("/api/v1/sensors/38112cac-6fd9-40c2-a270-33e85b50c40f/metrics")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.maxLast30Days").value("2500"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.avgLast30Days").value("1000"));
    }

    public static byte[] convertObjectToJsonBytes(Object object)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);

        return mapper.writeValueAsBytes(object);
    }

}
