package com.example.forum.model;

import lombok.Data;

@Data
public class DailyStats {

    private String date; // 2026-04-26

    private int newUsers;
    private int newPosts;
    private int activeUsers;

    private int views;
    private int likes;
    private int favorites;

    private int dislikes;
    private int blocks;

    private double avgDuration;
}
