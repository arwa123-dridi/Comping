// ============================================================
// FILE: dto/GroupConversationRequestDTO.java
// ============================================================
package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupConversationRequestDTO {
    private String nomGroupe;
    private List<String> participantIds; // emails or IDs, min 2
    private String avatarGroupe;
}
