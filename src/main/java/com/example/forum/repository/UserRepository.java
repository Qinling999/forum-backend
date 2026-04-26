package com.example.forum.repository;

import com.example.forum.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {

    // 登录用
    User findByUsername(String username);

    // 我关注了多少人
    @Query(value = "{ '_id': ?0 }", fields = "{ 'following': 1 }")
    User findFollowingById(String userId);

    // 有多少人关注我
    @Query(value = "{ 'following': ?0 }")
    List<User> findFans(String userId);

    long countByCreateTimeAfter(Date date);

    // 今日新增用户
    long countByCreateTimeBetween(Date start, Date end);
}