package com.example.forum.controller;

import com.example.forum.model.Comment;
import com.example.forum.model.Notification;
import com.example.forum.model.Post;
import com.example.forum.model.User;
import com.example.forum.repository.CommentRepository;
import com.example.forum.repository.NotificationRepository;
import com.example.forum.repository.PostRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.common.Result;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/post")
@CrossOrigin
public class PostController {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private CommentRepository commentRepository;

    // ========================
    // 1. 创建帖子（必须登录）
    // ========================
    @PostMapping("/create")
    public Result<Post> createPost(@RequestBody Post post,
                                   HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        String token = request.getHeader("Authorization");

        if (token == null || token.isEmpty()) {
            token = request.getHeader("authorization");
        }

        if (userId == null) {
            return Result.error("未登录");
        }

        // 校验内容
        if (post.getContent() == null || post.getContent().trim().isEmpty()) {
            return Result.error("内容不能为空");
        }

        // 查当前用户
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return Result.error("用户不存在");
        }

        User user = userOpt.get();

        // 强制绑定用户信息
        post.setAuthorId(userId);
        post.setAuthorName(user.getUsername());
        post.setAuthorAvatar(user.getAvatar());

        // 初始化字段
        post.setCreateTime(new Date());
        post.setViews(0);
        post.setLikes(0);
        post.setCommentCount(0); // ⭐ 新结构必须有
        post.setLikedUsers(new ArrayList<>());

        return Result.success(postRepository.save(post));
    }

    // ========================
    // 2. 获取所有帖子
    // ========================
    @GetMapping("/list")
    public Result<List<Post>> getAllPosts() {
        List<Post> list = postRepository.findAllByOrderByCreateTimeDesc();

        for (Post p : list) {
            if (p.getAuthorAvatar() == null) {
                Optional<User> u = userRepository.findById(p.getAuthorId());
                u.ifPresent(user -> p.setAuthorAvatar(user.getAvatar()));
            }
        }

        return Result.success(postRepository.findAllByOrderByCreateTimeDesc());
    }

    // ========================
    // 3. 分类
    // ========================
    @GetMapping("/category/{categoryId}")
    public Result<List<Post>> getPostsByCategory(@PathVariable String categoryId) {

        if (categoryId == null) {
            return Result.error("分类不能为空");
        }

        return Result.success(
                postRepository.findByCategoryIdOrderByCreateTimeDesc(categoryId)
        );
    }

    // ========================
    // 4. 帖子详情（浏览+1）
    // ========================
    @GetMapping("/{id}")
    public Result<Post> getPostById(@PathVariable String id) {

        Optional<Post> optional = postRepository.findById(id);

        if (!optional.isPresent()) {
            return Result.error("帖子不存在");
        }

        Post post = optional.get();

        // ⭐ 补充作者头像
        Optional<User> userOpt = userRepository.findById(post.getAuthorId());
        userOpt.ifPresent(user -> post.setAuthorAvatar(user.getAvatar()));

        // 浏览量 +1
        post.setViews(post.getViews() + 1);
        postRepository.save(post);

        return Result.success(post);
    }
    // ========================
// 5. 添加评论（必须登录）
// ========================

    // ========================
// 6. 点赞 / 取消点赞（必须登录）
// ========================
    @PostMapping("/like/{postId}")
    public Result<Post> likePost(@PathVariable String postId,
                                 HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");

        Optional<Post> optional = postRepository.findById(postId);

        if (!optional.isPresent()) {
            return Result.error("帖子不存在");
        }

        Post post = optional.get();

        if (post.getLikedUsers() == null) {
            post.setLikedUsers(new ArrayList<>());
        }

        boolean isLike;

        if (post.getLikedUsers().contains(userId)) {
            post.getLikedUsers().remove(userId);
            isLike = false;
        } else {
            post.getLikedUsers().add(userId);
            isLike = true;

            // ⭐ 新增：点赞通知
            if (isLike && !userId.equals(post.getAuthorId())) {

                Optional<User> userOpt = userRepository.findById(userId);

                if (userOpt.isEmpty()) return Result.success(post);

                User u = userOpt.get();

                boolean exists = notificationRepository
                        .existsByUserIdAndFromUserIdAndPostIdAndType(
                                post.getAuthorId(),
                                userId,
                                postId,
                                "like"
                        );

                if (!exists) {
                    Notification n = new Notification();
                    n.setUserId(post.getAuthorId());
                    n.setFromUserId(userId);
                    n.setFromUserName(u.getUsername());
                    n.setType("like");
                    n.setPostId(postId);
                    n.setContent(u.getUsername() + " 点赞了你的帖子");
                    n.setIsRead(false);
                    n.setCreateTime(new Date());

                    notificationRepository.save(n);
                }
            }
        }

        post.setLikes(post.getLikedUsers().size());

        return Result.success(postRepository.save(post));
    }

    // ========================
    // 7. 分页
    // ========================
    @GetMapping("/page")
    public Result<Page<Post>> getPostsByPage(
            @RequestParam int page,
            @RequestParam int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createTime")
        );

        return Result.success(postRepository.findAll(pageable));
    }

    // ========================
    // 8. 搜索
    // ========================
    @GetMapping("/search")
    public Result<List<Post>> searchPosts(@RequestParam String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.error("关键词不能为空");
        }

        return Result.success(postRepository.searchPosts(keyword));
    }

    // ========================
    // 9. 我的帖子（必须登录）
    // ========================
    @GetMapping("/my")
    public Result<List<Post>> getMyPosts(HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");

        return Result.success(
                postRepository.findByAuthorIdOrderByCreateTimeDesc(userId)
        );
    }

    // ========================
    // 10. 删除帖子（权限校验）
    // ========================
    @DeleteMapping("/delete/{postId}")
    public Result<String> deletePost(@PathVariable String postId,
                                     HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");

        Optional<Post> optional = postRepository.findById(postId);

        if (!optional.isPresent()) {
            return Result.error("帖子不存在");
        }

        Post post = optional.get();

        if (!post.getAuthorId().equals(userId)) {
            return Result.error("无权限删除");
        }

        postRepository.deleteById(postId);

        return Result.success("删除成功");
    }

    // ========================
    // 11. 搜索 + 分页
    // ========================
    @GetMapping("/searchPage")
    public Result<Page<Post>> searchPostsByPage(
            @RequestParam String keyword,
            @RequestParam int page,
            @RequestParam int size) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.error("关键词不能为空");
        }
        if (page < 0 || size <= 0) {
            return Result.error("分页参数错误");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createTime")
        );

        return Result.success(postRepository.searchPosts(keyword, pageable));
    }


    // ========================
// 12. 根据用户ID获取帖子
// ========================
    @GetMapping("/user/{userId}")
    public Result<List<Post>> getPostsByUserId(@PathVariable String userId) {

        if (userId == null || userId.isEmpty()) {
            return Result.error("用户ID不能为空");
        }

        return Result.success(
                postRepository.findByAuthorIdOrderByCreateTimeDesc(userId)
        );
    }

    //13. 上传图片
    @PostMapping("/upload/picture")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            // ⭐ 防止文件名为 null
            String originalName = file.getOriginalFilename();
            if (originalName == null) {
                originalName = "image.jpg";
            }

            String fileName = UUID.randomUUID() + "_" + originalName;

            // ⭐ 上传目录
            String uploadDir = System.getProperty("user.dir") + "/uploads/post/";

            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    return Result.error("目录创建失败");
                }
            }

            File dest = new File(uploadDir + fileName);

            // ⭐ 保存文件
            file.transferTo(dest);

            // ⭐ 返回访问路径
            String url = "http://localhost:8080/upload/post/" + fileName;

            return Result.success(url);

        } catch (Exception e) {
            e.printStackTrace(); // ⭐ 一定要打印
            return Result.error("上传失败：" + e.getMessage());
        }
    }

}