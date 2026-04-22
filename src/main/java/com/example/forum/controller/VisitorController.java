package com.example.forum.controller;

import com.example.forum.common.Result;
import com.example.forum.model.Visitor;
import com.example.forum.service.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/visitor")
public class VisitorController {

    @Autowired
    private VisitorService visitorService;

    @GetMapping("/list/{userId}")
    public Result list(@PathVariable String userId) {
        return Result.success(visitorService.getVisitors(userId));
    }

    @GetMapping("/count/{userId}")
    public Result count(@PathVariable String userId) {
        return Result.success(visitorService.count(userId));
    }
}