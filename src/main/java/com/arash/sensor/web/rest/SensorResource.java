package com.arash.sensor.web.rest;

import com.arash.sensor.dto.request.RegisterMeasurementRequest;
import com.arash.sensor.dto.response.MetricsResponse;
import com.arash.sensor.dto.response.SensorStatusResponse;
import com.arash.sensor.service.SensorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api")
public class SensorResource {

    private final SensorService sensorService;

    @Autowired
    public SensorResource(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @PostMapping(value = "/v1/sensors/{uuid}/measurements")
    public ResponseEntity registerMeasurements
            (@PathVariable(name = "uuid") String uuid,
             @Valid @RequestBody RegisterMeasurementRequest registerMeasurementRequest) {
        sensorService.registerMeasurement(registerMeasurementRequest, uuid);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(value = "/v1/sensors/{uuid}")
    public ResponseEntity<SensorStatusResponse> findSensorStatus(@PathVariable(name = "uuid") String uuid) {
        return new ResponseEntity<>(sensorService.findStatus(uuid), HttpStatus.OK);
    }

    @GetMapping(value = "/v1/sensors/{uuid}/metrics")
    public ResponseEntity<MetricsResponse> findSensorMetrics(@PathVariable(name = "uuid") String uuid) {
        return new ResponseEntity<>(sensorService.findMetrics(uuid), HttpStatus.OK);
    }
}
