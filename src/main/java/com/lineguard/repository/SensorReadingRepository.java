package com.lineguard.repository;

import com.lineguard.model.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
    List<SensorReading> findByProductionLineIdOrderByRecordedAtDesc(Long productionLineId);
}
