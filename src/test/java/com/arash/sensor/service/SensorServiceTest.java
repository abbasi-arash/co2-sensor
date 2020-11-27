package com.arash.sensor.service;

import com.arash.sensor.domain.SensorEntity;
import com.arash.sensor.domain.SensorStatusEntity;
import com.arash.sensor.dto.enums.SensorStatus;
import com.arash.sensor.dto.request.RegisterMeasurementRequest;
import com.arash.sensor.dto.response.MetricsResponse;
import com.arash.sensor.repository.SensorRepository;
import com.arash.sensor.repository.SensorStatusRepository;
import com.arash.sensor.service.event.MeasurementEvent;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = {"app.co2.accept.level=2000"})
public class SensorServiceTest {

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private SensorStatusRepository sensorStatusRepository;

    @InjectMocks
    private SensorService sensorService;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(sensorService, "acceptableCo2Level", 2000);
    }

    @Test
    public void register_measurement_async_test() {
        ZonedDateTime time = ZonedDateTime.now();
        RegisterMeasurementRequest registerMeasurementRequest = new RegisterMeasurementRequest(400, time);
        MeasurementEvent event = new MeasurementEvent(registerMeasurementRequest, "38112cac-6fd9-40c2-a270-33e85b50c40f");
        SensorEntity entity = SensorEntity.builder()
                .co2Level(400)
                .time(time)
                .uuid(event.getUuid())
                .build();
        sensorService.asyncRegisterMeasurement(event);
        verify(sensorRepository, times(1)).save(entity);
    }

    @Test
    public void when_last_status_ok_normal_update_status() {
        SensorEntity sensorEntity1 = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now().minusMinutes(1), 1000);
        SensorEntity sensorEntity2 = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now().minusMinutes(2), 1500);
        Mockito.when(sensorRepository.findTop2ByUuidOrderByTimeDesc(Mockito.anyString())).thenReturn(Lists.newArrayList(sensorEntity1, sensorEntity2));
        Mockito.when(sensorStatusRepository.findById(Mockito.anyString())).thenReturn(Optional.of(new SensorStatusEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", SensorStatus.OK)));
        SensorEntity currentSensor = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now(), 1000);
        sensorService.updateStatus(currentSensor);
        SensorStatusEntity expected = new SensorStatusEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", SensorStatus.OK);
        verify(sensorStatusRepository, times(1)).save(expected);
    }

    //
    @Test
    public void when_last_status_alert_current_co2_normal_except_alert() {
        SensorEntity sensorEntity1 = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now().minusMinutes(1), 2100);
        SensorEntity sensorEntity2 = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now().minusMinutes(2), 3000);
        Mockito.when(sensorRepository.findTop2ByUuidOrderByTimeDesc(Mockito.anyString())).thenReturn(Lists.newArrayList(sensorEntity1, sensorEntity2));
        Mockito.when(sensorStatusRepository.findById(Mockito.anyString())).thenReturn(Optional.of(new SensorStatusEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", SensorStatus.ALERT)));
        SensorEntity currentSensor = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now(), 1000);
        sensorService.updateStatus(currentSensor);
        SensorStatusEntity expected = new SensorStatusEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", SensorStatus.ALERT);
        verify(sensorStatusRepository, times(0)).save(expected);
    }

    @Test
    public void when_last_status_ok_current_co2_higher_level_except_warn() {
        SensorEntity sensorEntity1 = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now().minusMinutes(1), 1000);
        SensorEntity sensorEntity2 = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now().minusMinutes(2), 1400);
        Mockito.when(sensorRepository.findTop2ByUuidOrderByTimeDesc(Mockito.anyString())).thenReturn(Lists.newArrayList(sensorEntity1, sensorEntity2));
        Mockito.when(sensorStatusRepository.findById(Mockito.anyString())).thenReturn(Optional.of(new SensorStatusEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", SensorStatus.OK)));
        SensorEntity currentSensor = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now(), 3000);
        sensorService.updateStatus(currentSensor);
        SensorStatusEntity expected = new SensorStatusEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", SensorStatus.WARN);
        verify(sensorStatusRepository, times(1)).save(expected);
    }

    @Test
    public void when_last_status_warn_current_and_2last_co2_higher_level_except_alert() {
        SensorEntity sensorEntity1 = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now().minusMinutes(1), 2500);
        SensorEntity sensorEntity2 = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now().minusMinutes(2), 2600);
        Mockito.when(sensorRepository.findTop2ByUuidOrderByTimeDesc(Mockito.anyString())).thenReturn(Lists.newArrayList(sensorEntity1, sensorEntity2));
        Mockito.when(sensorStatusRepository.findById(Mockito.anyString())).thenReturn(Optional.of(new SensorStatusEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", SensorStatus.WARN)));
        SensorEntity currentSensor = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now(), 2800);
        sensorService.updateStatus(currentSensor);
        SensorStatusEntity expected = new SensorStatusEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", SensorStatus.ALERT);
        verify(sensorStatusRepository, times(1)).save(expected);
    }

    @Test
    public void when_metric_return_data() {
        Mockito.when(sensorRepository.findAverageCo2Level(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(1000);
        Mockito.when(sensorRepository.findMaxCo2Level(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(4000);
        MetricsResponse metricsResponse = sensorService.findMetrics("38112cac-6fd9-40c2-a270-33e85b50c40f");
        assertEquals(metricsResponse.getAvgLast30Days(), Integer.valueOf(1000));
        assertEquals(metricsResponse.getMaxLast30Days(), Integer.valueOf(4000));
    }
}
