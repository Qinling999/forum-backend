package com.example.forum.controller;

import com.example.forum.common.Result;
import com.example.forum.model.History;
import com.example.forum.repository.CommentRepository;
import com.example.forum.repository.HistoryRepository;
import com.example.forum.repository.PostRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
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
    public Result<List<Map<String, Object>>> trend(
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) throws Exception {

        List<Map<String, Object>> list = new ArrayList<>();

        int days = 7;

        if ("30".equals(range)) days = 30;

        // =========================
        // ⭐ 自定义时间
        // =========================
        if ("custom".equals(range) && startDate != null && endDate != null) {

            Date start = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
            Date end = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);

            Calendar cal = Calendar.getInstance();
            cal.setTime(start);

            while (!cal.getTime().after(end)) {

                Date dayStart = statsService.getStartOfDay(cal.getTime());
                Date dayEnd = statsService.getEndOfDay(cal.getTime());

                Map<String, Object> map = statsService.buildDayData(dayStart, dayEnd);

                list.add(map);

                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            return Result.success(list);
        }

        // =========================
        // ⭐ 默认 7 / 30 天
        // =========================
        for (int i = days - 1; i >= 0; i--) {

            Date dayStart = statsService.getStartOfDay(i);
            Date dayEnd = statsService.getEndOfDay(i);

            Map<String, Object> map = statsService.buildDayData(dayStart, dayEnd);

            list.add(map);
        }

        return Result.success(list);
    }

    @GetMapping("/analysis/hot")
    public Result<Map<String, Object>> hotAnalysis() {
        return Result.success(statsService.getHotAnalysis());
    }

    @GetMapping("/analysis/recommend")
    public Result<Map<String, Object>> recommendAnalysis() {
        return Result.success(statsService.getRecommendAnalysis());
    }

    @GetMapping("/analysis/user")
    public Result<Map<String, Object>> userAnalysis() {
        return Result.success(statsService.getUserAnalysis());
    }
}
