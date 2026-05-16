package tn.comping.spring.backendcomping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OllamaResponse {
    private String response;
    private boolean done;
    private String model;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("total_duration")
    private Long totalDuration;
    @JsonProperty("load_duration")
    private Long loadDuration;
    @JsonProperty("prompt_eval_duration")
    private Long promptEvalDuration;
    @JsonProperty("eval_duration")
    private Long evalDuration;
}
