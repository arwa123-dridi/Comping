package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Message;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    
    List<Message> findByConversationIdOrderByDateEnvoiAsc(String conversationId);
    
    List<Message> findByConversationIdAndSupprimeFalseOrderByDateEnvoiAsc(String conversationId);
    
    List<Message> findByConversationIdAndDestinataireIdAndLuFalseAndSupprimeFalse(String conversationId, String destinataireId);
    
    long countByConversationIdAndDestinataireIdAndLuFalseAndSupprimeFalse(String conversationId, String destinataireId);
    
    // Messages non lus pour un utilisateur dans une conversation
    long countByConversationIdAndDestinataireIdAndLuFalseAndSupprimeFalseOrderByDateEnvoiDesc(
            String conversationId, String destinataireId);
}

