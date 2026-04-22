package com.example.forum.service;

import com.example.forum.model.Comment;
import com.example.forum.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    // 新增评论
    public Comment addComment(Comment comment) {
        return commentRepository.save(comment);
    }

    // 获取帖子评论
    public List<Comment> getCommentsByPostId(String postId) {
        return commentRepository.findByPostIdOrderByCreateTimeDesc(postId);
    }

    // 删除评论（可选）
    public void delete(String id) {
        commentRepository.deleteById(id);
    }
}