package tn.comping.spring.backendcomping.ai.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.comping.spring.backendcomping.ai.model.AiSuggestionLog;

public interface AiSuggestionLogRepository extends MongoRepository<AiSuggestionLog, String> {}
