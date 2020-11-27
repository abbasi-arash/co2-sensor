package com.arash.sensor.repository;

import com.arash.sensor.domain.SensorStatusEntity;
import com.arash.sensor.dto.enums.SensorStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class SensorStatusRepositoryTest {
    @Autowired
    private SensorStatusRepository sensorStatusRepository;

    @Test
    public void save_sensor_status_entity_test() {
        SensorStatusEntity sensorStatusEntity = new SensorStatusEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", SensorStatus.OK);
        SensorStatusEntity result = sensorStatusRepository.save(sensorStatusEntity);
        assertEquals(result, sensorStatusEntity);
    }
}
