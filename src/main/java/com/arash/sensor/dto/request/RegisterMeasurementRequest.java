package com.arash.sensor.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterMeasurementRequest {
    @NotNull
    private Integer co2;
    @NotNull
    private ZonedDateTime time;
}
