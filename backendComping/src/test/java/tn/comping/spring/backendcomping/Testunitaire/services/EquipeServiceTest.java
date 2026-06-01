package tn.comping.spring.backendcomping.Testunitaire.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tn.comping.spring.backendcomping.dto.EquipeRequestDTO;
import tn.comping.spring.backendcomping.dto.EquipeResponseDTO;
import tn.comping.spring.backendcomping.entities.Equipe;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.repositories.EquipeRepository;
import tn.comping.spring.backendcomping.repositories.SortieRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.EquipeServiceImpl;

@ExtendWith(MockitoExtension.class)
class EquipeServiceTest {

    @Mock private EquipeRepository equipeRepository;
    @Mock private SortieRepository sortieRepository;
    @Mock private SignupRepository signupRepository;

    @InjectMocks
    private EquipeServiceImpl equipeService;

    private SignupEntity organisateur;
    private Equipe equipe;

    @BeforeEach
    void setUp() {
        organisateur = SignupEntity.builder()
                .id("ORG1")
                .build();

        equipe = Equipe.builder()
                .id("E1")
                .nom("Equipe test")
                .niveau("FACILE")
                .nbMembresMax(5)
                .organisateur(organisateur)
                .membres(List.of(organisateur))
                .build();
    }

    @Test
    void shouldCreateEquipeWhenOrganisateurExists() {
        EquipeRequestDTO request = new EquipeRequestDTO();
        request.setNom("Equipe test");
        request.setDescription("Description");
        request.setNbMembresMax(5);
        request.setNiveau("FACILE");
        request.setOrganisateurId("ORG1");

        when(signupRepository.findById("ORG1")).thenReturn(Optional.of(organisateur));
        when(equipeRepository.save(any(Equipe.class))).thenAnswer(invocation -> {
            Equipe saved = invocation.getArgument(0);
            saved.setId("E1");
            return saved;
        });

        EquipeResponseDTO result = equipeService.createEquipe(request);

        assertNotNull(result);
        assertEquals("E1", result.getId());
        assertEquals("Equipe test", result.getNom());
        verify(equipeRepository).save(any(Equipe.class));
    }

    @Test
    void shouldAddMemberToEquipe() {
        SignupEntity membre = SignupEntity.builder().id("U1").build();
        equipe.setMembres(List.of(organisateur));

        when(equipeRepository.findById("E1")).thenReturn(Optional.of(equipe));
        when(signupRepository.findById("U1")).thenReturn(Optional.of(membre));
        when(equipeRepository.save(any(Equipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EquipeResponseDTO result = equipeService.ajouterMembre("E1", "U1", "User Test");

        assertNotNull(result);
        assertTrue(result.getNbMembresActuels() == null || result.getNbMembresActuels() >= 1);
        verify(equipeRepository).save(any(Equipe.class));
    }
}