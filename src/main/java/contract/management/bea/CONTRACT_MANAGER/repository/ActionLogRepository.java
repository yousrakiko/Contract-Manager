package contract.management.bea.CONTRACT_MANAGER.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import contract.management.bea.CONTRACT_MANAGER.model.ActionLog;

@Repository
public interface ActionLogRepository extends JpaRepository<ActionLog, Integer> {


   @Query("SELECT a FROM ActionLog a WHERE a.timestamp > :cutoffDate")
List<ActionLog> findActionsFromLast30Days(@Param("cutoffDate") LocalDateTime cutoffDate);
}


