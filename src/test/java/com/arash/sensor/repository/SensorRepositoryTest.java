package com.arash.sensor.repository;

import com.arash.sensor.domain.SensorEntity;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.ZonedDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class SensorRepositoryTest {
    @Autowired
    private SensorRepository sensorRepository;

    @Test
    public void save_sensor_entity_test() {
        SensorEntity sensor = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now(), 1600);
        SensorEntity entity = sensorRepository.save(sensor);
        assertEquals(entity, sensor);
    }

    @Test
    public void find_top2_by_uuid_order_by_time_desc_test() {
        String uuid = "38112cac-6fd9-40c2-a270-33e85b50c40f";
        SensorEntity sensorEntity1 = new SensorEntity(uuid, ZonedDateTime.now(), 1600);
        SensorEntity sensorEntity2 = new SensorEntity(uuid, ZonedDateTime.now().minusMinutes(6), 3000);
        SensorEntity sensorEntity3 = new SensorEntity(uuid, ZonedDateTime.now().minusMinutes(4), 650);
        sensorRepository.saveAll(Lists.newArrayList(sensorEntity1, sensorEntity2, sensorEntity3));
        List<SensorEntity> sensors = sensorRepository.findTop2ByUuidOrderByTimeDesc(uuid);
        assertThat(sensors, hasSize(2));
        assertThat(sensors, contains(sensorEntity1, sensorEntity3));
    }

    @Test
    public void find_max_co2_level_test() {
        String uuid = "38112cac-6fd9-40c2-a270-33e85b50c40f";
        SensorEntity sensorEntity1 = new SensorEntity(uuid, ZonedDateTime.now(), 1000);
        SensorEntity sensorEntity2 = new SensorEntity(uuid, ZonedDateTime.now().minusMinutes(4), 2000);
        SensorEntity sensorEntity3 = new SensorEntity(uuid, ZonedDateTime.now().minusMinutes(2), 3000);
        sensorRepository.saveAll(Lists.newArrayList(sensorEntity1, sensorEntity2, sensorEntity3));
        Integer max = sensorRepository.findMaxCo2Level(uuid, ZonedDateTime.now().minusMinutes(4), ZonedDateTime.now());
        assertEquals(max, Integer.valueOf(3000));
    }

    @Test
    public void find_average_co2_level_test() {
        String uuid = "38112cac-6fd9-40c2-a270-33e85b50c40f";
        SensorEntity sensorEntity1 = new SensorEntity(uuid, ZonedDateTime.now(), 1000);
        SensorEntity sensorEntity2 = new SensorEntity(uuid, ZonedDateTime.now().minusMinutes(4), 2000);
        SensorEntity sensorEntity3 = new SensorEntity(uuid, ZonedDateTime.now().minusMinutes(2), 3000);
        sensorRepository.saveAll(Lists.newArrayList(sensorEntity1, sensorEntity2, sensorEntity3));
        Integer max = sensorRepository.findAverageCo2Level(uuid, ZonedDateTime.now().minusMinutes(4), ZonedDateTime.now());
        assertEquals(max, Integer.valueOf(2000));
    }
}
