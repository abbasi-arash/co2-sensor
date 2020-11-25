package com.arash.sensor.service;

import com.arash.sensor.domain.SensorEntity;
import com.arash.sensor.repository.SensorRepository;
import com.arash.sensor.dto.enums.SensorStatus;
import com.arash.sensor.dto.request.RegisterMeasurementRequest;
import com.arash.sensor.dto.response.MetricsResponse;
import com.arash.sensor.dto.response.SensorStatusResponse;
import com.arash.sensor.service.event.MeasurementEvent;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class SensorServiceTest {

    @Mock
    private SensorRepository sensorRepository;

    @InjectMocks
    private SensorService sensorService;

    @Test
    public void register_measurement_async_test(){
        ZonedDateTime time = ZonedDateTime.now();
        RegisterMeasurementRequest registerMeasurementRequest = new RegisterMeasurementRequest(400,time);
        MeasurementEvent event = new MeasurementEvent(registerMeasurementRequest,"38112cac-6fd9-40c2-a270-33e85b50c40f");
        SensorEntity entity = SensorEntity.builder()
                .co2Level(400)
                .time(time)
                .uuid(event.getUuid())
                .build();
        sensorService.asyncRegisterMeasurement(event);
        verify(sensorRepository, times(1)).save(entity);
    }

    @Test
    public void when_status_return_warn() {
        SensorEntity sensorEntity = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now(), 2000);
        Mockito.when(sensorRepository.findTop3ByUuidOrderByTimeDesc(Mockito.anyString())).thenReturn(Lists.newArrayList(sensorEntity));
        SensorStatusResponse response = sensorService.findStatus("38112cac-6fd9-40c2-a270-33e85b50c40f");
        assertEquals(response, new SensorStatusResponse(SensorStatus.WARN));
    }

    @Test
    public void when_status_return_ok() {
        SensorEntity sensorEntity1 = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now(), 1000);
        SensorEntity sensorEntity2 = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now().minusMinutes(1), 1500);
        SensorEntity sensorEntity3 = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now().minusMinutes(2), 1800);
        Mockito.when(sensorRepository.findTop3ByUuidOrderByTimeDesc(Mockito.anyString())).thenReturn(Lists.newArrayList(sensorEntity1, sensorEntity2, sensorEntity3));
        SensorStatusResponse response = sensorService.findStatus("38112cac-6fd9-40c2-a270-33e85b50c40f");
        assertEquals(response, new SensorStatusResponse(SensorStatus.OK));
    }

    @Test
    public void when_status_return_alert() {
        SensorEntity sensorEntity1 = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now(), 2500);
        SensorEntity sensorEntity2 = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now().minusMinutes(1), 4000);
        SensorEntity sensorEntity3 = new SensorEntity("38112cac-6fd9-40c2-a270-33e85b50c40f", ZonedDateTime.now().minusMinutes(2), 3000);
        Mockito.when(sensorRepository.findTop3ByUuidOrderByTimeDesc(Mockito.anyString())).thenReturn(Lists.newArrayList(sensorEntity1, sensorEntity2, sensorEntity3));
        SensorStatusResponse response = sensorService.findStatus("38112cac-6fd9-40c2-a270-33e85b50c40f");
        assertEquals(response, new SensorStatusResponse(SensorStatus.ALERT));
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
