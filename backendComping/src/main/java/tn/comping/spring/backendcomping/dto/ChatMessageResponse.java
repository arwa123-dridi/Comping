package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponse {
    private String id;
    private String message;
    private String response;
    private String context;
    private String userId;
    private Date timestamp;
    private Long processingTimeMs;
    private boolean success;
    private String error;
}
