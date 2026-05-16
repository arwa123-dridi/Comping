package tn.comping.spring.backendcomping.services.serviceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.comping.spring.backendcomping.dto.EquipeScoreDTO;
import tn.comping.spring.backendcomping.dto.SortieScoreDTO;
import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.repositories.*;
import java.util.Calendar;
import java.util.Date;
import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecommandationServiceImpl implements IRecommandationService {

    private final SortieRepository       sortieRepo;
    private final EquipeRepository       equipeRepo;
    private final ParticipationRepository partRepo;
    private final UserProfileRepository  profileRepo;
    private final SignupRepository       signupRepo;
    // ── Constantes ────────────────────────────────────────
    private static final int    SEUIL_HISTORIQUE = 3;
    private static final int    TOP_SORTIES      = 5;
    private static final int    TOP_EQUIPES      = 3;
    private static final double SEUIL_SCORE      = 0.20;

    // ══════════════════════════════════════════════════════
    //  1. RECOMMANDATION SORTIES
    // ══════════════════════════════════════════════════════

    @Override
    public List<SortieScoreDTO> recommanderSorties(String utilisateurId) {
        long nbPart = partRepo.countByUtilisateurId(utilisateurId);
        log.info("Recommandation sorties → userId={} nbParticipations={}", utilisateurId, nbPart);

        // Pas assez d'historique → sorties populaires
        if (nbPart < SEUIL_HISTORIQUE) {
            log.info("Historique insuffisant → sorties populaires");
            return getSortiesPopulaires();
        }

        UserProfile profil = construireOuMettreAJourProfil(utilisateurId);
LocalDateTime maintenant = LocalDateTime.now();

List<Sortie> candidats = sortieRepo.findByDateDebutAfter(maintenant);

        List<SortieScoreDTO> resultats = candidats.stream()
                // Exclure sorties déjà rejointes
                .filter(s -> !partRepo.existsByUtilisateurIdAndSortieId(utilisateurId, s.getId()))
                // Exclure sorties complètes
                .filter(s -> {
                    int max      = s.getCapaciteMax() != null ? s.getCapaciteMax() : 0;
                    int inscrits = s.getParticipantIds() != null ? s.getParticipantIds().size() : 0;
                    return inscrits < max;
                })
                .map(s -> {
                    double score  = calculerScoreSortie(profil, s);
                    String raison = construireRaisonSortie(profil, s);
                    log.debug("Sortie '{}' → score={}", s.getTitre(), score);
                    return new SortieScoreDTO(s, score, raison);
                })
                .filter(dto -> dto.getScore() >= SEUIL_SCORE)
                .sorted(Comparator.comparingDouble(SortieScoreDTO::getScore).reversed())
                .limit(TOP_SORTIES)
                .collect(Collectors.toList());

        return resultats.isEmpty() ? getSortiesPopulaires() : resultats;
    }

    // ── Calcul score sortie ───────────────────────────────
    private double calculerScoreSortie(UserProfile profil, Sortie sortie) {
        double score = 0.0;

        // Région fréquente → +0.35
        if (profil.getRegionsFrequentes() != null
                && sortie.getRegion() != null
                && profil.getRegionsFrequentes().contains(sortie.getRegion())) {
            score += 0.35;
        }

        // Difficulté habituelle → +0.30
        if (profil.getDifficultesFrequentes() != null
                && sortie.getDifficulte() != null
                && profil.getDifficultesFrequentes().contains(sortie.getDifficulte())) {
            score += 0.30;
        }

        // Saison préférée → +0.20
        if (sortie.getDateDebut() != null) {
            String saisonSortie = calculerSaisonLocal(sortie.getDateDebut());
            if (profil.getSaisonsPreferees() != null
                    && profil.getSaisonsPreferees().contains(saisonSortie)) {
                score += 0.20;
            }
        }

        // Places disponibles → +0.15
        int placesLibres = (sortie.getCapaciteMax() != null ? sortie.getCapaciteMax() : 0)
                - (sortie.getParticipantIds() != null ? sortie.getParticipantIds().size() : 0);
        if (placesLibres > 0) score += 0.15;

        return Math.min(score, 1.0);
    }

    private String construireRaisonSortie(UserProfile profil, Sortie sortie) {
        List<String> raisons = new ArrayList<>();

        if (profil.getRegionsFrequentes() != null
                && profil.getRegionsFrequentes().contains(sortie.getRegion())) {
            raisons.add("Région habituelle");
        }
        if (profil.getDifficultesFrequentes() != null
                && profil.getDifficultesFrequentes().contains(sortie.getDifficulte().name())) {
            raisons.add("Niveau adapté");
        }
        if (sortie.getDateDebut() != null) {
            String saison = calculerSaisonLocal(sortie.getDateDebut());
            if (profil.getSaisonsPreferees() != null
                    && profil.getSaisonsPreferees().contains(saison)) {
                raisons.add("Saison préférée");
            }
        }
        return raisons.isEmpty() ? "Recommandé pour vous" : String.join(" · ", raisons);
    }

    // ══════════════════════════════════════════════════════
    //  2. RECOMMANDATION ÉQUIPES
    // ══════════════════════════════════════════════════════

    @Override
    public List<EquipeScoreDTO> recommanderEquipes(String utilisateurId) {
        long nbPart = partRepo.countByUtilisateurId(utilisateurId);
        log.info("Recommandation équipes → userId={} nbParticipations={}", utilisateurId, nbPart);

        if (nbPart < SEUIL_HISTORIQUE) {
            return getEquipesDisponibles(utilisateurId);
        }

        UserProfile profil = construireOuMettreAJourProfil(utilisateurId);

        List<Equipe> candidats = equipeRepo.findAll().stream()
                // Exclure équipes dont l'user est déjà membre
                .filter(eq -> eq.getMembres() == null
                        || eq.getMembres().stream().noneMatch(m -> m.getId().equals(utilisateurId)))
                // Exclure équipes complètes
                .filter(eq -> {
                    int max     = eq.getNbMembresMax() != null ? eq.getNbMembresMax() : 10;
                    int membres = eq.getMembres()      != null ? eq.getMembres().size() : 0;
                    return membres < max;
                })
                .collect(Collectors.toList());

        List<EquipeScoreDTO> resultats = candidats.stream()
                .map(eq -> {
                    double score  = calculerScoreEquipe(profil, eq);
                    String raison = construireRaisonEquipe(profil, eq);
                    return new EquipeScoreDTO(eq, score, raison);
                })
                .filter(dto -> dto.getScore() >= SEUIL_SCORE)
                .sorted(Comparator.comparingDouble(EquipeScoreDTO::getScore).reversed())
                .limit(TOP_EQUIPES)
                .collect(Collectors.toList());

        return resultats.isEmpty() ? getEquipesDisponibles(utilisateurId) : resultats;
    }

    // ── Calcul score équipe ───────────────────────────────
    private double calculerScoreEquipe(UserProfile profil, Equipe equipe) {
        double score = 0.0;

        // Niveau compatible → +0.40
        int niveauUser   = profil.getNiveauNum();
        int niveauEquipe = niveauStringToNum(equipe.getNiveau());
        int ecart        = Math.abs(niveauUser - niveauEquipe);
        if      (ecart == 0) score += 0.40;
        else if (ecart == 1) score += 0.20;

        // Taux de remplissage → +0.30
        int max     = equipe.getNbMembresMax() != null ? equipe.getNbMembresMax() : 10;
        int membres = equipe.getMembres()      != null ? equipe.getMembres().size() : 0;
        double taux = (double) membres / max;
        if      (taux < 0.50) score += 0.30;
        else if (taux < 0.85) score += 0.15;

        // Équipe active (bonus fixe) → +0.30
        score += 0.30;

        return Math.min(score, 1.0);
    }

    private String construireRaisonEquipe(UserProfile profil, Equipe equipe) {
        List<String> raisons = new ArrayList<>();

        int ecart = Math.abs(profil.getNiveauNum() - niveauStringToNum(equipe.getNiveau()));
        if      (ecart == 0) raisons.add("Niveau identique");
        else if (ecart == 1) raisons.add("Niveau compatible");

        int max     = equipe.getNbMembresMax() != null ? equipe.getNbMembresMax() : 10;
        int membres = equipe.getMembres()      != null ? equipe.getMembres().size() : 0;
        if (membres < max) raisons.add((max - membres) + " place(s) libre(s)");

        return raisons.isEmpty() ? "Équipe recommandée" : String.join(" · ", raisons);
    }

    // ══════════════════════════════════════════════════════
    //  3. CONSTRUCTION DU PROFIL DEPUIS L'HISTORIQUE
    // ══════════════════════════════════════════════════════

    @Override
    public UserProfile construireOuMettreAJourProfil(String utilisateurId) {
        // Retourner profil existant s'il est récent (< 1h)
        Optional<UserProfile> existing = profileRepo.findByUtilisateurId(utilisateurId);
        if (existing.isPresent()) {
            UserProfile p = existing.get();
            if (p.getDerniereMiseAJour() != null) {
                long minutes = java.time.temporal.ChronoUnit.MINUTES.between(
                        p.getDerniereMiseAJour(), java.time.LocalDateTime.now());
                if (minutes < 60) return p;
            }
        }
        return recalculerProfil(utilisateurId);
    }

    private UserProfile recalculerProfil(String utilisateurId) {
        List<Participation> historique =
                partRepo.findByUtilisateurIdOrderByDateInscriptionDesc(utilisateurId);

        log.info("Recalcul profil userId={} → {} participations", utilisateurId, historique.size());

        // Récupérer l'entité utilisateur
        SignupEntity utilisateur = signupRepo.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable: " + utilisateurId));

        UserProfile profil = new UserProfile();
        profil.setUtilisateur(utilisateur);
        profil.setNbParticipationsTotal(historique.size());
        profil.setDerniereMiseAJour(java.time.LocalDateTime.now());

        // ── Compter régions ───────────────────────────────
        Map<String, Long> regionCount = historique.stream()
                .filter(p -> p.getSortie() != null && p.getSortie().getRegion() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getSortie().getRegion(), Collectors.counting()));

        profil.setRegionsFrequentes(topN(regionCount, 3));

        // ── Compter difficultés ───────────────────────────
        Map<String, Long> diffCount = historique.stream()
                .filter(p -> p.getSortie() != null && p.getSortie().getDifficulte() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getSortie().getDifficulte().toString(), Collectors.counting()));

        List<String> topDiffs = topN(diffCount, 2);
        profil.setDifficultesFrequentes(topDiffs);
        if (!topDiffs.isEmpty()) profil.setNiveauDominant(topDiffs.get(0));
        // ── Compter saisons ───────────────────────────────
        Map<String, Long> saisonCount = historique.stream()
                .filter(p -> p.getDateInscription() != null)
                .collect(Collectors.groupingBy(
                        p -> calculerSaisonLocal(p.getDateInscription()), Collectors.counting()));

        profil.setSaisonsPreferees(topN(saisonCount, 2));

        // ── Compter jours de semaine ──────────────────────
        Map<String, Long> jourCount = historique.stream()
                .filter(p -> p.getDateInscription() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getDateInscription().getDayOfWeek().name(), Collectors.counting()));

        profil.setJoursPreferees(topN(jourCount, 2));

        // Supprimer ancien profil et sauvegarder le nouveau
        profileRepo.deleteByUtilisateurId(utilisateurId);
        return profileRepo.save(profil);
    }

    @Override
    public void mettreAJourProfilApresInscription(String utilisateurId) {
        profileRepo.deleteByUtilisateurId(utilisateurId);
        if (partRepo.countByUtilisateurId(utilisateurId) >= SEUIL_HISTORIQUE) {
            recalculerProfil(utilisateurId);
            log.info("Profil recalculé après inscription → userId={}", utilisateurId);
        }
    }


    // ══════════════════════════════════════════════════════
    //  4. FALLBACKS
    // ══════════════════════════════════════════════════════

    private List<SortieScoreDTO> getSortiesPopulaires() {
        return sortieRepo.findByDateDebutAfter(LocalDateTime.now()).stream()
                .filter(s -> s.getCapaciteMax() != null)
                .sorted(Comparator.comparingInt(s ->
                        -(s.getParticipantIds() != null ? s.getParticipantIds().size() : 0)))
                .limit(TOP_SORTIES)
                .map(s -> {
                    SortieScoreDTO dto = new SortieScoreDTO(s, 0.5, "Populaire en ce moment");
                    dto.setEstPopulaire(true);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private List<EquipeScoreDTO> getEquipesDisponibles(String utilisateurId) {
        return equipeRepo.findAll().stream()
                .filter(eq -> {
                    int max     = eq.getNbMembresMax() != null ? eq.getNbMembresMax() : 10;
                    int membres = eq.getMembres()      != null ? eq.getMembres().size() : 0;
                    return membres < max;
                })
                .limit(TOP_EQUIPES)
                .map(eq -> {
                    EquipeScoreDTO dto = new EquipeScoreDTO(eq, 0.5, "Places disponibles");
                    dto.setEstPopulaire(true);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════
    //  5. UTILITAIRES
    // ══════════════════════════════════════════════════════

    private String calculerSaison(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int mois = cal.get(Calendar.MONTH) + 1;
        if (mois >= 3 && mois <= 5)  return "PRINTEMPS";
        if (mois >= 6 && mois <= 8)  return "ETE";
        if (mois >= 9 && mois <= 11) return "AUTOMNE";
        return "HIVER";
    }

    private String calculerSaisonLocal(java.time.LocalDateTime ldt) {
        int mois = ldt.getMonthValue();
        if (mois >= 3 && mois <= 5)  return "PRINTEMPS";
        if (mois >= 6 && mois <= 8)  return "ETE";
        if (mois >= 9 && mois <= 11) return "AUTOMNE";
        return "HIVER";
    }

    private int niveauStringToNum(String niveau) {
        if (niveau == null) return 1;
        return switch (niveau) {
            case "Débutant",    "FACILE"    -> 0;
            case "Intermédiaire","MOYEN"    -> 1;
            case "Avancé",      "DIFFICILE" -> 2;
            default                         -> 1;
        };
    }

    // Top N entrées d'une Map<String, Long> par valeur décroissante
    private List<String> topN(Map<String, Long> map, int n) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(n)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }


}