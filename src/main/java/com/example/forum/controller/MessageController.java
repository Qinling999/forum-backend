package com.example.forum.controller;

import com.example.forum.model.Message;
import com.example.forum.service.MessageService;
import com.example.forum.common.Result;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public Result send(@RequestBody Message msg,
                       HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");

        if (userId == null) {
            return Result.error("未登录");
        }

        msg.setFromUserId(userId);

        messageService.send(msg);

        return Result.success("发送成功");
    }

    @GetMapping("/list")
    public Result list(HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");

        if (userId == null) {
            return Result.error("未登录");
        }

        return Result.success(
                messageService.getConversations(userId)
        );
    }

    @GetMapping("/messages")
    public Result messages(
            @RequestParam String targetUserId,
            @RequestParam int page,
            @RequestParam int size,
            HttpServletRequest request
    ) {

        String userId = (String) request.getAttribute("userId");

        if (userId == null) {
            return Result.error("未登录");
        }

        return Result.success(
                messageService.getMessages(userId, targetUserId, page, size)
        );
    }

    @PostMapping("/read")
    public Result read(@RequestParam String targetUserId,
                       HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");

        if (userId == null) {
            return Result.error("未登录");
        }

        messageService.read(userId, targetUserId);

        return Result.success("已读");
    }
}