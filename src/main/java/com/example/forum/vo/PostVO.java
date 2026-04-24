package com.example.forum.vo;

import com.example.forum.model.Comment;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class PostVO {

    private String id;
    private String title;
    private String content;

    private String authorId;
    private String authorName; // ⭐ 动态拼接
    private String avatar;     // ⭐ 可以顺便加头像
    private String categoryId;
    private Date createTime;
    private Integer likes;

    private List<Comment> comments;

    private int views;

    // ⭐ 推荐分数
    private double score;

    // ⭐ 推荐解释（重点）
    private String reason;

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}