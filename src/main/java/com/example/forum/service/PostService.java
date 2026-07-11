package com.example.forum.service;

import com.example.forum.model.History;
import com.example.forum.model.Post;
import com.example.forum.model.User;
import com.example.forum.repository.CommentRepository;
import com.example.forum.repository.HistoryRepository;
import com.example.forum.repository.PostRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.vo.PostVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private CommentRepository commentRepository;

    public List<PostVO> recommend(String userId, String categoryId) {

        List<Post> posts = postRepository.findAll();
        User user = userRepository.findById(userId).orElse(null);
        Map<String, Double> interestMap = buildUserInterestScore(userId);
        Set<String> followingIds = getFollowingIds(userId);

        List<PostVO> result = new ArrayList<>();

        for (Post post : posts) {
            // ==========================
// ❌ 拉黑作者（直接过滤）
// ==========================

            Set<String> blockedIds = user != null && user.getBlockedUserIds() != null
                    ? new HashSet<>(user.getBlockedUserIds())
                    : new HashSet<>();

            if (blockedIds.contains(post.getAuthorId())) {
                continue;
            }

            if (categoryId != null && !categoryId.isEmpty()
                    && !categoryId.equals(post.getCategoryId())) {
                continue;
            }

            double score = 0;
            List<String> reasons = new ArrayList<>();

            // ==========================
            // ⭐ 1. 兴趣（最高优先级）
            // ==========================
            double interestScore = interestMap.getOrDefault(post.getCategoryId(), 0.0);
            score += interestScore * 10;

            if (interestScore > 5) {
                reasons.add("🎯 符合你的兴趣");
            }

            // ==========================
            // ⭐ 2. 关注
            // ==========================
            if (followingIds.contains(post.getAuthorId())) {
                score += 80;
                reasons.add("👀 来自你关注的人");
            }

            // 3. 相似用户
            Set<String> collaborativePosts = getCollaborativePosts(userId);

            if (collaborativePosts.contains(post.getId())) {
                score += 60;
                reasons.add("👥 相似用户喜欢");
            }

            // ==========================
            // ⭐ 4. 热度
            // ==========================
            long hot = (post.getLikes() * 3)
                    + (post.getViews())
                    + (commentRepository.countByPostId(post.getId()) * 2);

            score += hot * 0.3;

            if (hot > 50) {
                reasons.add("🔥 热门内容");
            }

            // ==========================
            // ⭐ 5. 时间（最低权重）
            // ==========================
            long hours = (System.currentTimeMillis() -
                    post.getCreateTime().getTime()) / (1000 * 60 * 60);

            double timeScore = 1.0 / (1 + hours * 0.1);
            score += timeScore * 50;

            if (hours < 24) {
                reasons.add("🆕 最近发布");
            }



            // ==========================
// ❌ 不喜欢（降权）
// ==========================
            Set<String> dislikedPostIds = user != null && user.getDislikedPostIds() != null
                    ? new HashSet<>(user.getDislikedPostIds())
                    : new HashSet<>();

            if (dislikedPostIds.contains(post.getId().toString())) {
                score -= 100;// 强烈降权
                reasons.add("🙈 已标记不喜欢");
            }

            // ==========================
// ⭐ 停留时间（高价值行为）
// ==========================
            double durationScore = getDurationScore(userId, post.getId());
            score += durationScore;

            if (durationScore > 30) {
                reasons.add("⏳ 停留时间较长");
            }

            // ==========================
            // VO
            // ==========================
            PostVO vo = new PostVO();
            BeanUtils.copyProperties(post, vo);

            long commentCount = commentRepository.countByPostId(post.getId());
            vo.setCommentCount(commentCount);

// ⭐ 强制补充（防止字段不一致/未来改名出问题）
            vo.setCategoryId(post.getCategoryId());

            vo.setScore(score);

            // ⭐ 保证理由顺序就是优先级顺序
            vo.setReason(String.join(" · ", reasons));

            result.add(vo);
        }

        // ==========================
        // 排序
        // ==========================
        result.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // ==========================
        // ⭐ 兜底机制（必须存在）
        // ==========================
        if (result.isEmpty()) {

            List<PostVO> fallback = new ArrayList<>();

            for (Post post : posts) {

                double score = 0;

                int hot = (post.getLikes() * 3) + post.getViews();
                score += hot;

                long hours = (System.currentTimeMillis() -
                        post.getCreateTime().getTime()) / (1000 * 60 * 60);

                double timeScore = 1.0 / (1 + hours * 0.1);
                score += timeScore * 100;

                PostVO vo = new PostVO();
                BeanUtils.copyProperties(post, vo);

                long commentCount = commentRepository.countByPostId(post.getId());
                vo.setCommentCount(commentCount);

                vo.setScore(score);
                vo.setReason("🔥 热门推荐 · 🆕 最新内容");

                fallback.add(vo);
            }

            fallback.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

            return fallback;
        }

        return result;
    }

    private Map<String, Double> buildUserInterestScore(String userId) {

        Map<String, Double> scoreMap = new HashMap<>();

        // ======================
        // ⭐ 1. 浏览 + 停留时间
        // ======================
        List<History> historyList = historyRepository.findByUserId(userId);

        for (History h : historyList) {

            Post post = postRepository.findById(h.getPostId()).orElse(null);
            if (post == null) continue;

            String cid = post.getCategoryId();

            double score = 1;

            if (h.getDuration() != null) {
                long d = h.getDuration();
                if (d > 30) score += 5;
                else if (d > 10) score += 2;
            }

            scoreMap.put(cid,
                    scoreMap.getOrDefault(cid, 0.0) + score);
        }

        // ======================
        // ⭐ 2. 点赞（修复）
        // ======================
        List<Post> likedPosts = postRepository.findLikedPosts(userId);

        for (Post post : likedPosts) {
            String cid = post.getCategoryId();
            scoreMap.put(cid,
                    scoreMap.getOrDefault(cid, 0.0) + 3);
        }

        // ======================
        // ⭐ 3. 收藏（从User取）
        // ======================
        User user = userRepository.findById(userId).orElse(null);

        if (user != null && user.getFavoritePosts() != null) {

            for (String postId : user.getFavoritePosts()) {

                Post post = postRepository.findById(postId).orElse(null);
                if (post == null) continue;

                String cid = post.getCategoryId();

                scoreMap.put(cid,
                        scoreMap.getOrDefault(cid, 0.0) + 5);
            }
        }

        // ======================
        // ❌ 4. 不喜欢
        // ======================
        if (user != null && user.getDislikedPostIds() != null) {

            for (String postId : user.getDislikedPostIds()) {

                Post post = postRepository.findById(postId).orElse(null);
                if (post == null) continue;

                String cid = post.getCategoryId();

                scoreMap.put(cid,
                        scoreMap.getOrDefault(cid, 0.0) - 5);
            }
        }

        return scoreMap;
    }

    private Set<String> getFollowingIds(String userId) {

        User user = userRepository.findById(userId).orElse(null);

        if (user == null || user.getFollowing() == null) {
            return new HashSet<>();
        }

        return new HashSet<>(user.getFollowing());
    }

    private double getDurationScore(String userId, String postId) {

        List<History> list = historyRepository.findByUserId(userId);

        for (History h : list) {
            if (h.getPostId().equals(postId) && h.getDuration() != null) {

                long d = h.getDuration();

                if (d > 30) return 50;   // 强兴趣
                if (d > 10) return 20;   // 中兴趣
                return 5;                // 弱兴趣
            }
        }
        return 0;
    }

    private Map<String, Map<String, Double>> buildUserBehaviorMatrix() {

        Map<String, Map<String, Double>> matrix = new HashMap<>();

        List<User> users = userRepository.findAll();

        for (User user : users) {

            Map<String, Double> behavior = new HashMap<>();

            String userId = user.getId();

            // 浏览记录
            List<History> historyList = historyRepository.findByUserId(userId);

            for (History h : historyList) {

                double score = 1;

                if (h.getDuration() != null) {
                    long d = h.getDuration();
                    if (d > 30) score += 5;
                    else if (d > 10) score += 2;
                }

                behavior.put(h.getPostId(),
                        behavior.getOrDefault(h.getPostId(), 0.0) + score);
            }

            // 点赞
            List<Post> likedPosts = postRepository.findLikedPosts(userId);

            for (Post p : likedPosts) {
                behavior.put(p.getId(),
                        behavior.getOrDefault(p.getId(), 0.0) + 3);
            }

// 收藏
            List<Post> favoritePosts = postRepository.findFavoritedPosts(userId);

            for (Post p : favoritePosts) {
                behavior.put(p.getId(),
                        behavior.getOrDefault(p.getId(), 0.0) + 5);
            }

            // 不喜欢
            if (user.getDislikedPostIds() != null) {
                for (String pid : user.getDislikedPostIds()) {
                    behavior.put(pid,
                            behavior.getOrDefault(pid, 0.0) - 5);
                }
            }

            matrix.put(userId, behavior);
        }

        return matrix;
    }

    private double cosineSimilarity(Map<String, Double> a,
                                    Map<String, Double> b) {

        Set<String> union = new HashSet<>();
        union.addAll(a.keySet());
        union.addAll(b.keySet());

        double dot = 0, normA = 0, normB = 0;

        for (String key : union) {
            double va = a.getOrDefault(key, 0.0);
            double vb = b.getOrDefault(key, 0.0);

            dot += va * vb;
            normA += va * va;
            normB += vb * vb;
        }

        if (normA == 0 || normB == 0) return 0;

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private List<String> findSimilarUsers(String userId,
                                          Map<String, Map<String, Double>> matrix) {

        Map<String, Double> target = matrix.get(userId);

        List<Map.Entry<String, Double>> list = new ArrayList<>();

        for (String otherId : matrix.keySet()) {

            if (otherId.equals(userId)) continue;

            double sim = cosineSimilarity(target, matrix.get(otherId));

            list.add(new AbstractMap.SimpleEntry<>(otherId, sim));
        }

        return list.stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Set<String> getCollaborativePosts(String userId) {

        Map<String, Map<String, Double>> matrix = buildUserBehaviorMatrix();

        List<String> similarUsers = findSimilarUsers(userId, matrix);

        Set<String> result = new HashSet<>();

        for (String uid : similarUsers) {

            List<History> list = historyRepository.findByUserId(uid);

            for (History h : list) {
                result.add(h.getPostId());
            }
        }

        return result;
    }

}
