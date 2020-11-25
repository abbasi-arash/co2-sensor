package com.arash.sensor.dto.response;

import com.arash.sensor.dto.enums.SensorStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SensorStatusResponse {
    private SensorStatus status;
}
