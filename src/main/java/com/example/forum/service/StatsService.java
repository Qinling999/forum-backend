package com.example.forum.service;

import com.example.forum.model.Category;
import com.example.forum.model.Post;
import com.example.forum.repository.CategoryRepository;
import com.example.forum.repository.CommentRepository;
import com.example.forum.repository.PostRepository;
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
}
