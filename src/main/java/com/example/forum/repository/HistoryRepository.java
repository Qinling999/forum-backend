package com.example.forum.repository;

import com.example.forum.model.History;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface HistoryRepository extends MongoRepository<History, String> {

    // 查用户浏览记录（按时间倒序）
    List<History> findByUserIdOrderByCreateTimeDesc(String userId);

    // 查某条是否存在（用于去重）
    History findByUserIdAndPostId(String userId, String postId);

    // 删除旧记录
    void deleteByUserIdAndPostId(String userId, String postId);

    List<History> findByUserId(String userId);

    // 活跃用户数（去重）
    @Query(value = "{}", fields = "{userId:1}")
    List<History> findAllUserIds();

    // 平均停留时间
    @Aggregation(pipeline = {
            "{ $match: { duration: { $ne: null } } }",
            "{ $group: { _id: null, avg: { $avg: '$duration' } } }"
    })
    List<Map<String, Object>> avgDuration();

    List<History> findByCreateTimeBetween(Date start, Date end);
}