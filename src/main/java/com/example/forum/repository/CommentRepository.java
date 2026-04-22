package com.example.forum.repository;

import com.example.forum.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {

    // 根据帖子查评论（按时间倒序）
    List<Comment> findByPostIdOrderByCreateTimeDesc(String postId);

    // 分页版本（推荐）
    Page<Comment> findByPostId(String postId, Pageable pageable);

    List<Comment> findByPostIdAndParentIdIsNullOrderByCreateTimeDesc(String postId);

    List<Comment> findByParentIdOrderByCreateTimeAsc(String parentId);

    List<Comment> findByUserIdOrderByCreateTimeDesc(String userId);

    List<Comment> findByParentId(String parentId);

    long countByUserId(String userId);

}