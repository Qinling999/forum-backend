package com.example.forum.service;

import com.example.forum.model.Category;
import com.example.forum.model.History;
import com.example.forum.model.Post;
import com.example.forum.model.User;
import com.example.forum.repository.*;
import com.example.forum.vo.CategoryStatVO;
import com.example.forum.vo.HotPostVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HistoryRepository historyRepository;

    public Date getStartOfDay(int daysAgo) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public Date getEndOfDay(int daysAgo) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    public String formatDate(Date date) {
        return new java.text.SimpleDateFormat("MM-dd").format(date);
    }

    public Map<String, Object> getHotAnalysis() {

        List<Post> posts = postRepository.findAll();

        Map<String, Object> result = new HashMap<>();

        // =========================
        // 1️⃣ Top 帖子（全站）
        // =========================
        List<HotPostVO> topPosts = posts.stream()
                .map(this::buildHotPost)
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(10)
                .collect(Collectors.toList());

        result.put("topPosts", topPosts);

        // =========================
        // 2️⃣ 分类统计（关键修复点）
        // =========================
        Map<String, Long> categoryCount = posts.stream()
                .collect(Collectors.groupingBy(
                        Post::getCategoryId,
                        Collectors.counting()
                ));

        long total = posts.size();

        // =========================
        // 3️⃣ 分类名称映射
        // =========================
        List<Category> categories = categoryRepository.findAll();

        Map<String, String> categoryMap = new HashMap<>();
        for (Category c : categories) {
            categoryMap.put(c.getId(), c.getName());
        }

        // =========================
        // 4️⃣ 分类分布（带名称）
        // =========================
        List<CategoryStatVO> categoryStats = new ArrayList<>();

        for (Map.Entry<String, Long> entry : categoryCount.entrySet()) {

            String categoryId = entry.getKey();

            CategoryStatVO vo = new CategoryStatVO();
            vo.setCategoryId(categoryId);

            // ⭐ 分类名称
            vo.setCategoryName(
                    categoryMap.getOrDefault(categoryId, "未知分类")
            );

            vo.setCount(entry.getValue());

            vo.setPercentage(
                    total == 0 ? 0 :
                            entry.getValue() * 1.0 / total
            );

            categoryStats.add(vo);
        }

        result.put("categoryDistribution", categoryStats);

        // =========================
// 5️⃣ 分类 Top 榜（带名称）
// =========================
        Map<String, List<HotPostVO>> categoryTop = new HashMap<>();

        for (String categoryId : categoryCount.keySet()) {

            List<HotPostVO> list = posts.stream()
                    .filter(p -> categoryId.equals(p.getCategoryId()))
                    .map(this::buildHotPost)
                    .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                    .limit(5)
                    .collect(Collectors.toList());

            // ⭐ 包装成结构体（推荐）
            Map<String, Object> wrapper = new HashMap<>();
            wrapper.put("categoryName", categoryMap.getOrDefault(categoryId, "未知分类"));
            wrapper.put("list", list);

            categoryTop.put(categoryId, list); // 如果你不想改前端结构可以保留原结构
        }

        result.put("categoryTop", categoryTop);

        return result;
    }

    private HotPostVO buildHotPost(Post post) {

        HotPostVO vo = new HotPostVO();

        vo.setId(post.getId());
        vo.setTitle(post.getTitle());
        vo.setCategoryId(post.getCategoryId());
        vo.setLikes(post.getLikes());
        vo.setViews(post.getViews());

        long commentCount = commentRepository.countByPostId(post.getId());
        vo.setCommentCount(commentCount);

        double score =
                post.getLikes() * 3 +
                        commentCount * 2 +
                        post.getViews();

        vo.setScore(score);

        return vo;
    }

    public Map<String, Object> buildDayData(Date start, Date end) {

        Map<String, Object> map = new HashMap<>();

        map.put("date", new java.text.SimpleDateFormat("MM-dd").format(start));

        long users = userRepository.countByCreateTimeBetween(start, end);
        long posts = postRepository.countByCreateTimeBetween(start, end);

        List<History> histories =
                historyRepository.findByCreateTimeBetween(start, end);

        Set<String> activeSet = new HashSet<>();

        for (History h : histories) {
            if (h.getUserId() != null) {
                activeSet.add(h.getUserId());
            }
        }

        map.put("users", users);
        map.put("posts", posts);
        map.put("active", activeSet.size());

        return map;
    }

    public Date parseDate(String dateStr) {
        try {
            return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        } catch (Exception e) {
            throw new RuntimeException("日期格式错误");
        }
    }

    // =========================
// ⭐ 新增：支持 Date 参数
// =========================
    public Date getStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    public Date getEndOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);

        return cal.getTime();
    }

    //推荐系统分析
    public Map<String, Object> getRecommendAnalysis() {

        Map<String, Object> result = new HashMap<>();

        result.put("exposure", buildExposure());
        result.put("interest", buildInterest());
        result.put("behavior", buildBehavior());
        result.put("performance", buildPerformance());

        return result;
    }
    private Map<String, Object> buildExposure() {

        List<History> list = historyRepository.findAll();

        long exposure = list.size();
        long validClick = list.stream()
                .filter(h -> h.getDuration() != null && h.getDuration() > 2)
                .count();

        double ctr = exposure == 0 ? 0 :
                validClick * 1.0 / exposure;

        Map<String, Object> map = new HashMap<>();
        map.put("exposure", exposure);
        map.put("click", validClick);
        map.put("ctr", ctr);

        return map;
    }
    private List<CategoryStatVO> buildInterest() {

        List<History> historyList = historyRepository.findAll();

        Map<String, Double> scoreMap = new HashMap<>();

        for (History h : historyList) {

            Post post = postRepository.findById(h.getPostId()).orElse(null);
            if (post == null) continue;

            String category = post.getCategoryId();

            double score = 1;

            if (h.getDuration() != null) {
                score += h.getDuration() / 10.0;
            }

            scoreMap.put(category,
                    scoreMap.getOrDefault(category, 0.0) + score);
        }

        // ⭐ 分类名称映射
        List<Category> categories = categoryRepository.findAll();
        Map<String, String> categoryMap = new HashMap<>();
        for (Category c : categories) {
            categoryMap.put(c.getId(), c.getName());
        }

        double total = scoreMap.values().stream()
                .mapToDouble(Double::doubleValue).sum();

        List<CategoryStatVO> result = new ArrayList<>();

        for (Map.Entry<String, Double> entry : scoreMap.entrySet()) {

            CategoryStatVO vo = new CategoryStatVO();

            vo.setCategoryId(entry.getKey());

            // ⭐ 核心修改
            vo.setCategoryName(
                    categoryMap.getOrDefault(entry.getKey(), "未知分类")
            );

            vo.setCount(entry.getValue().longValue());

            vo.setPercentage(
                    total == 0 ? 0 : entry.getValue() / total
            );

            result.add(vo);
        }

        return result;
    }
    private Map<String, Object> buildBehavior() {

        Map<String, Object> map = new HashMap<>();

        List<History> historyList = historyRepository.findAll();
        List<User> users = userRepository.findAll();

        long view = historyList.size();

        long longView = historyList.stream()
                .filter(h -> h.getDuration() != null && h.getDuration() > 20)
                .count();

        long dislike = users.stream()
                .mapToLong(u -> u.getDislikedPostIds() == null ? 0 : u.getDislikedPostIds().size())
                .sum();

        long block = users.stream()
                .mapToLong(u -> u.getBlockedUserIds() == null ? 0 : u.getBlockedUserIds().size())
                .sum();

        map.put("view", view);
        map.put("longView", longView);
        map.put("dislike", dislike);
        map.put("block", block);

        return map;
    }
    private Map<String, Object> buildPerformance() {

        List<History> list = historyRepository.findAll();

        double avgDuration = list.stream()
                .filter(h -> h.getDuration() != null)
                .mapToLong(History::getDuration)
                .average()
                .orElse(0);

        long highQuality = list.stream()
                .filter(h -> h.getDuration() != null && h.getDuration() > 30)
                .count();

        double ratio = list.size() == 0 ? 0 :
                highQuality * 1.0 / list.size();

        Map<String, Object> map = new HashMap<>();
        map.put("avgDuration", avgDuration);
        map.put("highQualityRatio", ratio);

        return map;
    }

    //用户行为分析
    public Map<String, Object> getUserAnalysis() {

        Map<String, Object> result = new HashMap<>();

        result.put("behaviorStats", buildBehaviorStats());
        result.put("durationStats", buildDurationStats());
        result.put("activeUsers", buildActiveUsers());

        return result;
    }
    private Map<String, Object> buildBehaviorStats() {

        List<History> historyList = historyRepository.findAll();
        List<Post> posts = postRepository.findAll();
        List<User> users = userRepository.findAll();

        // 👁 浏览
        long view = historyList.size();

        // 👍 点赞（直接用帖子里的点赞数）
        long like = posts.stream()
                .mapToLong(Post::getLikes)
                .sum();

        // 🙈 不喜欢
        long dislike = users.stream()
                .mapToLong(u ->
                        u.getDislikedPostIds() == null ? 0 : u.getDislikedPostIds().size()
                )
                .sum();

        // 🚫 拉黑
        long block = users.stream()
                .mapToLong(u ->
                        u.getBlockedUserIds() == null ? 0 : u.getBlockedUserIds().size()
                )
                .sum();

        Map<String, Object> map = new HashMap<>();
        map.put("view", view);
        map.put("like", like);
        map.put("dislike", dislike);
        map.put("block", block);

        return map;
    }
    private Map<String, Object> buildDurationStats() {

        List<History> list = historyRepository.findAll();

        long shortStay = list.stream()
                .filter(h -> h.getDuration() != null && h.getDuration() < 5)
                .count();

        long mediumStay = list.stream()
                .filter(h -> h.getDuration() != null && h.getDuration() >= 5 && h.getDuration() < 20)
                .count();

        long longStay = list.stream()
                .filter(h -> h.getDuration() != null && h.getDuration() >= 20)
                .count();

        Map<String, Object> map = new HashMap<>();
        map.put("short", shortStay);
        map.put("medium", mediumStay);
        map.put("long", longStay);

        return map;
    }
    private List<Map<String, Object>> buildActiveUsers() {

        List<History> list = historyRepository.findAll();

        Map<String, Long> userMap = list.stream()
                .collect(Collectors.groupingBy(
                        History::getUserId,
                        Collectors.counting()
                ));

        return userMap.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .map(e -> {

                    Map<String, Object> m = new HashMap<>();

                    String userId = e.getKey();

                    User user = userRepository.findById(userId).orElse(null);

                    m.put("userId", userId);

                    // ⭐ 核心修改
                    m.put("userName",
                            user != null ? user.getUsername() : "未知用户");

                    m.put("count", e.getValue());

                    return m;
                })
                .collect(Collectors.toList());
    }
}
