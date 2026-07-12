package tn.comping.spring.backendcomping.entities;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueEntry {
    private Date date;
    private String statutPrecedent;
    private String statutNouveau;
    private String commentaire;
    private String auteurId;
}
