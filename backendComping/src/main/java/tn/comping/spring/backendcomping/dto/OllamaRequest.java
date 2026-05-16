package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OllamaRequest {
    private String model;
    private String prompt;
    private boolean stream;
    private int temperature;
    private int top_k;
    private double top_p;
    private int num_predict;
}
