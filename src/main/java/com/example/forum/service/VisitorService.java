package com.example.forum.service;

import com.example.forum.model.Visitor;
import com.example.forum.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VisitorService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserRepository userRepository;

    // =====================
    // 记录访问
    // =====================
    public void recordVisit(String userId, String visitorId) {

        Query query = new Query();
        query.addCriteria(
                Criteria.where("userId").is(userId)
                        .and("visitorId").is(visitorId)
        );

        Visitor exist = mongoTemplate.findOne(query, Visitor.class);

        if (exist != null) {
            exist.setVisitTime(new Date());
            mongoTemplate.save(exist);
        } else {
            Visitor v = new Visitor();
            v.setUserId(userId);
            v.setVisitorId(visitorId);
            v.setVisitTime(new Date());
            mongoTemplate.save(v);
        }
    }

    // =====================
    // 获取访客
    // =====================
    public List<Map<String, Object>> getVisitors(String userId) {

        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.with(Sort.by(Sort.Direction.DESC, "visitTime"));

        List<Visitor> list = mongoTemplate.find(query, Visitor.class);

        List<Map<String, Object>> result = new ArrayList<>();

        for (Visitor v : list) {

            var userOpt = userRepository.findById(v.getVisitorId());

            if (!userOpt.isPresent()) continue;

            Map<String, Object> map = new HashMap<>();
            map.put("userId", v.getVisitorId());
            map.put("username", userOpt.get().getUsername());
            map.put("avatar", userOpt.get().getAvatar());
            map.put("time", v.getVisitTime());

            result.add(map);
        }

        return result;
    }

    // =====================
    // 数量
    // =====================
    public long count(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        return mongoTemplate.count(query, Visitor.class);
    }
}