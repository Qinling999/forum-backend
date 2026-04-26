package com.example.forum.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Document(collection = "users")
@Data
public class User {

    @Id
    private String id;
    private String username;
    private String password;
    private String email;
    private String nickname;
    private String avatar;
    private String role;

    // 我关注的人
    private List<String> following;
    // 关注我的人
    private List<String> followers;
    private List<String> favoritePosts = new ArrayList<>();

    private Date createTime;

    private List<String> dislikedPostIds; // 不喜欢
    private List<String> blockedUserIds;  // 拉黑作者

}
