package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.CibleType;
import tn.comping.spring.backendcomping.entities.Interaction;
import tn.comping.spring.backendcomping.entities.TypeInteraction;
import java.util.List;
import java.util.Optional;

@Repository
public interface InteractionRepository extends MongoRepository<Interaction, String> {
    
    // Vérifier si utilisateur a déjà interagi avec cette cible
    Optional<Interaction> findByUtilisateurIdAndCibleTypeAndCibleId(String utilisateurId, 
                                                                   CibleType cibleType, String cibleId);
    
    // Récupérer TOUS les likes d'une cible (type=LIKE seulement)
    List<Interaction> findByCibleTypeAndCibleIdAndTypeAndVisible(CibleType cibleType, 
                                                                 String cibleId, 
                                                                 TypeInteraction type, 
                                                                 boolean visible);
    
    // Récupérer TOUS les commentaires d'une cible (type=COMMENTAIRE)
    List<Interaction> findByCibleTypeAndCibleIdAndTypeAndVisibleOrderByDateInteractionDesc(
            CibleType cibleType, String cibleId, TypeInteraction type, boolean visible);
    
    // Compter les likes d'une cible
    long countByCibleTypeAndCibleIdAndTypeAndVisible(CibleType cibleType, String cibleId, 
                                                    TypeInteraction type, boolean visible);
    
    // Récupérer interactions d'un utilisateur pour une cible spécifique
    List<Interaction> findByUtilisateurIdAndCibleTypeAndCibleIdOrderByDateInteractionDesc(
            String utilisateurId, CibleType cibleType, String cibleId);
}

