package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.comping.spring.backendcomping.dto.SortiePlanifieeDTO;
import tn.comping.spring.backendcomping.dto.SortieResponseDTO;
import tn.comping.spring.backendcomping.entities.Participation;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.entities.Sortie;
import tn.comping.spring.backendcomping.entities.UserProfile;
import tn.comping.spring.backendcomping.repositories.ParticipationRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.repositories.SortieRepository;
import tn.comping.spring.backendcomping.repositories.UserProfileRepository;
import tn.comping.spring.backendcomping.services.IPlanningService;
import tn.comping.spring.backendcomping.utils.mapper.SortieMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PlanningServiceImpl implements IPlanningService {

    private final SortieRepository            sortieRepo;
    private final ParticipationRepository     partRepo;
    private final UserProfileRepository       profileRepo;
    private final SignupRepository            signupRepo;
    private final RecommandationServiceImpl   recommandationService;

    // ── Constantes ────────────────────────────────────────
    private static final int MAX_SORTIES_PLANNING  = 8;
    private static final int HORIZON_MOIS          = 3;
    private static final int ESPACEMENT_MIN_JOURS  = 7;

    // ══════════════════════════════════════════════════════
    //  1. GÉNÉRER LE PLANNING COMPLET
    // ══════════════════════════════════════════════════════

    @Override
    public List<SortiePlanifieeDTO> genererPlanning(String utilisateurId) {
        log.info("Génération planning → userId={}", utilisateurId);

        // ── Récupérer le profil utilisateur ───────────────
        UserProfile profil = recommandationService.construireOuMettreAJourProfil(utilisateurId);

        // ── Récupérer l'historique (participations passées) ──
        List<Participation> historique =
                partRepo.findByUtilisateurIdOrderByDateInscriptionDesc(utilisateurId);

        // ── Extraire les patterns comportementaux ─────────
        PatternUtilisateur pattern = extrairePattern(historique, profil);

        // ── Charger les sorties futures disponibles ───────
        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime horizon    = maintenant.plusMonths(HORIZON_MOIS);

        List<Sortie> candidates = sortieRepo.findBetweenDates(maintenant, horizon).stream()
                // Pas déjà inscrit
                .filter(s -> !partRepo.existsByUtilisateurIdAndSortieId(utilisateurId, s.getId()))
                // Places disponibles
                .filter(s -> {
                    int cap = s.getCapaciteMax() != null ? s.getCapaciteMax() : 0;
                    int ins = s.getParticipantIds() != null ? s.getParticipantIds().size() : 0;
                    return cap > 0 && ins < cap;
                })
                .collect(Collectors.toList());

        log.info("Candidates pour planning : {}", candidates.size());

        // ── Scorer et positionner dans le calendrier ──────
        List<SortiePlanifieeDTO> planifiees = new ArrayList<>();
        Set<LocalDate> datesOccupees        = new HashSet<>();

        candidates.stream()
                .map(s -> scorerEtPlanifier(s, profil, pattern))
                .sorted(Comparator.comparingInt(SortiePlanifieeDTO::getScoreMatch).reversed())
                .forEach(dto -> {
                    if (planifiees.size() >= MAX_SORTIES_PLANNING) return;

                    // Espacer les propositions d'au moins 7 jours
                    LocalDate date = dto.getDateRecommandee();
                    boolean conflit = datesOccupees.stream()
                            .anyMatch(d -> Math.abs(d.toEpochDay() - date.toEpochDay()) < ESPACEMENT_MIN_JOURS);

                    if (!conflit) {
                        planifiees.add(dto);
                        datesOccupees.add(date);
                    }
                });

        // ── Marquer le meilleur choix ─────────────────────
        if (!planifiees.isEmpty()) {
            planifiees.get(0).setEstMeilleurChoix(true);
        }

        log.info("Planning généré : {} sorties planifiées", planifiees.size());
        return planifiees;
    }

    // ══════════════════════════════════════════════════════
    //  2. PLANNING PAR MOIS
    // ══════════════════════════════════════════════════════

    @Override
    public List<SortiePlanifieeDTO> getPlanningParMois(String utilisateurId, String mois) {
        // mois = "2026-06"
        YearMonth ym = YearMonth.parse(mois, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate debut = ym.atDay(1);
        LocalDate fin   = ym.atEndOfMonth();

        return genererPlanning(utilisateurId).stream()
                .filter(dto -> {
                    LocalDate d = dto.getDateRecommandee();
                    return !d.isBefore(debut) && !d.isAfter(fin);
                })
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════
    //  3. VALIDER UNE SORTIE (INSCRIRE + MAJ PROFIL)
    // ══════════════════════════════════════════════════════

    @Override
    public void validerSortie(String utilisateurId, String sortieId) {
        // Vérifier que la sortie existe et a des places
        Sortie sortie = sortieRepo.findById(sortieId)
                .orElseThrow(() -> new RuntimeException("Sortie introuvable : " + sortieId));

        int cap = sortie.getCapaciteMax() != null ? sortie.getCapaciteMax() : 0;
        int ins = sortie.getParticipantIds() != null ? sortie.getParticipantIds().size() : 0;
        if (ins >= cap) throw new RuntimeException("Sortie complète");

        if (partRepo.existsByUtilisateurIdAndSortieId(utilisateurId, sortieId))
            throw new RuntimeException("Déjà inscrit");

        // Créer la participation
        SignupEntity user = signupRepo.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Participation p = Participation.builder()
                .utilisateur(user)
                .sortie(sortie)
                .dateInscription(LocalDateTime.now())
                .dateCreation(LocalDateTime.now())
                .statutPresence("CONFIRME")
                .aValideChecklist(false)
                .build();

        partRepo.save(p);

        // Ajouter l'ID dans participantIds
        if (sortie.getParticipantIds() == null) sortie.setParticipantIds(new ArrayList<>());
        sortie.getParticipantIds().add(utilisateurId);
        sortieRepo.save(sortie);

        // Recalculer le profil IA
        recommandationService.mettreAJourProfilApresInscription(utilisateurId);
        log.info("Sortie {} validée pour user {}", sortieId, utilisateurId);
    }

    // ══════════════════════════════════════════════════════
    //  LOGIQUE PRIVÉE
    // ══════════════════════════════════════════════════════

    /**
     * Score la sortie et calcule la dateRecommandee selon le pattern utilisateur.
     */
    private SortiePlanifieeDTO scorerEtPlanifier(Sortie sortie, UserProfile profil,
                                                 PatternUtilisateur pattern) {
        int score = 0;
        List<String> raisons = new ArrayList<>();

        // ── Région préférée → 35 pts ──────────────────────
        if (profil.getRegionsFrequentes() != null
                && sortie.getRegion() != null
                && profil.getRegionsFrequentes().contains(sortie.getRegion())) {
            score += 35;
            raisons.add("Région visitée " + compterVisites(profil, sortie.getRegion()) + " fois");
        }

        // ── Difficulté habituelle → 30 pts ────────────────
        if (profil.getDifficultesFrequentes() != null
                && sortie.getDifficulte() != null
                && profil.getDifficultesFrequentes().contains(sortie.getDifficulte().name())) {
            score += 30;
            raisons.add("Niveau similaire à vos " + profil.getNbParticipationsTotal() + " dernières sorties");
        }

        // ── Saison préférée → 20 pts ──────────────────────
        if (sortie.getDateDebut() != null) {
            String saison = saisonDe(sortie.getDateDebut());
            if (profil.getSaisonsPreferees() != null
                    && profil.getSaisonsPreferees().contains(saison)) {
                score += 20;
                raisons.add("Saison préférée : " + saisonLabel(saison));
            }
        }

        // ── Places libres → 15 pts ────────────────────────
        int libres = (sortie.getCapaciteMax() != null ? sortie.getCapaciteMax() : 0)
                - (sortie.getParticipantIds() != null ? sortie.getParticipantIds().size() : 0);
        if (libres > 0) {
            score += 15;
            raisons.add(libres + " place" + (libres > 1 ? "s" : "") + " disponible" + (libres > 1 ? "s" : ""));
        }

        if (raisons.isEmpty()) raisons.add("Sortie recommandée pour vous");

        // ── Date recommandée : utiliser la vraie date de la sortie ──
        LocalDate dateReco = sortie.getDateDebut() != null
                ? sortie.getDateDebut().toLocalDate()
                : LocalDate.now().plusWeeks(2);

        // Mapper la sortie
        SortieResponseDTO dto = SortieMapper.toDto(sortie);

        return SortiePlanifieeDTO.builder()
                .sortie(dto)
                .dateRecommandee(dateReco)
                .scoreMatch(Math.min(score, 100))
                .raisonsRecommandation(raisons)
                .estMeilleurChoix(false)
                .placesRestantes(libres)
                .saison(saisonDe(sortie.getDateDebut() != null ? sortie.getDateDebut() : LocalDateTime.now()))
                .build();
    }

    /** Extrait les patterns comportementaux depuis l'historique */
    private PatternUtilisateur extrairePattern(List<Participation> historique,
                                               UserProfile profil) {
        PatternUtilisateur p = new PatternUtilisateur();

        // Fréquence mensuelle moyenne
        if (!historique.isEmpty()) {
            long moisSpan = ChronoUnit.MONTHS.between(
                    historique.get(historique.size() - 1).getDateInscription().toLocalDate(),
                    LocalDate.now()) + 1;
            p.frequenceParMois = (double) historique.size() / Math.max(moisSpan, 1);
        }

        // Jours préférés
        p.joursPreferees = profil.getJoursPreferees() != null
                ? profil.getJoursPreferees()
                : List.of("SATURDAY", "SUNDAY");

        return p;
    }

    // ── Utilitaires ───────────────────────────────────────

    private String saisonDe(LocalDateTime ldt) {
        int m = ldt.getMonthValue();
        if (m >= 3 && m <= 5)  return "PRINTEMPS";
        if (m >= 6 && m <= 8)  return "ETE";
        if (m >= 9 && m <= 11) return "AUTOMNE";
        return "HIVER";
    }

    private String saisonLabel(String s) {
        return switch (s) {
            case "PRINTEMPS" -> "Printemps 🌿";
            case "ETE"       -> "Été ☀️";
            case "AUTOMNE"   -> "Automne 🍂";
            default          -> "Hiver ❄️";
        };
    }

    private int compterVisites(UserProfile profil, String region) {
        if (profil.getRegionCount() == null) return 1;
        Integer count = profil.getRegionCount().get(region);
        return count != null ? count : 1;
    }

    /** Classe interne légère pour les patterns */
    private static class PatternUtilisateur {
        double       frequenceParMois = 1.0;
        List<String> joursPreferees   = List.of("SATURDAY", "SUNDAY");
    }
}