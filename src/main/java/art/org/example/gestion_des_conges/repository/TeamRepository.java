package art.org.example.gestion_des_conges.repository;

import art.org.example.gestion_des_conges.entity.Team;
import art.org.example.gestion_des_conges.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByName(String name);

    boolean existsByName(String name);

    // === NOUVELLES MÉTHODES POUR SPRINT 2 ===

    // US-02.6 : Trouver les équipes par validateur (ManyToOne donc plusieurs équipes par validateur)
    List<Team> findByValidator(User validator);

    // Trouver une équipe par ID de validateur
    Optional<Team> findByValidatorId(Long validatorId);

    // Trouver TOUTES les équipes d'un validateur (s'il peut en avoir plusieurs)
    List<Team> findAllByValidatorId(Long validatorId);

    // Trouver les équipes sans validateur
    List<Team> findByValidatorIsNull();

    // Trouver les équipes avec nom similaire
    @Query("SELECT t FROM Team t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Team> findSimilarByName(@Param("name") String name);

    // Compter les membres d'une équipe
    @Query("SELECT COUNT(u) FROM User u WHERE u.team.id = :teamId")
    Long countMembersByTeamId(@Param("teamId") Long teamId);

    // Trouver les équipes avec des membres
    @Query("SELECT DISTINCT t FROM Team t JOIN t.members m WHERE SIZE(t.members) > 0")
    List<Team> findTeamsWithMembers();

    // Trouver les équipes vides
    @Query("SELECT t FROM Team t WHERE SIZE(t.members) = 0 OR t.members IS EMPTY")
    List<Team> findEmptyTeams();

    // Recherche d'équipes
    @Query("SELECT t FROM Team t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Team> searchTeams(@Param("search") String search);

    // Trouver toutes les équipes avec leur nombre de membres
    @Query("SELECT t, COUNT(m) as memberCount FROM Team t LEFT JOIN t.members m GROUP BY t.id ORDER BY t.name")
    List<Object[]> findAllTeamsWithMemberCount();

    // Trouver une équipe avec tous ses membres (chargement eager)
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members WHERE t.id = :teamId")
    Optional<Team> findTeamWithMembersById(@Param("teamId") Long teamId);

    // Trouver les équipes d'un validateur avec leurs membres
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members WHERE t.validator.id = :validatorId")
    List<Team> findTeamsWithMembersByValidatorId(@Param("validatorId") Long validatorId);

    // Vérifier unicité du nom (sauf pour l'équipe courante)
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM Team t WHERE LOWER(t.name) = LOWER(:name) AND t.id != :excludeId")
    boolean existsByNameExcludingId(@Param("name") String name,
                                    @Param("excludeId") Long excludeId);

    // Trouver toutes les équipes avec leur validateur
    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.validator ORDER BY t.name")
    List<Team> findAllWithValidator();

    // Trouver les équipes où un utilisateur est membre
    @Query("SELECT t FROM Team t JOIN t.members m WHERE m.id = :userId")
    Optional<Team> findTeamByMemberId(@Param("userId") Long userId);
}