package tn.comping.spring.backendcomping.utils.mapper;

import org.springframework.stereotype.Component;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.Avis;

@Component
public class AvisMapper {
    public Avis toEntity(AvisRequest req) {
        Avis a = new Avis();
        a.setSiteCampingId(req.getSiteCampingId());
        a.setUtilisateurId(req.getUtilisateurId());
        a.setNote(req.getNote());
        a.setCommentaire(req.getCommentaire());
        a.setItineraire(req.getItineraire());
        a.setConvention(req.getConvention());
        return a;
    }

    public AvisResponse toResponse(Avis a) {
        return new AvisResponse(
            a.getId(), a.getSiteCampingId(), a.getUtilisateurId(),
            a.getNote(), a.getCommentaire(),
            a.getDateCreation(), a.getStatutModeration()
        );
    }
}