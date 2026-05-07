package tn.comping.spring.backendcomping.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequestDTO {
    private String conversationId;
    private String contenu;
    private String typeMessage; // TEXT, IMAGE
    private String transcription;
}
