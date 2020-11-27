package com.arash.sensor.domain;

import com.arash.sensor.dto.enums.SensorStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "TBL_SENSOR_STATUS")
public class SensorStatusEntity {
    @Id
    private String uuid;

    @Column(name = "status")
    private SensorStatus status;

}
