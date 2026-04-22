package com.example.forum.controller;

import com.example.forum.model.Comment;
import com.example.forum.model.Notification;
import com.example.forum.model.Post;
import com.example.forum.model.User;
import com.example.forum.repository.CommentRepository;
import com.example.forum.repository.NotificationRepository;
import com.example.forum.repository.PostRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.service.CommentService;
import com.example.forum.common.Result;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/comment")
@CrossOrigin
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    // ========================
    // 1. 添加评论
    // ========================
    @PostMapping("/{postId}")
    public Result<String> addComment(@PathVariable String postId,
                                     @RequestBody Comment comment,
                                     HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");

        if (userId == null) return Result.error("未登录");

        if (comment == null || comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            return Result.error("评论不能为空");
        }

        Optional<Post> optional = postRepository.findById(postId);
        if (optional.isEmpty()) return Result.error("帖子不存在");

        Post post = optional.get();

        // 构建评论
        comment.setId(UUID.randomUUID().toString());
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setCreateTime(new Date());
        comment.setIsDeleted(false);

        Optional<User> userOpt = userRepository.findById(userId);
        comment.setUserName(userOpt.map(User::getUsername).orElse("匿名用户"));

        if (comment.getParentId() == null || comment.getParentId().isEmpty()) {
            comment.setParentId(null);
        }

        commentRepository.save(comment);
        // ========================
// ⭐ 评论通知
// ========================
        if (!userId.equals(post.getAuthorId())) {

            Notification n = new Notification();
            n.setUserId(post.getAuthorId()); // 被通知的人（帖子作者）
            n.setFromUserId(userId);
            n.setFromUserName(comment.getUserName());
            n.setType("comment");
            n.setPostId(postId);
            n.setCommentId(comment.getId());
            n.setContent(comment.getUserName() + "：" + comment.getContent());
            n.setIsRead(false);
            n.setCreateTime(new Date());

            notificationRepository.save(n);
        }
        // ========================
// ⭐ 回复评论通知
// ========================
        if (comment.getParentId() != null) {

            Optional<Comment> parentOpt = commentRepository.findById(comment.getParentId());

            if (parentOpt.isPresent()) {

                Comment parent = parentOpt.get();

                // 不要给自己发通知
                if (!userId.equals(parent.getUserId())) {

                    Notification n = new Notification();
                    n.setUserId(parent.getUserId()); // 被回复的人
                    n.setFromUserId(userId);
                    n.setFromUserName(comment.getUserName());
                    n.setType("reply");
                    n.setPostId(postId);
                    n.setCommentId(parent.getId());
                    n.setContent(comment.getUserName() + " 回复你：" + comment.getContent());
                    n.setIsRead(false);
                    n.setCreateTime(new Date());

                    notificationRepository.save(n);
                }
            }
        }

        // 更新评论数
        post.setCommentCount(
                post.getCommentCount() == null ? 1 : post.getCommentCount() + 1
        );
        postRepository.save(post);

        return Result.success("评论成功");
    }

    // ========================
    // 2. 获取评论列表
    // ========================
    @GetMapping("/list/{postId}")
    public Result<List<Comment>> getComments(@PathVariable String postId) {

        if (postId == null || postId.trim().isEmpty()) {
            return Result.error("帖子ID不能为空");
        }

        List<Comment> list =
                commentRepository.findByPostIdOrderByCreateTimeDesc(postId);

        return Result.success(list);
    }

    // ========================
    // 3. 删除评论（带权限）
    // ========================
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id,
                                 HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");

        if (userId == null) {
            return Result.error("未登录");
        }

        Optional<Comment> optional = commentRepository.findById(id);

        if (optional.isEmpty()) {
            return Result.error("评论不存在");
        }

        Comment comment = optional.get();

        if (!userId.equals(comment.getUserId())) {
            return Result.error("无权限删除");
        }

        // ⭐ 软删除
        comment.setIsDeleted(true);
        comment.setContent("该评论已删除");
        comment.setUserName(null);

        commentRepository.save(comment);

        return Result.success("删除成功");
    }

    // ========================
    // 4. 分页
    // ========================
    @GetMapping("/page")
    public Result<Page<Comment>> getCommentsByPage(
            @RequestParam String postId,
            @RequestParam int page,
            @RequestParam int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createTime")
        );

        return Result.success(
                commentRepository.findByPostId(postId, pageable)
        );
    }

    // ========================
    // 5. 评论点赞
    // ========================
    @PostMapping("/like/{commentId}")
    public Result<Map<String, Object>> likeComment(@PathVariable String commentId,
                                                   HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");

        Optional<Comment> optional = commentRepository.findById(commentId);
        if (optional.isEmpty()) return Result.error("评论不存在");

        Comment comment = optional.get();

        if (comment.getLikedUsers() == null) {
            comment.setLikedUsers(new ArrayList<>());
        }

        boolean isLike;

        if (comment.getLikedUsers().contains(userId)) {
            comment.getLikedUsers().remove(userId);
            isLike = false;
        } else {
            comment.getLikedUsers().add(userId);
            isLike = true;
        }
        // ========================
// ⭐ 评论点赞通知
// ========================
        if (isLike && !userId.equals(comment.getUserId())) {

            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isPresent()) {

                User u = userOpt.get();

                Notification n = new Notification();
                n.setUserId(comment.getUserId());
                n.setFromUserId(userId);
                n.setFromUserName(u.getUsername());
                n.setType("comment_like");
                n.setPostId(comment.getPostId());
                n.setCommentId(commentId);
                n.setContent(u.getUsername() + " 点赞了你的评论");
                n.setIsRead(false);
                n.setCreateTime(new Date());

                notificationRepository.save(n);
            }
        }

        comment.setLikes(comment.getLikedUsers().size());

        commentRepository.save(comment);

        Map<String, Object> result = new HashMap<>();
        result.put("likes", comment.getLikes());
        result.put("isLike", isLike);

        return Result.success(result);
    }

    // ========================
    // 6. 评论树
    // ========================
    @GetMapping("/tree/{postId}")
    public Result<List<Map<String, Object>>> getCommentTree(@PathVariable String postId) {

        List<Comment> roots =
                commentRepository.findByPostIdAndParentIdIsNullOrderByCreateTimeDesc(postId);

        List<Map<String, Object>> result = new ArrayList<>();

        for (Comment root : roots) {

            // ⭐ 查用户头像
            Optional<User> userOpt = userRepository.findById(root.getUserId());
            userOpt.ifPresent(user -> root.setUserAvatar(user.getAvatar()));

            Map<String, Object> map = new HashMap<>();
            map.put("comment", root);

            List<Comment> replies =
                    commentRepository.findByParentIdOrderByCreateTimeAsc(root.getId());

            // ⭐ 给回复补头像
            for (Comment reply : replies) {
                Optional<User> u = userRepository.findById(reply.getUserId());
                u.ifPresent(user -> reply.setUserAvatar(user.getAvatar()));
            }

            map.put("replies", replies);

            result.add(map);
        }

        return Result.success(result);
    }

    //7. 我的评论
    @GetMapping("/my")
    public Result<List<Comment>> getMyComments(HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");

        if (userId == null) {
            return Result.error("未登录");
        }

        List<Comment> list = commentRepository
                .findByUserIdOrderByCreateTimeDesc(userId);

        return Result.success(list);
    }
}