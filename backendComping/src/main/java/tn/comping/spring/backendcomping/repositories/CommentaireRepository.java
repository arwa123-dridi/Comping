package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Commentaire;

import java.util.List;

@Repository
public interface CommentaireRepository extends MongoRepository<Commentaire, String> {
    List<Commentaire> findByPostIdOrderByDatePublicationAsc(String postId);
    List<Commentaire> findByPostIdAndParentCommentIdOrderByDatePublicationAsc(String postId, String parentCommentId);
    List<Commentaire> findByParentCommentIdOrderByDatePublicationAsc(String parentCommentId);
}
