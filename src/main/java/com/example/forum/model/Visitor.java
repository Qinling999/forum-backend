package com.example.forum.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data   // ⭐关键！！自动生成 getter/setter
@Document(collection = "visitors")
public class Visitor {

    @Id
    private String id;

    private String userId;     // 被访问者
    private String visitorId; // 访问者

    private Date visitTime;
}