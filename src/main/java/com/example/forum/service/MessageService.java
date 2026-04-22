package com.example.forum.service;

import com.example.forum.model.Conversation;
import com.example.forum.model.Message;
import com.example.forum.repository.ConversationRepository;
import com.example.forum.repository.MessageRepository;
import com.example.forum.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserRepository userRepository;

    // =====================
    // 用户排序（保证唯一）
    // =====================
    private String[] sortUsers(String u1, String u2) {
        return (u1.compareTo(u2) < 0)
                ? new String[]{u1, u2}
                : new String[]{u2, u1};
    }

    private String buildKey(String u1, String u2) {
        String[] users = sortUsers(u1, u2);
        return users[0] + "_" + users[1];
    }

    // =====================
    // 发送消息
    // =====================
    public void send(Message msg) {

        if (msg.getToUserId() == null) {
            throw new RuntimeException("目标用户不能为空");
        }

        msg.setCreateTime(new Date());
        msg.setRead(false);

        messageRepository.save(msg);

        String[] users = sortUsers(msg.getFromUserId(), msg.getToUserId());
        String key = users[0] + "_" + users[1];

        Conversation convo = conversationRepository.findById(key)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setId(key);
                    c.setUserA(users[0]);
                    c.setUserB(users[1]);
                    c.setUnreadCountA(0);
                    c.setUnreadCountB(0);
                    return c;
                });

        convo.setLastMessage(msg.getContent());
        convo.setLastTime(new Date());

        // ⭐ 未读数增加（只在发送时）
        if (msg.getToUserId().equals(convo.getUserA())) {
            convo.setUnreadCountA(
                    Optional.ofNullable(convo.getUnreadCountA()).orElse(0) + 1
            );
        } else {
            convo.setUnreadCountB(
                    Optional.ofNullable(convo.getUnreadCountB()).orElse(0) + 1
            );
        }

        conversationRepository.save(convo);
    }

    // =====================
    // 会话列表
    // =====================
    public List<Map<String, Object>> getConversations(String userId) {

        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(
                Criteria.where("userA").is(userId),
                Criteria.where("userB").is(userId)
        ));

        List<Conversation> list =
                mongoTemplate.find(query, Conversation.class);

        Map<String, Conversation> uniqueMap = new HashMap<>();

        for (Conversation c : list) {

            // ❗ 1. 过滤自己和自己
            if (c.getUserA().equals(c.getUserB())) continue;

            // ❗ 2. 生成唯一 key（防重复）
            String key = buildKey(c.getUserA(), c.getUserB());

            // ❗ 3. 保留最新的一条（按时间）
            if (!uniqueMap.containsKey(key)
                    || c.getLastTime().after(uniqueMap.get(key).getLastTime())) {

                uniqueMap.put(key, c);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Conversation c : uniqueMap.values()) {

            String targetId;

            if (userId.equals(c.getUserA())) {
                targetId = c.getUserB();
            } else if (userId.equals(c.getUserB())) {
                targetId = c.getUserA();
            } else {
                continue; // 理论不会发生，防御
            }

            // ❗ 防止 targetId 还是自己
            if (userId.equals(targetId)) continue;

            int unread = userId.equals(c.getUserA())
                    ? Optional.ofNullable(c.getUnreadCountA()).orElse(0)
                    : Optional.ofNullable(c.getUnreadCountB()).orElse(0);

            var userOpt = userRepository.findById(targetId);

            String username = "未知用户";
            String avatar = "";

            if (userOpt.isPresent()) {
                username = userOpt.get().getUsername();
                avatar = userOpt.get().getAvatar();
            }

            Map<String, Object> map = new HashMap<>();
            map.put("targetUserId", targetId);
            map.put("targetName", username);
            map.put("avatar", avatar);
            map.put("lastMessage", c.getLastMessage());
            map.put("lastTime", c.getLastTime());
            map.put("unread", unread);

            result.add(map);
        }

        // ⭐ 按最后时间排序
        result.sort((a, b) ->
                ((Date) b.get("lastTime"))
                        .compareTo((Date) a.get("lastTime"))
        );

        return result;
    }

    // =====================
    // 聊天记录
    // =====================
    public Page<Message> getMessages(String me, String target, int page, int size) {

        Query query = new Query();

        query.addCriteria(new Criteria().orOperator(
                Criteria.where("fromUserId").is(me).and("toUserId").is(target),
                Criteria.where("fromUserId").is(target).and("toUserId").is(me)
        ));

        query.with(Sort.by(Sort.Direction.DESC, "createTime"));
        query.skip((long) page * size).limit(size);

        List<Message> list = mongoTemplate.find(query, Message.class);

        long count = mongoTemplate.count(
                Query.of(query).limit(-1).skip(-1),
                Message.class
        );

        return new PageImpl<>(list, PageRequest.of(page, size), count);
    }

    // =====================
    // 标记已读（100%稳定版）
    // =====================
    public void read(String me, String target) {

        // 1️⃣ 所有消息设为已读
        Query query = new Query();
        query.addCriteria(
                Criteria.where("fromUserId").is(target)
                        .and("toUserId").is(me)
                        .and("read").is(false)
        );

        mongoTemplate.updateMulti(
                query,
                Update.update("read", true),
                Message.class
        );

        // 2️⃣ 清零未读数（绝对正确）
        String key = buildKey(me, target);

        conversationRepository.findById(key)
                .ifPresent(c -> {

                    if (me.equals(c.getUserA())) {
                        c.setUnreadCountA(0);
                    } else {
                        c.setUnreadCountB(0);
                    }

                    conversationRepository.save(c);
                });
    }
}