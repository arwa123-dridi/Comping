package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "AssignmentStrategy")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentStrategy {
    @Id
    private String id;
    private String name;
    private String description;
    
    // ROUND_ROBIN, EXPERTISE_FIRST, LEAST_LOADED, BALANCED, MANUAL
    private AssignmentType type;
    
    @Builder.Default
    private Map<String, Double> weights = new HashMap<>();
    // weights can include: "workload", "expertise", "responseTime", "availability"
    
    private Boolean enabled;
    private Integer priority; // Higher number = higher priority when multiple strategies apply
    
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    
    public enum AssignmentType {
        ROUND_ROBIN,
        EXPERTISE_FIRST,
        LEAST_LOADED,
        BALANCED,
        MANUAL
    }
}
