package com.example.forum.repository;

import com.example.forum.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;

public interface PostRepository extends MongoRepository<Post, String> {

    // 获取所有帖子（按时间倒序）
    List<Post> findAllByOrderByCreateTimeDesc();

    // 按分类查询
    List<Post> findByCategoryId(String categoryId);

    // 按分类 + 时间排序（更实用）
    List<Post> findByCategoryIdOrderByCreateTimeDesc(String categoryId);

    Page<Post> findAll(Pageable pageable);

    long countByAuthorId(String authorId);

    // 模糊搜索 title 或 content（支持中文）
    @Query("{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'content': { $regex: ?0, $options: 'i' } } ] }")
    List<Post> searchPosts(String keyword);

    List<Post> findByAuthorIdOrderByCreateTimeDesc(String authorId);

    @Query("{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'content': { $regex: ?0, $options: 'i' } } ] }")
    Page<Post> searchPosts(String keyword, Pageable pageable);

    List<Post> findByAuthorId(String authorId);

    @Query("{ 'likedUsers': ?0 }")
    List<Post> findLikedPosts(String userId);

    @Query("{ 'favoritedUsers': ?0 }")
    List<Post> findFavoritedPosts(String userId);
}