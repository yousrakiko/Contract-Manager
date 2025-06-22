package contract.management.bea.CONTRACT_MANAGER.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import contract.management.bea.CONTRACT_MANAGER.dto.SimpleActionDto;
import contract.management.bea.CONTRACT_MANAGER.model.ActionLog;
import contract.management.bea.CONTRACT_MANAGER.repository.ActionLogRepository;

@Service
public class ActionLogService {

    @Autowired
    private ActionLogRepository actionLogRepository;

    public List<SimpleActionDto> getLatestSimpleActions() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<ActionLog> logs = actionLogRepository.findActionsFromLast30Days(thirtyDaysAgo);

        return logs.stream().map(log -> {
            String summary;
            if (log.getActionType().contains("STATUS_UPDATE") && log.getOldValue() != null && log.getNewValue() != null) {
                summary = "Updated to " + log.getNewValue();
            } else {
                summary = log.getDescription() != null ? log.getDescription() : log.getActionType();
            }

            return new SimpleActionDto(
                log.getModule(),
                Integer.parseInt(log.getReferenceId()),
                log.getUserMatricule(),
                summary,
                log.getTimestamp().toString()
            );
        }).toList();
    }
    public void logUserAction(String module, String referenceId, String actionType, String oldValue, String newValue, String description, String userMatricule) {
    ActionLog log = new ActionLog(
        module,
        referenceId,
        actionType,
        oldValue,
        newValue,
        description,
        userMatricule
    );
    actionLogRepository.save(log);
}
}

