package com.example.forum.model;

import com.example.forum.common.Result;
import com.example.forum.repository.CommentRepository;
import com.example.forum.repository.HistoryRepository;
import com.example.forum.repository.PostRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.service.StatsService;
import com.example.forum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private StatsService statsService;

    // =========================
    // ⭐ 概览接口
    // =========================
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {

        Map<String, Object> map = new HashMap<>();

        // =========================
        // ⭐ 时间范围（今天）
        // =========================
        Date start = getStartOfDay();
        Date end = new Date();

        // =========================
        // ⭐ 基础统计
        // =========================
        long userCount = userRepository.count();
        long postCount = postRepository.count();

        long todayUsers = userRepository.countByCreateTimeBetween(start, end);
        long todayPosts = postRepository.countByCreateTimeBetween(start, end);

        // =========================
        // ⭐ 活跃用户（去重）
        // =========================
        List<History> histories = historyRepository.findAllUserIds();

        Set<String> userSet = new HashSet<>();
        for (History h : histories) {
            if (h.getUserId() != null) {
                userSet.add(h.getUserId());
            }
        }

        int activeUsers = userSet.size();

        // =========================
        // ⭐ 平均停留时间
        // =========================
        double avgDuration = 0;

        List<Map<String, Object>> avgList = historyRepository.avgDuration();

        if (!avgList.isEmpty() && avgList.get(0).get("avg") != null) {
            avgDuration = Double.parseDouble(
                    avgList.get(0).get("avg").toString()
            );
        }

        // =========================
        // ⭐ 总浏览量
        // =========================
        long totalViews = postRepository.sumViews();

        // =========================
        // ⭐ 总点赞数
        // =========================
        long totalLikes = postRepository.sumLikes();

        // =========================
        // ⭐ 总评论数
        // =========================
        long totalComments = commentRepository.count();

        // =========================
        // ⭐ 返回
        // =========================
        map.put("userCount", userCount);
        map.put("postCount", postCount);
        map.put("todayUsers", todayUsers);
        map.put("todayPosts", todayPosts);
        map.put("activeUsers", activeUsers);
        map.put("avgDuration", avgDuration);

        map.put("totalViews", totalViews);
        map.put("totalLikes", totalLikes);
        map.put("totalComments", totalComments);

        return Result.success(map);
    }

    // =========================
    // ⭐ 获取今天开始时间
    // =========================
    private Date getStartOfDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> trend() {

        List<Map<String, Object>> list = new ArrayList<>();

        // ⭐ 最近7天
        for (int i = 30; i >= 0; i--) {

            Date start = statsService.getStartOfDay(i);
            Date end = statsService.getEndOfDay(i);

            Map<String, Object> map = new HashMap<>();

            // ======================
            // 日期
            // ======================
            map.put("date", statsService.formatDate(start));

            // ======================
            // 用户新增
            // ======================
            long users = userRepository.countByCreateTimeBetween(start, end);

            // ======================
            // 帖子新增
            // ======================
            long posts = postRepository.countByCreateTimeBetween(start, end);

            // ======================
            // 活跃用户（去重）
            // ======================
            List<History> histories =
                    historyRepository.findByCreateTimeBetween(start, end);

            Set<String> activeSet = new HashSet<>();

            for (History h : histories) {
                if (h.getUserId() != null) {
                    activeSet.add(h.getUserId());
                }
            }

            int active = activeSet.size();

            // ======================
            // 填充
            // ======================
            map.put("users", users);
            map.put("posts", posts);
            map.put("active", active);

            list.add(map);
        }

        return Result.success(list);
    }

    @GetMapping("/analysis/hot")
    public Result<Map<String, Object>> hotAnalysis() {
        return Result.success(statsService.getHotAnalysis());
    }
}
