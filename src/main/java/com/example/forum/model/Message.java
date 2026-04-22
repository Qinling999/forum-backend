package com.example.forum.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
public class Message {

    @Id
    private String id;

    private String fromUserId;
    private String toUserId;

    private String content;
    private String type = "text";

    private Boolean read = false;

    private Date createTime = new Date();
}