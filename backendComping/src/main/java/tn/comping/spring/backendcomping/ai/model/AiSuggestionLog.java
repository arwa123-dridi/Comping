package tn.comping.spring.backendcomping.ai.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tn.comping.spring.backendcomping.ai.dto.PostDraft;

import java.time.Instant;

@Data
@Builder
@Document(collection = "ai_suggestions_log")
public class AiSuggestionLog {

    @Id
    private String id;
    private String topic;
    private PostDraft draft;
    private Instant timestamp;
    /** Email de l'utilisateur connecté, peut être null si non disponible. */
    private String userId;
}
