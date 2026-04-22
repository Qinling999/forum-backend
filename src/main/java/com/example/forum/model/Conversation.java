package com.example.forum.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
public class Conversation {

    @Id
    private String id;

    private String userA;
    private String userB;

    private String lastMessage;
    private Date lastTime;

    private int unreadCountA = 0;
    private int unreadCountB = 0;
}