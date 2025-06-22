package contract.management.bea.CONTRACT_MANAGER.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import contract.management.bea.CONTRACT_MANAGER.dto.NotificationDTO;
import contract.management.bea.CONTRACT_MANAGER.model.Notification.NotificationType;
import java.time.LocalDateTime;

@Service
public class WebsocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebsocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

 public void sendToUser(String userId, NotificationDTO notification) {
        try {
            String destination = "/user/" + userId + "/queue/notifications";
            System.out.println("Sending WebSocket notification to: " + destination);
            System.out.println("Notification content: " + notification.getTitle());
            
            messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);
            
            System.out.println("WebSocket notification sent successfully");
        } catch (Exception e) {
            System.err.println("Failed to send WebSocket notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendToUser(String userId, String title, String message, String link, NotificationType type) {
        NotificationDTO notification = new NotificationDTO(title, message, link, type, LocalDateTime.now());
        sendToUser(userId, notification);
    }

    // Broadcast to all connected users (optional)
    public void broadcastToAll(NotificationDTO notification) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications", notification);
            System.out.println("Broadcast notification sent to all users");
        } catch (Exception e) {
            System.err.println("Failed to broadcast notification: " + e.getMessage());
        }
    }
}