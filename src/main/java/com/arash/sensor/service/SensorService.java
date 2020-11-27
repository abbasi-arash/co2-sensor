package com.arash.sensor.service;

import com.arash.sensor.domain.SensorEntity;
import com.arash.sensor.domain.SensorStatusEntity;
import com.arash.sensor.dto.enums.SensorStatus;
import com.arash.sensor.dto.request.RegisterMeasurementRequest;
import com.arash.sensor.dto.response.MetricsResponse;
import com.arash.sensor.dto.response.SensorStatusResponse;
import com.arash.sensor.repository.SensorRepository;
import com.arash.sensor.repository.SensorStatusRepository;
import com.arash.sensor.service.event.MeasurementEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SensorService {

    private ApplicationEventPublisher publisher;
    private SensorRepository sensorRepository;
    private SensorStatusRepository sensorStatusRepository;

    @Value("${app.co2.accept.level}")
    private Integer acceptableCo2Level;

    @Autowired
    public SensorService(ApplicationEventPublisher applicationEventPublisher, SensorRepository sensorRepository, SensorStatusRepository sensorStatusRepository) {
        this.publisher = applicationEventPublisher;
        this.sensorRepository = sensorRepository;
        this.sensorStatusRepository = sensorStatusRepository;
    }

    public void registerMeasurement(RegisterMeasurementRequest registerMeasurementRequest, String uuid) {
        publisher.publishEvent(new MeasurementEvent(registerMeasurementRequest, uuid));
    }

    @Async
    @EventListener
    @Transactional
    public void asyncRegisterMeasurement(MeasurementEvent event) {
        SensorEntity sensorEntity = SensorEntity.builder()
                .uuid(event.getUuid())
                .time(event.getRegisterMeasurementRequest().getTime())
                .co2Level(event.getRegisterMeasurementRequest().getCo2())
                .build();

        updateStatus(sensorEntity);
        sensorRepository.save(sensorEntity);
    }

    @Transactional
    public void updateStatus(SensorEntity sensorEntity) {
        Optional<SensorStatusEntity> lastStatus = sensorStatusRepository.findById(sensorEntity.getUuid());
        SensorStatus status = SensorStatus.UNKNOWN;
        if (lastStatus.isPresent() && sensorEntity.getCo2Level() <= 2000) {
            status = co2LevelNormalStatus(lastStatus.get().getStatus(), sensorEntity.getUuid());
        }

        if (lastStatus.isPresent() && sensorEntity.getCo2Level() > 2000) {
            status = co2LevelAlertStatusl(lastStatus.get().getStatus(), sensorEntity.getUuid());
        }
        if (!lastStatus.isPresent()) {
            status = initCo2LevelStatus(sensorEntity.getCo2Level());
        }
        if (!status.equals(SensorStatus.UNKNOWN)) {
            sensorStatusRepository.save(new SensorStatusEntity(sensorEntity.getUuid(), status));
        }
    }

    private SensorStatus co2LevelNormalStatus(SensorStatus lastStatus, String uuid) {
        if (SensorStatus.OK.equals(lastStatus)) {
            return SensorStatus.OK;
        } else if (SensorStatus.WARN.equals(lastStatus)) {
            return SensorStatus.OK;
        } else if (SensorStatus.ALERT.equals(lastStatus)) {
            List<SensorEntity> co2LevelHistory = sensorRepository.findTop2ByUuidOrderByTimeDesc(uuid);
            if (!(co2LevelHistory.stream().anyMatch(item -> item.getCo2Level() > acceptableCo2Level))) {
                return SensorStatus.OK;
            }
        }
        return SensorStatus.UNKNOWN;
    }

    private SensorStatus co2LevelAlertStatusl(SensorStatus lastStatus, String uuid) {
        if (SensorStatus.OK.equals(lastStatus)) {
            return SensorStatus.WARN;
        } else if (SensorStatus.WARN.equals(lastStatus)) {
            List<SensorEntity> co2LevelHistory = sensorRepository.findTop2ByUuidOrderByTimeDesc(uuid);
            if (co2LevelHistory.size() == 2 &&
                    !(co2LevelHistory.stream().anyMatch(item -> item.getCo2Level() < acceptableCo2Level))) {
                return SensorStatus.ALERT;
            }
        } else if (SensorStatus.ALERT.equals(lastStatus)) {
            return SensorStatus.ALERT;
        }
        return SensorStatus.UNKNOWN;
    }

    private SensorStatus initCo2LevelStatus(Integer co2Level) {
        return (co2Level <= 2000) ? SensorStatus.OK : SensorStatus.WARN;
    }

    public SensorStatusResponse findStatus(String uuid) {
        Optional<SensorStatusEntity> sensorStatusEntity = sensorStatusRepository.findById(uuid);
        SensorStatusResponse result = new SensorStatusResponse(SensorStatus.UNKNOWN);
        if (sensorStatusEntity.isPresent()) {
            result.setStatus(sensorStatusEntity.get().getStatus());
        }
        return result;
    }

    public MetricsResponse findMetrics(String uuid) {
        MetricsResponse result = new MetricsResponse();
        ZonedDateTime nowTime = ZonedDateTime.now();
        ZonedDateTime oneMonthAgo = nowTime.minusDays(30);

        result.setAvgLast30Days(sensorRepository.findAverageCo2Level(uuid, oneMonthAgo, nowTime));
        result.setMaxLast30Days(sensorRepository.findMaxCo2Level(uuid, oneMonthAgo, nowTime));
        return result;
    }
}
