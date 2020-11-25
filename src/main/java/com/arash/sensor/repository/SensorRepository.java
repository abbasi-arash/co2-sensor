package com.arash.sensor.repository;

import com.arash.sensor.domain.SensorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface SensorRepository extends JpaRepository<SensorEntity, String> {
    List<SensorEntity> findTop3ByUuidOrderByTimeDesc(String uuid);

    @Query("SELECT MAX (e.co2Level) FROM SensorEntity e WHERE e.uuid = :uuid and e.time between :startDate and :endDate")
    Integer findMaxCo2Level(@Param("uuid") String uuid,@Param("startDate") ZonedDateTime startDate,@Param("endDate") ZonedDateTime endDate);

    @Query("SELECT AVG(e.co2Level) FROM SensorEntity e WHERE e.uuid = :uuid and e.time between :startDate and :endDate")
    Integer findAverageCo2Level(@Param("uuid") String uuid,@Param("startDate") ZonedDateTime startDate,@Param("endDate") ZonedDateTime endDate);
}
