package com.example.forum.repository;

import com.example.forum.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByUserIdOrderByCreateTimeDesc(String userId);

    List<Notification> findByUserIdAndIsRead(String userId, Boolean isRead);
    long countByUserIdAndIsReadFalse(String userId);

    boolean existsByUserIdAndFromUserIdAndPostIdAndType(
            String userId,
            String fromUserId,
            String postId,
            String type
    );

}