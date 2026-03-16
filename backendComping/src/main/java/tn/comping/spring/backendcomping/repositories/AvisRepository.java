package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Avis;
import tn.comping.spring.backendcomping.entities.StatutAvis;
import tn.comping.spring.backendcomping.entities.TypeCible;

import java.util.List;

@Repository
public interface AvisRepository extends MongoRepository<Avis, String> {

    List<Avis> findByCibleIdAndTypeCibleAndValideOrderByDatePublicationDesc(
            String cibleId, TypeCible typeCible, boolean valide);

    List<Avis> findByUtilisateurIdOrderByDatePublicationDesc(String
                                                                     utilisateurId);

    List<Avis> findByStatutOrderByDatePublicationDesc(StatutAvis statut);

    long countByCibleIdAndTypeCibleAndValide(String cibleId, TypeCible
            typeCible, boolean valide);

    List<Avis> findByCibleIdAndTypeCible(String cibleId, TypeCible typeCible);

}