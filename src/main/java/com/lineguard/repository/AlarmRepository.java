package com.lineguard.repository;

import com.lineguard.model.Alarm;
import com.lineguard.model.AlarmStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findByProductionLineId(Long productionLineId);
    List<Alarm> findByProductionLineIdAndStatus(Long productionLineId, AlarmStatus status);
    List<Alarm> findByStatus(AlarmStatus status);
}
