package com.arash.sensor.service.event;

import com.arash.sensor.dto.request.RegisterMeasurementRequest;
import lombok.Data;

@Data
public class MeasurementEvent {
    private RegisterMeasurementRequest registerMeasurementRequest;
    private String uuid;

    public MeasurementEvent(RegisterMeasurementRequest registerMeasurementRequest,String uuid) {
        this.registerMeasurementRequest = registerMeasurementRequest;
        this.uuid = uuid;
    }
}
