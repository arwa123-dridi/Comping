package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Conversation;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    
    List<Conversation> findByParticipant1IdOrParticipant2IdOrderByDateDernierMessageDesc(
            String participant1Id, String participant2Id);
    
    Optional<Conversation> findByParticipant1IdAndParticipant2IdOrParticipant2IdAndParticipant1Id(
            String p1, String p2, String p2b, String p1b);
    
    List<Conversation> findByParticipant1IdAndActiveOrParticipant2IdAndActiveOrderByDateDernierMessageDesc(
            String userId, boolean active1, String userId2, boolean active2);
}

