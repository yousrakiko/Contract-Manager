package contract.management.bea.CONTRACT_MANAGER.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import contract.management.bea.CONTRACT_MANAGER.dto.NotificationDTO;
import contract.management.bea.CONTRACT_MANAGER.service.NotificationService;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public List<NotificationDTO> getNotificationsForUser(@PathVariable String userId) {
        return notificationService.getNotificationsForUser(userId).stream()
            .map(n -> new NotificationDTO(n.getId(), n.getTitle(), n.getMessage(), n.getLink(), n.getType(), n.getCreatedAt()))
            .toList();
    }

    @GetMapping("/{userId}/unread")
    @PreAuthorize("isAuthenticated()")
    public List<NotificationDTO> getUnread(@PathVariable String userId) {
        return notificationService.getUnreadNotificationsForUser(userId).stream()
            .map(n -> new NotificationDTO(n.getId(), n.getTitle(), n.getMessage(), n.getLink(), n.getType(), n.getCreatedAt()))
            .toList();
    }

    @PostMapping("/{id}/mark-as-seen")
    @PreAuthorize("isAuthenticated()")
    public void markAsSeen(@PathVariable Long id) {
        notificationService.markAsSeen(id);
    }

    @PostMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Long notificationId) {
        notificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok().build();
    }
}
