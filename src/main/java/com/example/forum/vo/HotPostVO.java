package com.example.forum.vo;

import lombok.Data;

@Data
public class HotPostVO {
    private String id;
    private String title;
    private String categoryId;
    private int likes;
    private int views;
    private long commentCount;
    private double score;
}
