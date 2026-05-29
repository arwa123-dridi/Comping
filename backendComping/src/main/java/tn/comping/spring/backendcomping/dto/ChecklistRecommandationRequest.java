package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistRecommandationRequest {
    private String city;
    private LocalDate date;
    private int difficulte;
}
