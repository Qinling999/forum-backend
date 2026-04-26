package com.example.forum.controller;

import com.example.forum.model.History;
import com.example.forum.model.Notification;
import com.example.forum.model.Post;
import com.example.forum.model.User;
import com.example.forum.repository.*;
import com.example.forum.service.UserService;
import com.example.forum.service.VisitorService;
import com.example.forum.util.JwtUtil;
import com.example.forum.common.Result;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private UserService userService;
    @Autowired
    private VisitorService visitorService;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private HistoryRepository historyRepository;

    // ========================
    // 1. 注册
    // ========================
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {

        if (user.getUsername() == null || user.getUsername().trim().length() == 0
                || user.getPassword() == null || user.getPassword().trim().length() == 0) {
            return Result.error("用户名或密码不能为空");
        }

        User exist = userRepository.findByUsername(user.getUsername());
        if (exist != null) {
            return Result.error("用户已存在");
        }

        // ⭐ 加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.setCreateTime(new Date());
        user.setFollowing(new ArrayList<>());
        user.setFollowers(new ArrayList<>());
        user.setRole("user");

        return Result.success(userRepository.save(user));
    }

    // ========================
    // 2. 登录
    // ========================
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User user) {

        if (user.getUsername() == null || user.getPassword() == null) {
            return Result.error("用户名或密码不能为空");
        }

        User dbUser = userRepository.findByUsername(user.getUsername());

        if (dbUser == null) {
            return Result.error("用户不存在");
        }

        if (!passwordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
            return Result.error("密码错误");
        }

        // ⭐ 加上 role
        String token = JwtUtil.generateToken(
                dbUser.getId(),
                dbUser.getUsername(),
                dbUser.getRole()
        );

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", dbUser);

        return Result.success(data);
    }

    // ========================
    // 3. 关注
    // ========================
    @PostMapping("/follow")
    public Result<String> follow(@RequestParam String targetUserId,
                                 HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");

        if (targetUserId == null || targetUserId.trim().isEmpty()) {
            return Result.error("目标用户不能为空");
        }

        if (userId.equals(targetUserId)) {
            return Result.error("不能关注自己");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        Optional<User> targetOpt = userRepository.findById(targetUserId);

        if (!userOpt.isPresent() || !targetOpt.isPresent()) {
            return Result.error("用户不存在");
        }

        User user = userOpt.get();
        User target = targetOpt.get();

        if (user.getFollowing() == null) user.setFollowing(new ArrayList<>());
        if (target.getFollowers() == null) target.setFollowers(new ArrayList<>());

        // 取消关注
        if (user.getFollowing().contains(targetUserId)) {

            user.getFollowing().remove(targetUserId);
            target.getFollowers().remove(userId);

            userRepository.save(user);
            userRepository.save(target);

            return Result.success("已取消关注");

        } else {

            user.getFollowing().add(targetUserId);
            target.getFollowers().add(userId);

            userRepository.save(user);
            userRepository.save(target);

            // ⭐⭐⭐ 关键修复
            try {
                Notification n = new Notification();
                n.setUserId(targetUserId);
                n.setType("follow");

                n.setFromUserId(userId);
                n.setFromUserName(user.getUsername()); // ⭐补上

                n.setContent(user.getUsername() + " 关注了你"); // ⭐统一

                n.setIsRead(false);
                n.setCreateTime(new Date());

                notificationRepository.save(n);

            } catch (Exception e) {
                System.out.println("通知创建失败");
            }

            return Result.success("关注成功");
        }
    }


    // ========================
    // 4. 我的关注
    // ========================
    @GetMapping("/following")
    public Result<List<User>> getFollowing(HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");

        Optional<User> userOpt = userRepository.findById(userId);

        if (!userOpt.isPresent()) {
            return Result.error("用户不存在");
        }

        List<User> list = userRepository.findAllById(userOpt.get().getFollowing());

        return Result.success(list);
    }

    // ========================
    // 5. 我的粉丝
    // ========================
    @GetMapping("/followers")
    public Result<List<User>> getFollowers(HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");

        Optional<User> userOpt = userRepository.findById(userId);

        if (!userOpt.isPresent()) {
            return Result.error("用户不存在");
        }

        List<User> list = userRepository.findAllById(userOpt.get().getFollowers());

        return Result.success(list);
    }

    // ========================
// 6. 收藏 / 取消收藏
// ========================
    @PostMapping("/favorite")
    public Result<String> toggleFavorite(
            @RequestParam String postId,
            HttpServletRequest request) {

        String userId = getUserIdFromRequest(request);

        if (userId == null) {
            return Result.error("未登录");
        }

        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            return Result.error("用户不存在");
        }

        User user = optionalUser.get();

        if (user.getFavoritePosts() == null) {
            user.setFavoritePosts(new ArrayList<>());
        }

        List<String> favorites = user.getFavoritePosts();

        // ⭐ 已收藏 → 取消收藏
        if (favorites.contains(postId)) {
            favorites.remove(postId);
            userRepository.save(user);
            return Result.success("取消收藏成功");
        }

        // ⭐ 未收藏 → 收藏
        favorites.add(postId);
        userRepository.save(user);

        return Result.success("收藏成功");
    }


    // ========================
// 7. 获取收藏列表（改为从 token 获取用户）
// ========================
    @GetMapping("/favorite/list")
    public Result<List<Post>> getFavoritePosts(HttpServletRequest request) {

        String userId = getUserIdFromRequest(request);

        if (userId == null) {
            return Result.error("未登录");
        }

        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            return Result.error("用户不存在");
        }

        User user = optionalUser.get();

        List<String> ids = user.getFavoritePosts();

        if (ids == null || ids.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        List<Post> posts = postRepository.findAllById(ids);

        return Result.success(posts);
    }


    // ========================
// ⭐ 工具方法：从请求中解析 userId
// ========================
    private String getUserIdFromRequest(HttpServletRequest request) {

        String token = request.getHeader("Authorization");

        if (token == null || token.isEmpty()) {
            return null;
        }

        // 如果用的是 Bearer token，需要加这一行
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return JwtUtil.getUserId(token);
    }

    @PutMapping("/update")
    public Result<User> updateUser(
            @RequestBody Map<String, String> params,
            HttpServletRequest request) {

        try {
            String userId = getUserIdFromRequest(request);

            if (userId == null) {
                return Result.error("未登录");
            }

            Optional<User> optionalUser = userRepository.findById(userId);

            if (optionalUser.isEmpty()) {
                return Result.error("用户不存在");
            }

            User user = optionalUser.get();

            String oldUsername = user.getUsername();

            // ======================
            // ⭐ 安全更新 username
            // ======================
            if (params.containsKey("username")) {

                String newName = params.get("username");

                // ❗关键保护：不能为空
                if (newName != null && !newName.trim().isEmpty()) {
                    user.setUsername(newName);
                }
            }

            if (params.containsKey("email")) {
                String email = params.get("email");
                if (email != null) {
                    user.setEmail(email);
                }
            }

            if (params.containsKey("avatar")) {
                String avatar = params.get("avatar");
                if (avatar != null) {
                    user.setAvatar(avatar);
                }
            }

            userRepository.save(user);

            // ======================
            // ⭐ 同步帖子（安全版）
            // ======================
            if (params.containsKey("username")) {

                String newName = user.getUsername();

                // ❗必须确保新名字有效
                if (newName != null && !newName.trim().isEmpty()
                        && !newName.equals(oldUsername)) {

                    List<Post> posts = postRepository.findByAuthorId(userId);

                    for (Post post : posts) {

                        // ❗只更新有问题的数据（可选优化）
                        if (post.getAuthorName() == null
                                || post.getAuthorName().trim().isEmpty()
                                || post.getAuthorName().equals(oldUsername)) {

                            post.setAuthorName(newName);
                        }
                    }

                    postRepository.saveAll(posts);
                }
            }

            return Result.success(user);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败");
        }
    }

    //9. 上传头像
    @PostMapping("/upload/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {

        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            String uploadDir = System.getProperty("user.dir") + "/uploads/avatar/";

            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File dest = new File(uploadDir + fileName);
            file.transferTo(dest);

            String url = "http://localhost:8080/upload/avatar/" + fileName;

            return Result.success(url);

        } catch (Exception e) {
            return Result.error("上传失败");
        }
    }

    //10. 获取用户
    @GetMapping("/{id}")
    public Result getUserById(@PathVariable String id,
                              HttpServletRequest request) {

        // ⭐ 当前登录用户
        String visitorId = (String) request.getAttribute("userId");

        // ⭐ 记录访客
        if (visitorId != null && !visitorId.equals(id)) {
            visitorService.recordVisit(id, visitorId);
        }

        Optional<User> optional = userRepository.findById(id);

        if (optional.isEmpty()) {
            return Result.error("用户不存在");
        }

        return Result.success(optional.get());
    }

    //11. 统计
    @GetMapping("/stats/{userId}")
    public Result getUserStats(@PathVariable String userId) {

        Map<String, Object> map = new HashMap<>();

        // 关注
        map.put("followers", userService.countFans(userId));
        map.put("following", userService.countFollowing(userId));

        // 帖子
        map.put("posts", postRepository.countByAuthorId(userId));

        // ⭐ 评论数
        map.put("comments", commentRepository.countByUserId(userId));

        // ⭐ 收藏数
        User user = userRepository.findById(userId).orElse(null);

        if (user != null && user.getFavoritePosts() != null) {
            map.put("favorites", user.getFavoritePosts().size());
        } else {
            map.put("favorites", 0);
        }


        return Result.success(map);
    }

    //修复
    @GetMapping("/fixPostAuthorName")
    public Result<?> fixPostAuthorName() {

        List<Post> posts = postRepository.findAll();

        for (Post post : posts) {

            if (post.getAuthorName() == null || post.getAuthorName().trim().isEmpty()) {

                Optional<User> userOpt = userRepository.findById(post.getAuthorId());

                if (userOpt.isPresent()) {
                    post.setAuthorName(userOpt.get().getUsername());
                }
            }
        }

        postRepository.saveAll(posts);

        return Result.success("修复完成");
    }

    // ======================
    // 12. 指定用户关注列表
    // ======================
    // 指定用户 ⭐
    @GetMapping("/following/{userId}")
    public Result getFollowing(@PathVariable String userId) {

        return Result.success(
                userService.getFollowing(userId)
        );
    }

    // ======================
    // ⭐ 指定用户粉丝列表
    // ======================
    @GetMapping("/fans/{userId}")
    public Result getFans(@PathVariable String userId) {
        return Result.success(userService.getFans(userId));
    }

    //13.修改密码
    @PostMapping("/changePassword")
    public Result<String> changePassword(
            @RequestBody Map<String, String> params,
            HttpServletRequest request) {

        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        String confirmPassword = params.get("confirmPassword");

        if (oldPassword == null || newPassword == null || confirmPassword == null) {
            return Result.error("参数不完整");
        }

        if (!newPassword.equals(confirmPassword)) {
            return Result.error("两次输入的新密码不一致");
        }

        // 获取当前用户（从JWT解析）
        String userId = (String) request.getAttribute("userId");

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 校验旧密码（BCrypt）
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error("旧密码错误");
        }

        // 新旧密码不能一样
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            return Result.error("新密码不能与旧密码相同");
        }

        // 加密新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return Result.success("修改成功，请重新登录");
    }

    //13. 后台数据统计
    private Date todayStart() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    @GetMapping("/admin/dashboard")
    public Result<Map<String, Object>> getDashboard(HttpServletRequest request) {
        try {
            String role = (String) request.getAttribute("role");

            if (!"admin".equals(role)) {
                return Result.error("无权限");
            }

            Map<String, Object> data = new HashMap<>();
            Date start = todayStart();

            long totalUsers = userRepository.count();
            long newUsersToday = userRepository.countByCreateTimeAfter(start);

            long totalPosts = postRepository.count();
            long newPostsToday = postRepository.countByCreateTimeAfter(start);

            long totalViews = postRepository.sumViews(); // ⭐重点
            long totalLikes = postRepository.sumLikes(); // ⭐重点
            long totalComments = commentRepository.count();

            List<History> historyList = historyRepository.findAll();

            double avgDuration = historyList.stream()
                    .filter(h -> h.getDuration() != null)
                    .mapToLong(History::getDuration)
                    .average()
                    .orElse(0);

            data.put("totalUsers", totalUsers);
            data.put("newUsersToday", newUsersToday);
            data.put("totalPosts", totalPosts);
            data.put("newPostsToday", newPostsToday);
            data.put("totalViews", totalViews);
            data.put("totalLikes", totalLikes);
            data.put("totalComments", totalComments);
            data.put("avgDuration", avgDuration);

            return Result.success(data);

        } catch (Exception e) {
            e.printStackTrace(); // ⭐⭐⭐ 关键
            return Result.error("系统运行异常");
        }
    }

}