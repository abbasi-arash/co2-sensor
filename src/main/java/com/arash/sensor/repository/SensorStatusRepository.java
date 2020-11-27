package com.arash.sensor.repository;

import com.arash.sensor.domain.SensorStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorStatusRepository extends JpaRepository<SensorStatusEntity, String> {

}
