package com.example.forum.model;

import java.util.Date;

public class UserBehavior {

    private String userId;
    private String postId;
    private String actionType; // view / like / collect / dislike
    private Integer duration;
    private Date createTime;
}
