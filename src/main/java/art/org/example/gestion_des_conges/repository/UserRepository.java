package art.org.example.gestion_des_conges.repository;

import art.org.example.gestion_des_conges.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByResetToken(String resetToken);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // === NOUVELLES MÉTHODES POUR SPRINT 2 ===

    // US-02.3 : Recherche d'utilisateurs (nom, prénom, email, username)
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchUsers(@Param("search") String search);

    // Filtrer par rôle
    List<User> findByRole(User.Role role);

    // Filtrer par statut actif/inactif (vous utilisez 'active' avec Boolean)
    List<User> findByActive(Boolean active);

    // Trouver les utilisateurs par équipe
    List<User> findByTeamId(Long teamId);

    // Trouver les utilisateurs sans équipe
    List<User> findByTeamIsNull();

    // Trouver les validateurs actifs (pour US-02.6)
    List<User> findByRoleAndActiveTrue(User.Role role);

    // Compter les utilisateurs par rôle (pour statistiques)
    Long countByRole(User.Role role);



    // Recherche avancée avec multiple critères
    @Query("SELECT u FROM User u WHERE " +
            "(:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
            "(:lastName IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
            "(:role IS NULL OR u.role = :role) AND " +
            "(:active IS NULL OR u.active = :active)")
    List<User> findUsersByCriteria(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("role") User.Role role,
            @Param("active") Boolean active
    );

    // Vérifier si un email existe (sauf pour l'utilisateur courant - pour l'édition)
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM User u WHERE u.email = :email AND u.id != :excludeId")
    boolean existsByEmailExcludingId(@Param("email") String email,
                                     @Param("excludeId") Long excludeId);

    // Vérifier si un username existe (sauf pour l'utilisateur courant)
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM User u WHERE u.username = :username AND u.id != :excludeId")
    boolean existsByUsernameExcludingId(@Param("username") String username,
                                        @Param("excludeId") Long excludeId);

    // Trouver les validateurs qui ne sont pas déjà validateurs d'une équipe
    @Query("SELECT u FROM User u WHERE u.role = 'VALIDATEUR' AND u.active = true " +
            "AND NOT EXISTS (SELECT t FROM Team t WHERE t.validator = u)")
    List<User> findAvailableValidateurs();
}