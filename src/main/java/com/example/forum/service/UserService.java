package com.example.forum.service;

import com.example.forum.model.User;
import com.example.forum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public long countFollowing(String userId) {

        User user = userRepository.findFollowingById(userId);

        if (user == null || user.getFollowing() == null) {
            return 0;
        }

        return user.getFollowing().size();
    }

    public long countFans(String userId) {
        return userRepository.findFans(userId).size();
    }

    // ⭐ 获取关注列表
    // ======================
    public List<User> getFollowing(String userId) {

        return userRepository.findById(userId)
                .map(user -> user.getFollowing())
                .orElse(new ArrayList<>())
                .stream()
                .map(id -> userRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    // ======================
    // ⭐ 获取粉丝列表
    // ======================
    public List<User> getFans(String userId) {
        return userRepository.findFans(userId);
    }
}