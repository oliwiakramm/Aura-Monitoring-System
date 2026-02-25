package com.Oliwia.aura.repository;

import com.Oliwia.aura.model.MetricRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MetricRepository extends JpaRepository<MetricRecord,Long> {

    Optional<MetricRecord> findTopByAgentIdOrderByTimestampDesc(String agentId);

    @Query("SELECT DISTINCT m.agentId FROM MetricRecord m")
    List<String> findAllDistinctAgentIds();

    List<MetricRecord> findByAgentIdAndTimestampAfter(String agentId, Instant since);
}