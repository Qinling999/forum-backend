package com.example.forum.repository;

import com.example.forum.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation, String> {

    Optional<Conversation> findByUserAAndUserB(String userA, String userB);
}