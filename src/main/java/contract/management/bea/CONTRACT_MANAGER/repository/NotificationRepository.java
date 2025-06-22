package contract.management.bea.CONTRACT_MANAGER.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import contract.management.bea.CONTRACT_MANAGER.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId);

    List<Notification> findByRecipientIdAndSeenFalseOrderByCreatedAtDesc(String recipientId);

    boolean existsByRecipientIdAndLink(String recipientId, String link);

    boolean existsByRecipientIdAndLinkAndTitle(String recipientId, String link, String title);

    List<Notification> findByLinkContainingAndTitleAndCreatedAtAfter(String link, String title, LocalDateTime createdAt);
}

