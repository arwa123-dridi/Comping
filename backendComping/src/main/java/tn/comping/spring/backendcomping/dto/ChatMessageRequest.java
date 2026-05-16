package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageRequest {
    private String message;
    private String context; // Optional context: "incident", "alert", "emergency"
    private String userId;
    private List<String> conversationHistory; // Previous messages for context
}
