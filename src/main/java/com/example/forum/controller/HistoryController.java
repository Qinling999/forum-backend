package com.example.forum.controller;

import com.example.forum.common.Result;
import com.example.forum.model.History;
import com.example.forum.repository.HistoryRepository;
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
    public Result<String> addHistory(@RequestBody History history) {

        if (history.getUserId() == null || history.getPostId() == null) {
            return Result.error("参数错误");
        }

        History exist = historyRepository
                .findByUserIdAndPostId(history.getUserId(), history.getPostId());

        if (exist != null) {
            // ⭐ 已存在 → 更新
            exist.setCreateTime(new Date());
            exist.setTitle(history.getTitle()); // 防止标题变了
            historyRepository.save(exist);
        } else {
            // ⭐ 不存在 → 新增
            history.setCreateTime(new Date());
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
