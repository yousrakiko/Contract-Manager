package contract.management.bea.CONTRACT_MANAGER.dto;

import java.time.LocalDateTime;
import contract.management.bea.CONTRACT_MANAGER.model.Notification.NotificationType;

public class NotificationDTO {
    private Long id;
    private String title;
    private String message;
    private String link;
    private NotificationType type;
    private LocalDateTime createdAt;

    // Default constructor for JSON deserialization
    public NotificationDTO() {
    }

    public NotificationDTO(String title, String message, String link, NotificationType type, LocalDateTime createdAt) {
        this.title = title;
        this.message = message;
        this.link = link;
        this.type = type;
        this.createdAt = createdAt;
    }

    public NotificationDTO(Long id, String title, String message, String link, NotificationType type, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.link = link;
        this.type = type;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}