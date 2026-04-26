package com.example.forum.vo;

import lombok.Data;

@Data
public class CategoryStatVO {
    private String categoryId;
    private String categoryName;
    private long count;
    private double percentage;
}