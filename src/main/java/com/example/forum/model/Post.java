package com.example.forum.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "posts")
@Data
public class Post {

    @Id
    private String id;

    @TextIndexed
    private String title;
    @TextIndexed
    private String content;

    private List<String> images;

    private String authorId;
    private String authorName; // 建议加（优化）
    private String authorAvatar;

    private String categoryId;

    private Date createTime;

    private int views;
    private List<String> likedUsers;
    private int likes;

    private Integer commentCount = 0;

    public Integer getCommentCount() {
        return commentCount == null ? 0 : commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }
}