package com.arash.sensor.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "TBL_SENSOR")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IdClass(SensorId.class)
public class SensorEntity {
    @Id
    @Column(name = "uuid")
    private String uuid;

    @Id
    @Column(name = "time")
    private ZonedDateTime time;

    @Column(name = "CO2_LEVEL")
    private Integer co2Level;

}
