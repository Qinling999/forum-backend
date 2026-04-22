package com.example.forum.controller;

import com.example.forum.model.Category;
import com.example.forum.repository.CategoryRepository;
import com.example.forum.common.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    // 获取所有分类
    @GetMapping("/list")
    public Result<List<Category>> getAll() {
        return Result.success(categoryRepository.findAll());
    }
}