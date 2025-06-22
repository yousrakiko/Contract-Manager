package contract.management.bea.CONTRACT_MANAGER.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import contract.management.bea.CONTRACT_MANAGER.model.ActionLog;
import contract.management.bea.CONTRACT_MANAGER.model.Comment;
import contract.management.bea.CONTRACT_MANAGER.model.Contracts;
import contract.management.bea.CONTRACT_MANAGER.model.Notification;
import contract.management.bea.CONTRACT_MANAGER.repository.ActionLogRepository;
import contract.management.bea.CONTRACT_MANAGER.repository.CommentRepository;
import contract.management.bea.CONTRACT_MANAGER.repository.ContractsRepository;
import contract.management.bea.CONTRACT_MANAGER.repository.NotificationRepository;

@Service
public class ContractStatusScheduler {

    @Autowired
    private ContractsRepository contractsRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ActionLogRepository actionLogRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    private boolean hasRecentNotification(String contractId, String title, int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        
        // For expiration notifications, check if there's a notification with the exact same title and contract ID
        if (title.contains("Expiring Soon") || title.equals("Contract Expired")) {
            List<Notification> recentNotifications = notificationRepository.findByLinkContainingAndTitleAndCreatedAtAfter(
                "/contracts/" + contractId + "/details",
                title,
                cutoffTime
            );
            return !recentNotifications.isEmpty();
        }
        
        // For other notifications, use the default check
        List<Notification> recentNotifications = notificationRepository.findByLinkContainingAndTitleAndCreatedAtAfter(
            "/contracts/" + contractId + "/details",
            title,
            cutoffTime
        );
        return !recentNotifications.isEmpty();
    }

    @Scheduled(cron = "0 */1 * * * *") 
    public void updateContractStatuses() {
        List<Contracts> allContracts = contractsRepository.findAll();

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        LocalDateTime now2 = LocalDateTime.now();

        for (Contracts contract : allContracts) {
            String status = contract.getStatus();
            Timestamp startAt = contract.getStartsdAt();
            LocalDate endAt = contract.getEndDate();
            String contractId = String.valueOf(contract.getContractId());

            // Check for contract activation
            if ("Signed".equalsIgnoreCase(status) && startAt != null && now.after(startAt)) {
                contract.setStatus("Active");
                contract.setUpdatedAt(now);
                contractsRepository.save(contract);

                // Create notification for contract activation
                String activationTitle = "Contract Activated";
                String activationMessage = "Contract #" + contractId + " has become active.";
                String activationLink = "/contracts/" + contractId + "/details";
                
                // Only send notification if there hasn't been one in the last 24 hours
                if (!hasRecentNotification(contractId, activationTitle, 24)) {
                    notificationService.sendNotificationToRole("ADMIN", activationTitle, activationMessage, activationLink);
                    notificationService.sendNotificationToRole("VALIDATOR2", activationTitle, activationMessage, activationLink);
                }

                ActionLog log = new ActionLog(
                    "contract",
                    contractId,
                    "STATUS_UPDATE",
                    "Signed",
                    "Active",
                    "Auto-update: Signed → Active",
                    "SYSTEM"
                );
                actionLogRepository.save(log);
            }

            // Check for contract expiration
            if ("Active".equalsIgnoreCase(status) && endAt != null) {
                long daysUntilExpiration = ChronoUnit.DAYS.between(now2.toLocalDate(), endAt);

                // Check for ~30 days warning (between 29 and 31 days)
                if (daysUntilExpiration >= 29 && daysUntilExpiration <= 31) {
                    String warningTitle = "Contract Expiring Soon (30 days)";
                    String warningMessage = "Contract #" + contractId + " will expire in approximately 30 days.";
                    String warningLink = "/contracts/" + contractId + "/details";

                    // Only send notification if there hasn't been one in the last 24 hours
                    if (!hasRecentNotification(contractId, warningTitle, 24)) {
                        notificationService.sendNotificationToRole("ADMIN", warningTitle, warningMessage, warningLink);
                        notificationService.sendNotificationToRole("VALIDATOR2", warningTitle, warningMessage, warningLink);
                    }
                }

                // Check for ~10 days warning (between 9 and 11 days)
                if (daysUntilExpiration >= 9 && daysUntilExpiration <= 11) {
                    String warningTitle = "Contract Expiring Soon (10 days)";
                    String warningMessage = "Contract #" + contractId + " will expire in approximately 10 days.";
                    String warningLink = "/contracts/" + contractId + "/details";

                    // Only send notification if there hasn't been one in the last 24 hours
                    if (!hasRecentNotification(contractId, warningTitle, 24)) {
                        notificationService.sendNotificationToRole("ADMIN", warningTitle, warningMessage, warningLink);
                        notificationService.sendNotificationToRole("VALIDATOR2", warningTitle, warningMessage, warningLink);
                    }
                }

                // Check for actual expiration
                if (now2.toLocalDate().isAfter(endAt)) {
                    contract.setStatus("Expired");
                    contract.setUpdatedAt(now);
                    contractsRepository.save(contract);

                    // Create notification for contract expiration
                    String expirationTitle = "Contract Expired";
                    String expirationMessage = "Contract #" + contractId + " has expired.";
                    String expirationLink = "/contracts/" + contractId + "/details";
                    
                    // Only send notification if there hasn't been one in the last 24 hours
                    if (!hasRecentNotification(contractId, expirationTitle, 24)) {
                        notificationService.sendNotificationToRole("ADMIN", expirationTitle, expirationMessage, expirationLink);
                        notificationService.sendNotificationToRole("VALIDATOR2", expirationTitle, expirationMessage, expirationLink);
                    }

                    ActionLog log = new ActionLog(
                        "contract",
                        contractId,
                        "STATUS_UPDATE",
                        "Active",
                        "Expired",
                        "Auto-update: Active → Expired",
                        "SYSTEM"
                    );
                    actionLogRepository.save(log);
                }
            }

            // Handle pending status changes
            if ("Pending".equalsIgnoreCase(status)) {
                List<Comment> comments = commentRepository.findByContract(contract);
                if (!comments.isEmpty()) {
                    contract.setStatus("Pending with Comment");
                    contractsRepository.save(contract);

                    ActionLog log = new ActionLog(
                        "contract",
                        contractId,
                        "STATUS_UPDATE",
                        "Pending",
                        "Pending with Comment",
                        "Comment added: → Pending with Comment",
                        "SYSTEM"
                    );
                    actionLogRepository.save(log);
                }
            }

            if ("Pending with Comment".equalsIgnoreCase(status)) {
                List<Comment> comments = commentRepository.findByContract(contract);
                if (comments.isEmpty()) {
                    contract.setStatus("Pending");
                    contractsRepository.save(contract);

                    ActionLog log = new ActionLog(
                        "contract",
                        contractId,
                        "STATUS_UPDATE",
                        "Pending with Comment",
                        "Pending",
                        "Comments removed: → Pending",
                        "SYSTEM"
                    );
                    actionLogRepository.save(log);
                }
            }
        }
    }
}