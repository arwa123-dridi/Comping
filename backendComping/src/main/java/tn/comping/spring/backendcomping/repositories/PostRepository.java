package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Post;

import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByAuteurIdOrderByDatePublicationDesc(String auteurId);
    List<Post> findByAuteurIdOrderByDatePublicationDesc(String auteurId, Pageable pageable);
    List<Post> findByAvisId(String avisId);
    List<Post> findByCibleIdAndCibleTypeOrderByDatePublicationDesc(String cibleId, String cibleType);
    List<Post> findByAuteurIdInOrderByDatePublicationDesc(List<String> auteurIds, Pageable pageable);
}
