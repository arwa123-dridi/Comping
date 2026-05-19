package tn.comping.spring.backendcomping.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
public class ReactionRequestDTO {
    private String emoji; // "❤️", "🔥", "👍", "😮", "😂"

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
}
