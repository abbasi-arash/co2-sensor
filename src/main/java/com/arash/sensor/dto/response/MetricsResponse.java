package com.arash.sensor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricsResponse {
    private Integer maxLast30Days;
    private Integer avgLast30Days;
}
