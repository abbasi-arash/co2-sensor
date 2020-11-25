package com.arash.sensor.service;

import com.arash.sensor.domain.SensorEntity;
import com.arash.sensor.repository.SensorRepository;
import com.arash.sensor.dto.enums.SensorStatus;
import com.arash.sensor.dto.request.RegisterMeasurementRequest;
import com.arash.sensor.dto.response.MetricsResponse;
import com.arash.sensor.dto.response.SensorStatusResponse;
import com.arash.sensor.service.event.MeasurementEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SensorService {

    private ApplicationEventPublisher publisher;
    private SensorRepository sensorRepository;

    @Autowired
    public SensorService(ApplicationEventPublisher applicationEventPublisher, SensorRepository sensorRepository) {
        this.publisher = applicationEventPublisher;
        this.sensorRepository = sensorRepository;
    }

    public void registerMeasurement(RegisterMeasurementRequest registerMeasurementRequest, String uuid) {
        publisher.publishEvent(new MeasurementEvent(registerMeasurementRequest, uuid));
    }

    @Async
    @EventListener
    public void asyncRegisterMeasurement(MeasurementEvent event)  {
        SensorEntity sensorEntity = SensorEntity.builder()
                .uuid(event.getUuid())
                .time(event.getRegisterMeasurementRequest().getTime())
                .co2Level(event.getRegisterMeasurementRequest().getCo2())
                .build();

        sensorRepository.save(sensorEntity);
    }

    public SensorStatusResponse findStatus(String uuid) {
        List<SensorEntity> co2LevelHistory = sensorRepository.findTop3ByUuidOrderByTimeDesc(uuid);
        SensorStatusResponse result = new SensorStatusResponse(SensorStatus.UNKNOWN);
        if (co2LevelHistory.size() < 3 && !co2LevelHistory.isEmpty() && co2LevelHistory.get(0).getCo2Level() >= 2000) {
            result.setStatus(SensorStatus.WARN);
        }
        int sensorAlertCount = co2LevelHistory.stream().filter(item -> item.getCo2Level() >= 2000).collect(Collectors.toList()).size();
        if (sensorAlertCount == 3) {
            result.setStatus(SensorStatus.ALERT);
        }
        int sensorOkCount = co2LevelHistory.stream().filter(item -> item.getCo2Level() < 2000).collect(Collectors.toList()).size();
        if (sensorOkCount == 3) {
            result.setStatus(SensorStatus.OK);
        }
        return result;
    }

    public MetricsResponse findMetrics(String uuid) {
        MetricsResponse result = new MetricsResponse();
        ZonedDateTime nowTime = ZonedDateTime.now();
        ZonedDateTime oneMonthAgo = nowTime.minusDays(30);

        result.setAvgLast30Days(sensorRepository.findAverageCo2Level(uuid, oneMonthAgo, nowTime ));
        result.setMaxLast30Days(sensorRepository.findMaxCo2Level(uuid, oneMonthAgo, nowTime));
        return result;
    }
}
