package com.example.forum.controller;

import com.example.forum.common.Result;
import com.example.forum.model.History;
import com.example.forum.repository.HistoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/history")
public class HistoryController {

    @Autowired
    private HistoryRepository historyRepository;

    // ========================
    // 1. 添加浏览记录
    // ========================
    @PostMapping("/add")
    public Result<String> addHistory(@RequestBody History history,
                                     HttpServletRequest request) {

        // ⭐ 从拦截器获取 userId
        String userId = (String) request.getAttribute("userId");

        if (userId == null || history.getPostId() == null) {
            return Result.error("参数错误");
        }

        // ⭐ 强制覆盖，防止前端伪造
        history.setUserId(userId);

        History exist = historyRepository
                .findByUserIdAndPostId(userId, history.getPostId());

        if (exist != null) {
            // 更新
            exist.setCreateTime(new Date());
            exist.setTitle(history.getTitle());

            // ⭐ 停留时间取最大值（你这个很好）
            if (history.getDuration() != null) {
                exist.setDuration(
                        Math.max(
                                exist.getDuration() == null ? 0 : exist.getDuration(),
                                history.getDuration()
                        )
                );
            }

            historyRepository.save(exist);

        } else {
            // 新增
            history.setCreateTime(new Date());

            // 防止 duration 为空
            if (history.getDuration() == null) {
                history.setDuration(0L);
            }

            historyRepository.save(history);
        }

        return Result.success("记录成功");
    }

    // ========================
    // 2. 获取我的浏览记录
    // ========================
    @GetMapping("/list/{userId}")
    public Result<List<History>> getHistory(@PathVariable String userId) {

        List<History> list =
                historyRepository.findByUserIdOrderByCreateTimeDesc(userId);

        // ⭐ 限制最多 20 条
        if (list.size() > 20) {
            list = list.subList(0, 20);
        }

        return Result.success(list);
    }

    // ========================
    // 3. 清空
    // ========================
    @DeleteMapping("/clear/{userId}")
    public Result<String> clear(@PathVariable String userId) {

        List<History> list =
                historyRepository.findByUserIdOrderByCreateTimeDesc(userId);

        historyRepository.deleteAll(list);

        return Result.success("已清空");
    }
}
