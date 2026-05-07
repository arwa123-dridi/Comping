package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Message;

import java.util.Date;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    
    List<Message> findByConversationIdOrderByDateCreationAsc(String conversationId);

    List<Message> findByConversationIdOrderByDateCreationAsc(String conversationId, Pageable pageable);
    
    List<Message> findByConversationIdAndExpediteurIdOrderByDateCreationDesc(String conversationId, String userId);
    
    long countByConversationIdAndDestinataireIdAndLuFalse(String conversationId, String userId);
    
    @Query("{ 'conversationId': ?0, 'dateCreation': { $gt: ?1 } }")
    List<Message> findRecentMessagesAfter(String conversationId, Date afterDate);
    
}
