package com.example.forum.controller;

import com.example.forum.model.Comment;
import com.example.forum.model.Notification;
import com.example.forum.repository.CommentRepository;
import com.example.forum.repository.NotificationRepository;
import com.example.forum.common.Result;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/notification")
@CrossOrigin
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private CommentRepository commentRepository;

    // ========================
    // 1. 获取通知列表
    // ========================
    @GetMapping("/list")
    public Result<List<Notification>> getList(@RequestParam String userId) {

        // ⭐ 1. 先查通知列表
        List<Notification> list =
                notificationRepository.findByUserIdOrderByCreateTimeDesc(userId);

        // ⭐ 2. 遍历处理（评论删除逻辑）
        for (Notification n : list) {

            if ("comment".equals(n.getType())) {

                // commentId 可能为空，先判空（很重要）
                if (n.getCommentId() == null) {
                    n.setContent("该评论已删除");
                    continue;
                }

                Optional<Comment> commentOpt =
                        commentRepository.findById(n.getCommentId());

                if (commentOpt.isPresent()) {
                    Comment c = commentOpt.get();

                    if (Boolean.TRUE.equals(c.getIsDeleted())) {
                        n.setContent("该评论已删除");
                    }

                } else {
                    // 评论不存在（被物理删除）
                    n.setContent("该评论已删除");
                }
            }
        }

        // ⭐ 3. 返回处理后的 list
        return Result.success(list);
    }

    // ========================
    // 2. 标记已读
    // ========================
    @PostMapping("/read/{id}")
    public Result<String> read(@PathVariable String id) {
        Optional<Notification> optional = notificationRepository.findById(id);
        if (optional.isEmpty()) return Result.error("通知不存在");

        Notification n = optional.get();
        n.setIsRead(true);
        notificationRepository.save(n);

        return Result.success("ok");
    }

    // ========================
    // 3. 全部已读
    // ========================
    @PostMapping("/readAll")
    public Result<?> readAll(@RequestBody Map<String, String> body) {

        String userId = body.get("userId");

        List<Notification> list =
                notificationRepository.findByUserIdAndIsRead(userId, false);

        list.forEach(n -> n.setIsRead(true));

        notificationRepository.saveAll(list);

        return Result.success();
    }

    // ========================
    // 4. 未读数量
    // ========================
    @GetMapping("/unreadCount")
    public Result<Long> count(@RequestParam String userId) {
        return Result.success(
                notificationRepository.countByUserIdAndIsReadFalse(userId)
        );
    }

    // ========================
    //5. 删除通知（带权限校验）
    // ========================
    @DeleteMapping("/{id}")
    public Result<String> deleteNotification(@PathVariable String id,
                                             HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");

        if (userId == null) {
            return Result.error("未登录");
        }

        Optional<Notification> optional = notificationRepository.findById(id);

        if (optional.isEmpty()) {
            return Result.error("通知不存在");
        }

        Notification notice = optional.get();

        // ⭐ 只能删除自己的通知
        if (!userId.equals(notice.getUserId())) {
            return Result.error("无权限删除");
        }

        notificationRepository.deleteById(id);

        return Result.success("删除成功");
    }
}