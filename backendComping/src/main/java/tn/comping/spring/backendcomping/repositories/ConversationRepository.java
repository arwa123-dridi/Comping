package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Conversation;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    
    Optional<Conversation> findByParticipant1IdAndParticipant2Id(String p1, String p2);
    Optional<Conversation> findByParticipant2IdAndParticipant1Id(String p2, String p1);
    
    List<Conversation> findByParticipant1IdOrParticipant2Id(String userId1, String userId2);
    
    // Get user's conversations
    List<Conversation> findByParticipant1IdOrParticipant2IdOrderByDateDernierMessageDesc(String p1, String p2);
    
}
