package com.example.forum.repository;

import com.example.forum.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {
}