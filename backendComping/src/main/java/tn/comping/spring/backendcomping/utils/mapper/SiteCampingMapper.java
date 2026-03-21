package tn.comping.spring.backendcomping.utils.mapper;

import org.springframework.stereotype.Component;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.SiteCamping;

@Component
public class SiteCampingMapper {
    public SiteCamping toEntity(SiteCampingRequest req) {
        SiteCamping s = new SiteCamping();
        s.setNom(req.getNom());
        s.setDescription(req.getDescription());
        s.setLocalisation(req.getLocalisation());
        s.setLatitude(req.getLatitude());
        s.setLongitude(req.getLongitude());
        s.setCapacite(req.getCapacite());
        s.setTarifs(req.getTarifs());
        s.setDisponible(req.isDisponible());
        s.setConsignesSecurite(req.getConsignesSecurite());
        s.setPhotos(req.getPhotos());
        s.setProprietaireId(req.getProprietaireId());
        return s;
    }

    public SiteCampingResponse toResponse(SiteCamping s) {
        return new SiteCampingResponse(
            s.getId(), s.getNom(), s.getDescription(), s.getLocalisation(),
            s.getLatitude(), s.getLongitude(), s.getCapacite(), s.getTarifs(),
            s.isDisponible(), s.getConsignesSecurite(), s.getPhotos(),
            s.getNoteMoyenne(), s.getProprietaireId()
        );
    }
}