package tn.comping.spring.backendcomping.utils.mapper;

import org.springframework.stereotype.Component;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.Alerte;

@Component
public class AlerteMapper {
    public Alerte toEntity(AlerteRequest req) {
        Alerte a = new Alerte();
        a.setSiteCampingId(req.getSiteCampingId());
        a.setType(req.getType());
        a.setTitre(req.getTitre());
        a.setDescription(req.getDescription());
        a.setPosition(req.getPosition());
        return a;
    }

    public AlerteResponse toResponse(Alerte a) {
        return new AlerteResponse(
            a.getId(), a.getSiteCampingId(), a.getType(),
            a.getTitre(), a.getDescription(),
            a.getDateDeclenchement(), a.getStatut(), a.getPosition()
        );
    }
}