package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.Date;

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

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
}
