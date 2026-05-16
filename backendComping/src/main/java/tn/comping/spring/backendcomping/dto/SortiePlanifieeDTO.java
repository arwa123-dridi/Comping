// ── SortiePlanifieeDTO.java ──────────────────────────────────────────────────
package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SortiePlanifieeDTO {

    private SortieResponseDTO sortie;

    /** Date recommandée calculée par le service (dans les 3 prochains mois) */
    private LocalDate dateRecommandee;

    /** Score de correspondance 0–100 */
    private int scoreMatch;

    /** Raisons lisibles par l'utilisateur */
    private List<String> raisonsRecommandation;

    /** true pour la sortie ayant le meilleur score */
    private boolean estMeilleurChoix;

    /** Nombre de places restantes au moment du calcul */
    private int placesRestantes;

    /** Saison de la sortie */
    private String saison;
}