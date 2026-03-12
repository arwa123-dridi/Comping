package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EquipeResponseDTO {
    private String id;
    private String nom;
    private String description;
    private LocalDateTime dateCreation;
    private Integer nbMembresMax;
    private Integer nbMembresActuels;
    private String niveau;
    private String organisateurId;
    private String organisateurNom;
    private List<MembreDTO> membres;
    private Integer nombreSorties;
    private List<String> sortiesTitres;
}