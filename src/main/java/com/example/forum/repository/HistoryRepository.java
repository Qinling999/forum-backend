package com.example.forum.repository;

import com.example.forum.model.History;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HistoryRepository extends MongoRepository<History, String> {

    // 查用户浏览记录（按时间倒序）
    List<History> findByUserIdOrderByCreateTimeDesc(String userId);

    // 查某条是否存在（用于去重）
    History findByUserIdAndPostId(String userId, String postId);

    // 删除旧记录
    void deleteByUserIdAndPostId(String userId, String postId);
}