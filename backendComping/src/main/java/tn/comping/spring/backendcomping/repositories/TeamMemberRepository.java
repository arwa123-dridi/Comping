package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.TeamMember;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends MongoRepository<TeamMember, String> {
    Optional<TeamMember> findByEmail(String email);
    List<TeamMember> findBySpecializationsContaining(String specialization);
    List<TeamMember> findByAvailableIsTrue();
    List<TeamMember> findByRole(String role);
    List<TeamMember> findByTeam(String team);
}
