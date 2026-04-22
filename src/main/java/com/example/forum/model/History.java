package com.example.forum.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "history")
public class History {

    @Id
    private String id;

    private String userId;
    private String postId;
    private String title;

    private Date createTime;
}