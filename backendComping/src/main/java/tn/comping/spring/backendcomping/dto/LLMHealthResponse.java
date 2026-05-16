package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LLMHealthResponse {
    private boolean available;
    private String modelName;
    private String status;
    private String ollamaVersion;
    private Long responseTimeMs;
}
