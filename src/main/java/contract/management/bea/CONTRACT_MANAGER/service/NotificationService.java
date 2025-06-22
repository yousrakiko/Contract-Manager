package contract.management.bea.CONTRACT_MANAGER.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import contract.management.bea.CONTRACT_MANAGER.dto.NotificationDTO;
import contract.management.bea.CONTRACT_MANAGER.model.Comment;
import contract.management.bea.CONTRACT_MANAGER.model.Contracts;
import contract.management.bea.CONTRACT_MANAGER.model.Notification;
import contract.management.bea.CONTRACT_MANAGER.model.Notification.NotificationType;
import contract.management.bea.CONTRACT_MANAGER.model.User;
import contract.management.bea.CONTRACT_MANAGER.repository.CommentRepository;
import contract.management.bea.CONTRACT_MANAGER.repository.ContractsRepository;
import contract.management.bea.CONTRACT_MANAGER.repository.NotificationRepository;
import contract.management.bea.CONTRACT_MANAGER.repository.UserRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final WebsocketNotificationService webSocketService;
    private final UserRepository userRepository;
    private final ContractsRepository contractsRepository;
    private final CommentRepository commentRepository;

    public NotificationService(NotificationRepository notificationRepository, WebsocketNotificationService webSocketService, UserRepository userRepository, ContractsRepository contractsRepository, CommentRepository commentRepository) {
        this.notificationRepository = notificationRepository;
        this.webSocketService = webSocketService;
        this.userRepository = userRepository;
        this.contractsRepository = contractsRepository;
        this.commentRepository = commentRepository;
    }

    public List<Notification> getNotificationsForUser(String recipientId) {
        // Check for recent actions before returning notifications
        checkAndCreateNotificationsForRecentActions(recipientId);
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
    }

    public List<Notification> getUnreadNotificationsForUser(String recipientId) {
        return notificationRepository.findByRecipientIdAndSeenFalseOrderByCreatedAtDesc(recipientId);
    }

    public Notification createNotification(String recipientId, String title, String message, String link, NotificationType type) {
        try {
            // Create and save notification
            Notification notification = new Notification();
            notification.setRecipientId(recipientId);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setLink(link);
            notification.setSeen(false);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setType(type != null ? type : NotificationType.INFO);

            Notification saved = notificationRepository.save(notification);

            // Send via WebSocket
            try {
                webSocketService.sendToUser(recipientId, new NotificationDTO(title, message, link, saved.getType(), null));
                System.out.println("Notification sent to user: " + recipientId + " - " + title);
            } catch (Exception e) {
                System.err.println("Failed to send WebSocket notification: " + e.getMessage());
                // Don't fail the entire operation if WebSocket fails
            }

            return saved;
        } catch (Exception e) {
            System.err.println("Failed to create notification: " + e.getMessage());
            throw e;
        }
    }

    public void markAsSeen(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setSeen(true);
            notificationRepository.save(n);
        });
    }

    public void sendNotificationToRole(String roleName, String title, String message, String link) {
        List<User> users = userRepository.findByRoleRoleName(roleName);
        for (User user : users) {
            createNotification(user.getMatricule(), title, message, link, NotificationType.INFO);
        }
    }

    public void checkAndCreateNotificationsForRecentActions(String userId) {
        // Get user's role
        User user = userRepository.findByMatricule(userId);
        if (user == null || user.getRole() == null) {
            return;
        }

        String roleName = user.getRole().getRoleName();
        
        // Check for recent contracts that need attention based on role
        if ("VALIDATOR2".equals(roleName)) {
            // Find contracts that are pending review
            List<Contracts> pendingContracts = contractsRepository.findByStatus("pending");
            for (Contracts contract : pendingContracts) {
                // Check if notification already exists
                boolean notificationExists = notificationRepository.existsByRecipientIdAndLink(
                    userId, 
                    "/contracts/" + contract.getContractId() + "/details"
                );
                
                if (!notificationExists) {
                    String title = "New Contract Pending Review";
                    String message = "Contract #" + contract.getContractId() + " has been confirmed and is pending your review.";
                    String link = "/contracts/" + contract.getContractId() + "/details";
                    
                    createNotification(userId, title, message, link, NotificationType.INFO);
                }
            }
        }

        // Check for existing comments that need attention
        if ("VALIDATOR1".equals(roleName)) {
            // Get comments from the last 7 days
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
            List<Comment> recentComments = commentRepository.findRecentCommentsForUser(cutoffDate);
            
            for (Comment comment : recentComments) {
                // Skip comments from the current user
                if (comment.getAuthor().getMatricule().equals(userId)) {
                    continue;
                }

                // Check if notification already exists for this specific comment
                boolean notificationExists = notificationRepository.existsByRecipientIdAndLinkAndTitle(
                    userId,
                    "/contractsManager",
                    "New Comment on Contract"
                );

                if (!notificationExists) {
                    String title = "New Comment on Contract";
                    String message = "Contract #" + comment.getContract().getContractId() + 
                                   " has a new comment from " + comment.getAuthor().getFirstName() + 
                                   " " + comment.getAuthor().getLastName() + 
                                   ". Please review the contract.";
                    String link = "/contractsManager";

                    createNotification(userId, title, message, link, NotificationType.INFO);
                }
            }
        }
    }

    public void markNotificationAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setSeen(true);
            notificationRepository.save(notification);
        });
    }
}

