package com.library.repository;
import com.library.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface NotificationRepository extends JpaRepository<Notification,Long> {
 List<Notification> findTop100ByUserIdOrderByCreatedAtDesc(Long userId);
 long countByUserIdAndReadFalse(Long userId);
 Optional<Notification> findByIdAndUserId(Long id,Long userId);
 boolean existsByUserIdAndTypeAndTitleAndMessage(Long userId,Notification.Type type,String title,String message);
 void deleteByUserId(Long userId);
}
